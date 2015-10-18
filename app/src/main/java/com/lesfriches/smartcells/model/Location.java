package com.lesfriches.smartcells.model;

/**
 * A location, with its technical footprint, and actions associated
 */
public class Location {

    public String id;
    public String name;
    public boolean enableWifi;
    public boolean enableBluetooth;
    public int volumeLevel;
    public FootPrint footprint;
    public boolean isLearning;
    public long timestampStartLearning = -1;
    public long timestampEndLearning = -1;

    // For computing, but transient = not saved
    public transient LocationMatch locationMatch = new LocationMatch();

    @Override
    public String toString() {
        if (footprint != null) {
            return footprint.toString();
        } else {
            return "";
        }
    }
}
