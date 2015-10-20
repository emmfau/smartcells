package com.lesfriches.smartcells;

import android.app.Application;

import com.lesfriches.smartcells.adapter.LocationAdapter;
import com.lesfriches.smartcells.manager.ActionManager;
import com.lesfriches.smartcells.manager.CellManager;
import com.lesfriches.smartcells.manager.PersistenceManager;
import com.lesfriches.smartcells.model.FootPrint;
import com.lesfriches.smartcells.model.Location;
import com.lesfriches.smartcells.model.Measure;
import com.lesfriches.smartcells.receiver.AlarmReceiver;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Main application class : used for globals variables and managers across all kind of controllers ( Activities, Receivers,
 * Services, ..). Provides a better lifetime guarantee (=the lifetime of the application), intead of any other external singleton
 * that is not explicitly tied to the lifetime of the application
 */
public class SmartCellsApplication extends Application {

    // Global functionnal variables
    public LinkedList<Measure> measures = new LinkedList<Measure>();
    public FootPrint currentFootprint = new FootPrint();
    public ArrayList<Location> locations = new ArrayList<Location>();
    public boolean locationsLoaded = false;

    // Global technical variables
    public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd,HH:mm:ss:SSS");
    public AlarmReceiver alarmReceiver = new AlarmReceiver();
    public boolean alarmSetted;
    public LocationAdapter locationAdapter;

    // Global managers
    public CellManager cellManager = new CellManager(this);
    public PersistenceManager persistenceManager = new PersistenceManager(this);
    public ActionManager actionManager = new ActionManager(this);

}
