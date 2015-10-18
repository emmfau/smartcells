package com.lesfriches.smartcells.model;

import java.util.ArrayList;

/**
 * Instant measure, dated, and with all tower infos.
 */
public class Measure {

    public long timestamp;

    public ArrayList<TowerInfo> towerInfos = new ArrayList<TowerInfo>();
}
