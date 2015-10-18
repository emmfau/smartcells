package com.lesfriches.smartcells.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.lesfriches.smartcells.R;
import com.lesfriches.smartcells.SmartCellsApplication;
import com.lesfriches.smartcells.adapter.LocationAdapter;
import com.lesfriches.smartcells.model.ComputedTowerInfo;
import com.lesfriches.smartcells.model.FootPrint;
import com.lesfriches.smartcells.model.Location;
import com.lesfriches.smartcells.service.SchedulingService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Main screen, with a dashboard to see global current footprint and locations.
 * Connected to Service, to receive events and refresh dashboard
 */
public class DashboardActivity extends AppCompatActivity {

    // Receiver for service events ( when a new measure is collected and the global footprint is updated )
    private BroadcastReceiver updateUIReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_action_action_track_changes);
        actionBar.setDisplayHomeAsUpEnabled(true);

        SmartCellsApplication app = ((SmartCellsApplication) getApplicationContext());

        // Enable cells scan auto-refresh (if not already enabled)
        if (!app.alarmSetted) {
            app.alarmReceiver.setAlarm(app);
        }

        // Load persistent locations
        if (!app.locationsLoaded) {
            app.persistenceManager.loadLocations();
            app.locationsLoaded = true;
        }

        // Create UI elements : draw footprint and locations, register events
        drawCurrentFootprint();
        updateHeaderValues();

        ListView locationListView = (ListView) findViewById(R.id.locationListView);
        app.locationAdapter = new LocationAdapter(this, R.layout.list_item_location, app.locations);
        locationListView.setAdapter(app.locationAdapter);

        locationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DashboardActivity.this, LocationActivity.class);
                Location location = (Location) parent.getItemAtPosition(position);
                intent.putExtra("locationId", location.id);
                startActivity(intent);
            }
        });

        // Register service events : listen to events, for UI update
        IntentFilter filter = new IntentFilter();
        filter.addAction(SchedulingService.class.getCanonicalName());
        updateUIReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                drawCurrentFootprint();
                updateHeaderValues();
                SmartCellsApplication app = ((SmartCellsApplication) getApplicationContext());
                app.locationAdapter.notifyDataSetChanged();
            }
        };
        registerReceiver(updateUIReceiver, filter);
    }


    /**
     * Draw UI current footprint, in a circle-style
     */
    private void drawCurrentFootprint() {
        SmartCellsApplication app = ((SmartCellsApplication) getApplicationContext());
        FootPrint footPrint = app.currentFootprint;
        // 1. Compute sum
        int sumCount = 0;
        for (ComputedTowerInfo cti : footPrint.computedTowerInfos) {
            sumCount += cti.count;
        }
        // 2. compute each percent
        List<Integer> footprintPercents = new ArrayList<Integer>();
        for (ComputedTowerInfo cti : footPrint.computedTowerInfos) {
            float f = (float) cti.count / (float) sumCount;
            int moy = (int) (f * 100);
            footprintPercents.add(moy);
        }

        // https://www.google.com/design/spec/style/color.html
        int[] blueColors = {
                Color.parseColor("#FFF9C4"),
                Color.parseColor("#FFF59D"),
                Color.parseColor("#FFF176"),
                Color.parseColor("#FFEE58"),
                Color.parseColor("#FFEB3B"),
                Color.parseColor("#FDD835"),
                Color.parseColor("#FBC02D"),
                Color.parseColor("#F9A825"),
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"), // Other values are non-significants
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"),
                Color.parseColor("#F57F17"),

        };

        DecoView decoView = (DecoView) findViewById(R.id.dynamicArcView);
        decoView.executeReset();
        decoView.deleteAll();
        SeriesItem back = new SeriesItem.Builder(Color.parseColor("#E2E2E2"))
                .setRange(0, 100, 100)
                .setCapRounded(false)
                .build();
        decoView.addSeries(back);

        for (int i = 0; i < footprintPercents.size(); i++) {
            // Compute sum
            int sum = 0;
            for (int s = i; s < footprintPercents.size(); s++) {
                sum += footprintPercents.get(s);
            }

            SeriesItem s = new SeriesItem.Builder(blueColors[i])
                    .setRange(0, 100, 0)
                    .setCapRounded(true)
                    .build();
            int si = decoView.addSeries(s);
            decoView.addEvent(new DecoEvent.Builder(sum)
                    .setIndex(si)
                    .setDelay(250)
                    .setDuration(500)
                    .build());
        }
    }

    /**
     * Draw UI global current footprint values : measures, cells
     */
    private void updateHeaderValues() {
        SmartCellsApplication app = ((SmartCellsApplication) getApplicationContext());
        TextView tvFootprints = (TextView) findViewById(R.id.tvFootprints);
        TextView tvMeasures = (TextView) findViewById(R.id.tvMeasures);
        tvFootprints.setText("Footprint : " + app.currentFootprint.computedTowerInfos.size());
        tvMeasures.setText("Measures : " + app.measures.size());
    }

    /**
     * When click on button +, add location and redirect to LocationActivity
     *
     * @param v
     */
    public void addLocation(View v) {
        SmartCellsApplication app = ((SmartCellsApplication) getApplicationContext());
        Location location = new Location();
        location.id = UUID.randomUUID().toString();
        app.locationAdapter.add(location);
        Intent i = new Intent(getApplicationContext(), LocationActivity.class);
        i.putExtra("locationId", location.id);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfSt atement
        if (id == R.id.action_settings) {
            Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disableMeasureReception();
        SmartCellsApplication app = ((SmartCellsApplication) getApplicationContext());
        app.persistenceManager.saveLocations();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disableMeasureReception();
    }

    private void disableMeasureReception() {
        try {
            if (updateUIReceiver != null) {
                unregisterReceiver(updateUIReceiver);
            }
        } catch (Exception e) {
            // If receiver already unregistered anywhere else, do nothing
            Log.w(DashboardActivity.class.getName(), "SchedulingService receiver already unregistered elsewhere, ignoring.");
        }

    }

}
