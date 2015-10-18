package com.lesfriches.smartcells.model;

import java.util.ArrayList;

/**
 * A technical footprint, created from a set of measures.
 * Contains ids of cells, and for each cell number of ids count, and signal strengh average.
 */
public class FootPrint {

    public ArrayList<ComputedTowerInfo> computedTowerInfos = new ArrayList<ComputedTowerInfo>(0);

    public int indexOf(TowerInfo ti) {
        for (ComputedTowerInfo cti : computedTowerInfos) {
            boolean isRealId = (ti.id != TowerInfo.BAD_ID_MAX && ti.id != TowerInfo.BAD_ID_MIN);
            boolean isRealPsc = (ti.psc != -1);
            if (isRealId && cti.id == ti.id) { // found CTI with existing id, returns index
                return computedTowerInfos.indexOf(cti);

            }
            if (cti.psc != -1 && cti.psc == ti.psc) { // found CTI with existing psc, returns index
                return computedTowerInfos.indexOf(cti);
            }
        }
        return -1; // not found, returns -1
    }

    @Override
    public String toString() {
        String returned = "";
        for (ComputedTowerInfo cti : computedTowerInfos) {
            returned += cti.toString();
        }
        return returned;
    }
}
