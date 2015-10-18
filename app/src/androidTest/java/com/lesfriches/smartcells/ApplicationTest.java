package com.lesfriches.smartcells;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.lesfriches.smartcells.manager.CellManager;
import com.lesfriches.smartcells.model.ComputedTowerInfo;
import com.lesfriches.smartcells.model.FootPrint;
import com.lesfriches.smartcells.model.LocationMatch;

/**
 * Unit testing of sensible use cases : footprint comparison
 */
public class ApplicationTest extends ApplicationTestCase<Application> {

    public ApplicationTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCompareFootprint() {
        ComputedTowerInfo cti1 = new ComputedTowerInfo();
        cti1.id = 1001;
        cti1.psc = 201;
        cti1.count = 75;
        cti1.dbm = 110;
        ComputedTowerInfo cti2 = new ComputedTowerInfo();
        cti2.id = 1002;
        cti2.psc = 202;
        cti2.count = 35;
        cti2.dbm = 113;
        ComputedTowerInfo cti3 = new ComputedTowerInfo();
        cti3.id = 1003;
        cti3.psc = 203;
        cti3.count = 12;
        cti3.dbm = 108;
        ComputedTowerInfo cti4 = new ComputedTowerInfo();
        cti4.id = 1004;
        cti4.psc = 204;
        cti4.count = 17;
        cti4.dbm = 98;
        ComputedTowerInfo cti5 = new ComputedTowerInfo();
        cti5.id = 1005;
        cti5.psc = 205;
        cti5.count = 8;
        cti5.dbm = 110;
        FootPrint footPrintGlobal = new FootPrint();
        footPrintGlobal.computedTowerInfos.add(cti1);
        footPrintGlobal.computedTowerInfos.add(cti2);
        footPrintGlobal.computedTowerInfos.add(cti3);
        footPrintGlobal.computedTowerInfos.add(cti4);
        footPrintGlobal.computedTowerInfos.add(cti5);

        ComputedTowerInfo cti6 = new ComputedTowerInfo();
        cti6.id = -1;
        cti6.psc = 201;
        cti6.count = 10;
        cti6.dbm = 110;
        ComputedTowerInfo cti7 = new ComputedTowerInfo();
        cti7.id = 1002;
        cti7.psc = 202;
        cti7.count = 9;
        cti7.dbm = 109;
        ComputedTowerInfo cti8 = new ComputedTowerInfo();
        cti8.id = -1;
        cti8.psc = 203;
        cti8.count = 1;
        cti8.dbm = 98;

        FootPrint footPrintLocation = new FootPrint();
        footPrintLocation.computedTowerInfos.add(cti6);
        footPrintLocation.computedTowerInfos.add(cti7);
        footPrintLocation.computedTowerInfos.add(cti8);

        CellManager cellManager = new CellManager(null);
        LocationMatch locationMatch = cellManager.compareFootprints(footPrintGlobal, footPrintLocation);

        assertEquals(89, locationMatch.currentMatchPercent);
    }

}