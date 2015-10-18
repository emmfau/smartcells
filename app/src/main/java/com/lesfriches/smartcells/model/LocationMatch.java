package com.lesfriches.smartcells.model;

/**
 * A location match, returned by the analysis and comparison engine
 */
public class LocationMatch {

    public transient int currentMatchPercent = -1;
    public transient int currentCommonLocationsPercent = -1;
    public transient FootPrint commonFootPrint = new FootPrint();

}
