package com.lesfriches.smartcells.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.lesfriches.smartcells.SmartCellsApplication;
import com.lesfriches.smartcells.model.Location;

/**
 * Background service launched at intervals (by AlarmReceiver) to get a current measure,
 * maintain the current global footprint, and analyse each locations footprint if there's
 * a location match, then run actions for this location.
 */
public class SchedulingService extends IntentService {


    public SchedulingService() {
        super("SchedulingService");
    }

    @Override
    public void onCreate() {
        Log.i("SchedulingService", "onCreate event.");
        super.onCreate();
        SmartCellsApplication app = ((SmartCellsApplication) getApplicationContext());
        if (!app.locationsLoaded) {
            app.persistenceManager.loadLocations();
            app.locationsLoaded = true;
        }

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Log.i("SchedulingService", "onHandleIntent event : createMeasure");
            SmartCellsApplication app = ((SmartCellsApplication) getApplicationContext());
            app.measures.add(app.cellManager.createMeasure());

            // Check if too much measures
            // TODO : externalize, and handle a period instead of a number of measures.
            if (app.measures.size() > 30) {
                app.measures.removeFirst();
            }

            // Create currentGlobalFootprint
            app.currentFootprint = app.cellManager.createFootPrint(app.measures);

            // check currentGlobalFootprint with all others footprints
            Log.i("SchedulingService", "Comparing all footprints");
            for (Location l : app.locations) {
                l.locationMatch = app.cellManager.compareFootprints(app.currentFootprint, l.footprint);
                Log.i("SchedulingService", "Compare current to " + l.name + " : " + l.locationMatch.currentMatchPercent + "% common");
                // TODO : check percent, and launch actions if needed :)
            }

            // Update UI (if UI is launched)
            Intent local = new Intent();
            local.setAction(SchedulingService.class.getCanonicalName());
            this.sendBroadcast(local);
        } catch (Exception e) {
            Log.e(SchedulingService.class.getName(), "Exception occured in SchedulingService : " + e.getLocalizedMessage());

        }

    }


}
