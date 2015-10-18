package com.lesfriches.smartcells.model;

/**
 * Technical unit information of one tower info signal
 */
public class TowerInfo {

    // Constants
    public final static int CDMA = 1;
    public final static int GSM = 2;
    public final static int LTE = 3;
    public final static int WCDMA = 4;

    public final static int BAD_ID_MIN = -1;
    public final static int BAD_ID_MAX = 2147483647;

    public int type;
    public int id = -1;
    public int psc = -1;
    public int dbm;

}
