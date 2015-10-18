package com.lesfriches.smartcells.model;

/**
 * A tower info from cells, extended with analysis values for comparison
 */
public class ComputedTowerInfo extends TowerInfo {
    // Variables for footprint
    public int dbmSum;
    public int dbmCount;
    public int averageDbm;
    public int count;

    // For computing, but transient = not saved
    public transient float averagePercent = -1;
    public transient float differencePercent = -1; // Difference between two CTI averagePercent

    @Override
    public String toString() {
        //averageDbm=(new Double(dbmSum)).doubleValue()/(new Double(dbmCount)).doubleValue();
        if (dbmCount != 0) {
            averageDbm = dbmSum / dbmCount;
        } else {
            averageDbm = dbmSum;
        }
        //return " |-- ["+id+","+psc+","+type+"]  ["+count+","+averageDbm+"]  (avrg="+averagePercent+"%, diff="+differencePercent+"%)\r\n";

        String returned = "[" + id + "," + psc + "]=[" + count + "]";
        if (differencePercent != -1) {
            returned += " dif" + ((int) differencePercent) + "% ";
        }
        if (averagePercent != -1) {
            returned += " avg" + ((int) averagePercent) + "% ";
        }
        returned += "\n";

        return returned;
    }

    @Override
    public boolean equals(Object o) {
        ComputedTowerInfo comparedCti = (ComputedTowerInfo) o;
        boolean isRealId = (this.id != TowerInfo.BAD_ID_MAX && this.id != TowerInfo.BAD_ID_MIN);
        boolean isComparedRealId = (comparedCti.id != TowerInfo.BAD_ID_MAX && comparedCti.id != TowerInfo.BAD_ID_MIN);
        boolean isRealPsc = (this.psc != -1);
        boolean isComparedRealPsc = (comparedCti.psc != -1);

        if (isRealId && isComparedRealId) { // if real ids
            return (this.id == comparedCti.id);
        } else {
            if (isRealPsc && isComparedRealPsc) { // if real psc
                return (this.psc == comparedCti.psc);
            } else {
                return false;
            }
        }
    }

    /**
     * Manual clone method
     * @return
     */
    public ComputedTowerInfo clone() {
        ComputedTowerInfo newCti = new ComputedTowerInfo();

        newCti.type = type;
        newCti.id = id;
        newCti.psc = psc;
        newCti.dbm = dbm;

        newCti.dbmSum = dbmSum;
        newCti.dbmCount = dbmCount;
        newCti.averageDbm = averageDbm;
        newCti.count = count;

        newCti.averagePercent = averagePercent;
        newCti.differencePercent = differencePercent;

        return newCti;
    }

}
