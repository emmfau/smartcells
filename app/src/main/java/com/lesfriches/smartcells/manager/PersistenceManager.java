package com.lesfriches.smartcells.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lesfriches.smartcells.SmartCellsApplication;
import com.lesfriches.smartcells.model.Location;

import java.util.ArrayList;

/**
 * Save and load locations in SharedPreferences, in JSON format
 */
public class PersistenceManager {

    private SmartCellsApplication app;

    public PersistenceManager(SmartCellsApplication scapp) {
        app = scapp;
    }

    /**
     * Save location as JSON in SharedPreferences
     */
    public void saveLocations() {
        Gson gson = new Gson();
        String locationsJson = gson.toJson(app.locations);
        SharedPreferences sharedPrefs = app.getSharedPreferences("SmartSwitcher", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("locations", locationsJson);
        Log.i("locations", locationsJson);
        editor.commit();
    }

    /**
     * Load location as JSON in SharedPreferences
     */
    public void loadLocations() {
        SharedPreferences sharedPrefs = app.getSharedPreferences("SmartSwitcher", Context.MODE_PRIVATE);
        String locationsJson = sharedPrefs.getString("locations", "[]");
        Gson gson = new Gson();
        app.locations = gson.fromJson(locationsJson, new TypeToken<ArrayList<Location>>() {
        }.getType());
    }

}
