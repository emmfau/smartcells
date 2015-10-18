package com.lesfriches.smartcells.manager;

import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.lesfriches.smartcells.SmartCellsApplication;
import com.lesfriches.smartcells.model.ComputedTowerInfo;
import com.lesfriches.smartcells.model.FootPrint;
import com.lesfriches.smartcells.model.LocationMatch;
import com.lesfriches.smartcells.model.Measure;
import com.lesfriches.smartcells.model.TowerInfo;

import java.util.LinkedList;

/**
 * Manager for cells operations ( create a measure, create a footprint)
 * Used both by activities and services
 */
public class CellManager {

    private SmartCellsApplication app;

    public CellManager(SmartCellsApplication scapp) {
        app = scapp;
    }

    /**
     * Create a measure from current CellInfo
     *
     * @return a current measure
     */
    public Measure createMeasure() {
        // Create a measure
        Measure m = new Measure();
        m.timestamp = System.currentTimeMillis();
        TelephonyManager tel = (TelephonyManager) app.getSystemService(Context.TELEPHONY_SERVICE);
        for (CellInfo ci : tel.getAllCellInfo()) {
            TowerInfo ti = new TowerInfo();
            if (ci instanceof CellInfoCdma) {
                ti.type = TowerInfo.CDMA;
                ti.id = ((CellInfoCdma) ci).getCellIdentity().getBasestationId();
                ti.psc = -1;
                ti.dbm = ((CellInfoCdma) ci).getCellSignalStrength().getDbm();
            } else if (ci instanceof CellInfoGsm) {
                ti.type = TowerInfo.GSM;
                ti.id = ((CellInfoGsm) ci).getCellIdentity().getCid();
                ti.psc = -1; // undefined for GSM
                ti.dbm = ((CellInfoGsm) ci).getCellSignalStrength().getDbm();
            } else if (ci instanceof CellInfoLte) {
                ti.type = TowerInfo.LTE;
                ti.id = ((CellInfoLte) ci).getCellIdentity().getCi();
                ti.psc = -1;
                ti.dbm = ((CellInfoLte) ci).getCellSignalStrength().getDbm();
            } else if (ci instanceof CellInfoWcdma) {
                ti.type = TowerInfo.WCDMA;
                ti.id = ((CellInfoWcdma) ci).getCellIdentity().getCid();
                ti.psc = ((CellInfoWcdma) ci).getCellIdentity().getPsc();
                ti.dbm = ((CellInfoWcdma) ci).getCellSignalStrength().getDbm();
            }
            m.towerInfos.add(ti);
        }
        return m;
    }

    /**
     * Create a global footprint from all measures
     *
     * @param measures
     * @return
     */
    public FootPrint createFootPrint(LinkedList<Measure> measures) {
        FootPrint globalFootPrint = new FootPrint();
        for (Measure m : measures) {
            for (TowerInfo ti : m.towerInfos) {
                // check if tower already present in footprint
                int index = globalFootPrint.indexOf(ti);
                if (index != -1) { // tower already present
                    ComputedTowerInfo cti = globalFootPrint.computedTowerInfos.get(index);
                    // check if there is real id values (cid, sid, psc, ..)
                    boolean isRealId = (ti.id != TowerInfo.BAD_ID_MAX && ti.id != TowerInfo.BAD_ID_MIN);
                    boolean isRealPsc = (ti.psc != -1);
                    if (cti.id == -1 && isRealId) { // update id if exists and not present
                        cti.id = ti.id;
                    }
                    if (cti.psc == -1 && isRealPsc) { // update psc if exists and not present
                        cti.psc = ti.psc;
                    }
                    // update footprint values
                    cti.dbmSum += ti.dbm;
                    cti.dbmCount++;
                    cti.count++;
                } else { // tower not present, create it
                    // create only if there is real id values (cid, sid, psc, ..)
                    boolean isRealId = (ti.id != TowerInfo.BAD_ID_MAX && ti.id != TowerInfo.BAD_ID_MIN);
                    boolean isRealPsc = (ti.psc != -1);
                    if (isRealId || isRealPsc) {
                        ComputedTowerInfo cti = new ComputedTowerInfo();
                        // get id only if real
                        if (isRealId) {
                            cti.id = ti.id;
                        }
                        // get psc only if real
                        if (isRealPsc) {
                            cti.psc = ti.psc;
                        }
                        // initialize footprint values
                        cti.dbmSum = ti.dbm;
                        cti.dbmCount = 1;
                        cti.count = 1;
                        globalFootPrint.computedTowerInfos.add(cti);
                    }

                }

            }
        }
        return globalFootPrint;
    }

    /**
     * Create footprint from some measures int a period of time defined by start / end
     *
     * @param measures
     * @param startTimestamp
     * @param endTimestamp
     * @return
     */
    public FootPrint createFootPrint(LinkedList<Measure> measures, long startTimestamp, long endTimestamp) {
        FootPrint footPrint = new FootPrint();
        for (Measure m : measures) {
            if (m.timestamp >= startTimestamp && m.timestamp <= endTimestamp) {
                for (TowerInfo ti : m.towerInfos) {
                    // check if tower already present in footprint
                    int index = footPrint.indexOf(ti);
                    if (index != -1) { // tower already present
                        ComputedTowerInfo cti = footPrint.computedTowerInfos.get(index);
                        // check if there is real id values (cid, sid, psc, ..)
                        boolean isRealId = (ti.id != TowerInfo.BAD_ID_MAX && ti.id != TowerInfo.BAD_ID_MIN);
                        boolean isRealPsc = (ti.psc != -1);
                        if (cti.id == -1 && isRealId) { // update id if exists and not present
                            cti.id = ti.id;
                        }
                        if (cti.psc == -1 && isRealPsc) { // update psc if exists and not present
                            cti.psc = ti.psc;
                        }
                        // update footprint values
                        cti.dbmSum += ti.dbm;
                        cti.dbmCount++;
                        cti.count++;
                    } else { // tower not present, create it
                        // create only if there is real id values (cid, sid, psc, ..)
                        boolean isRealId = (ti.id != TowerInfo.BAD_ID_MAX && ti.id != TowerInfo.BAD_ID_MIN);
                        boolean isRealPsc = (ti.psc != -1);
                        if (isRealId || isRealPsc) {
                            ComputedTowerInfo cti = new ComputedTowerInfo();
                            // get id only if real
                            if (isRealId) {
                                cti.id = ti.id;
                            }
                            // get psc only if real
                            if (isRealPsc) {
                                cti.psc = ti.psc;
                            }
                            // initialize footprint values
                            cti.dbmSum = ti.dbm;
                            cti.dbmCount = 1;
                            cti.count = 1;
                            footPrint.computedTowerInfos.add(cti);
                        }

                    }

                }
            }
        }
        return footPrint;
    }

    /**
     * Return the result of two footprints comparison
     * @param footprint1
     * @param footprint2
     * @return a LocationMatch object with detailed results of analysis
     */
    public LocationMatch compareFootprints(FootPrint footprint1, FootPrint footprint2) {
        LocationMatch lMatchReturned = new LocationMatch();
        if (footprint1 == null || footprint2 == null) {
            return lMatchReturned;
        }
        FootPrint computedFootprint1 = new FootPrint();
        FootPrint computedFootprint2 = new FootPrint();

        // Step 1 : compute footprints percents, and keep only TowerInfo > 10% of representation
        int totalFootprint1Count = 0;
        for (ComputedTowerInfo cti : footprint1.computedTowerInfos) {
            totalFootprint1Count += cti.count;
        }
        for (ComputedTowerInfo cti : footprint1.computedTowerInfos) {
            cti.averagePercent = ((float) cti.count) / ((float) totalFootprint1Count) * 100;
            if (cti.averagePercent > 10) {
                computedFootprint1.computedTowerInfos.add(cti.clone());
            }
        }
        int totalFootprint2Count = 0;
        for (ComputedTowerInfo cti : footprint2.computedTowerInfos) {
            totalFootprint2Count += cti.count;
        }
        for (ComputedTowerInfo cti : footprint2.computedTowerInfos) {
            cti.averagePercent = ((float) cti.count) / ((float) totalFootprint2Count) * 100;
            if (cti.averagePercent > 10) {
                computedFootprint2.computedTowerInfos.add(cti.clone());
            }
        }

        FootPrint commonFootprint = new FootPrint();

        float averageDifference = 0;
        for (ComputedTowerInfo cti : computedFootprint1.computedTowerInfos) {
            // If CTI is common, compute percent difference, and add to common
            if (computedFootprint2.computedTowerInfos.contains(cti)) {
                int index = computedFootprint2.computedTowerInfos.indexOf(cti);
                cti.differencePercent = Math.abs(cti.averagePercent - computedFootprint2.computedTowerInfos.get(index).averagePercent);
                commonFootprint.computedTowerInfos.add(cti.clone());
                averageDifference += cti.differencePercent;
            }
        }

        lMatchReturned.commonFootPrint = commonFootprint;

        // Common footprint contains X% of current location cells
        if (totalFootprint2Count != 0) {
            float a = commonFootprint.computedTowerInfos.size();
            float b = computedFootprint2.computedTowerInfos.size();
            Log.i("CellManager", "footprintcommon => " + a + " , " + b);
            lMatchReturned.currentCommonLocationsPercent = (int) (a / b * 100);
        }

        // Compute average difference, to create percent match
        if (commonFootprint.computedTowerInfos.size() != 0) {
            averageDifference = averageDifference / commonFootprint.computedTowerInfos.size();
            lMatchReturned.currentMatchPercent = 100 - (int) averageDifference;
        } else {
            averageDifference = -1;
        }

        return lMatchReturned;
    }

}
