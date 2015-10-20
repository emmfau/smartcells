package com.lesfriches.smartcells.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.MatrixCursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.Toast;

import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.lesfriches.smartcells.R;
import com.lesfriches.smartcells.SmartCellsApplication;
import com.lesfriches.smartcells.model.ComputedTowerInfo;
import com.lesfriches.smartcells.model.FootPrint;
import com.lesfriches.smartcells.model.Location;
import com.lesfriches.smartcells.model.Measure;
import com.lesfriches.smartcells.service.SchedulingService;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Screen for location details, and actions for this locations.
 * Used for a new location, and to edit an existing location.
 */
public class LocationActivity extends AppCompatActivity {

    private Location currentLocation;

    // Receiver for service events ( when a new measure is collected and the global footprint is updated )
    private BroadcastReceiver updateUIReceiver;

    Handler mHandler;
    int handlerCount=0;

    LinkedList<Measure> measures = new LinkedList<Measure>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String locationId = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            locationId = extras.getString("locationId");
        }

        // Get location
        SmartCellsApplication app = ((SmartCellsApplication) getApplicationContext());
        for (Location l : app.locations) {
            if (l.id.equals(locationId)) {
                currentLocation = l;
            }
        }

        // Load location values
        if (currentLocation != null) {
            EditText etLocationName = (EditText) findViewById(R.id.locationName);
            etLocationName.setText(currentLocation.name);
        }

        mHandler = new Handler();
        if (currentLocation.isLearning) {
            Button btLearnLocation=(Button)findViewById(R.id.btLearnLocation);
            btLearnLocation.setText("STOP (" + (60-handlerCount) +")");
        }

        if (currentLocation.footprint != null) {
            drawLocationFootprint();
        }

        if (currentLocation.enableBluetooth) {
            showBluetoothEnabled();
        } else {
            showBluetoothDisabled();
        }

        if (currentLocation.enableWifi) {
            showWifiEnabled();
        } else {
            showWifiDisabled();
        }

        SeekBar sbVolumeLevel = (SeekBar) findViewById(R.id.volumeLevel);
        sbVolumeLevel.setProgress(currentLocation.volumeLevel);
        sbVolumeLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentLocation.volumeLevel = seekBar.getProgress();
            }
        });

        showAnalysis();
    }


    private void showAnalysis() {
        SmartCellsApplication app = ((SmartCellsApplication) getApplicationContext());
        // Column definition : SimpleCursorAdapter needs ID named "_id"
        String[] columns = new String[]{"_id", "col1", "col2"};

        MatrixCursor matrixCursor = new MatrixCursor(columns);
        startManagingCursor(matrixCursor);
        matrixCursor.addRow(new Object[]{1, "Global footprint\n[id,psc]=[count,avg]", "Location footprint\n[id,psc]=[count,avg]"});
        matrixCursor.addRow(new Object[]{2, app.currentFootprint.toString(), currentLocation.toString()});
        matrixCursor.addRow(new Object[]{3, "Common footprint", "Result"});
        matrixCursor.addRow(new Object[]{4, currentLocation.locationMatch.commonFootPrint.toString(), "C-" + currentLocation.locationMatch.currentCommonLocationsPercent + "% M-" + currentLocation.locationMatch.currentMatchPercent + "%"});

        String[] from = new String[]{"col1", "col2"};
        int[] to = new int[]{R.id.textViewCol1, R.id.textViewCol2};
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.table_item_footprint, matrixCursor, from, to, 0);
        ListView lv = (ListView) findViewById(R.id.lv);
        lv.setAdapter(adapter);
    }

    private void showBluetoothEnabled() {
        Switch sw = (Switch) findViewById(R.id.switchBluetooth);
        sw.setChecked(true);
    }

    private void showBluetoothDisabled() {
        Switch sw = (Switch) findViewById(R.id.switchBluetooth);
        sw.setChecked(false);
    }

    public void onSwitchBluetoothClicked(View v) {
        if (currentLocation.enableBluetooth) {
            currentLocation.enableBluetooth = false;
            showBluetoothDisabled();
        } else {
            currentLocation.enableBluetooth = true;
            showBluetoothEnabled();
        }
    }

    private void showWifiEnabled() {
        Switch sw = (Switch) findViewById(R.id.switchWifi);
        sw.setChecked(true);
    }

    private void showWifiDisabled() {
        Switch sw = (Switch) findViewById(R.id.switchWifi);
        sw.setChecked(false);
    }

    public void onSwitchWifiClicked(View v) {
        if (currentLocation.enableWifi) {
            currentLocation.enableWifi = false;
            showWifiDisabled();
        } else {
            currentLocation.enableWifi = true;
            showWifiEnabled();
        }
    }


    public void onButtonResetClicked(View v) {
        currentLocation.timestampStartLearning = -1;
        currentLocation.timestampEndLearning = -1;
        currentLocation.isLearning = false;
        currentLocation.footprint = null;
        DecoView decoView = (DecoView) findViewById(R.id.locationArcView);
        decoView.executeReset();
        decoView.deleteAll();
    }


    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentLocation.isLearning) {
                handlerCount++;
                if (handlerCount > 0 && handlerCount < 60) {
                    Log.i("LocationActivity", "Create measure " + handlerCount);
                    Button btLearnLocation = (Button) findViewById(R.id.btLearnLocation);
                    btLearnLocation.setText("STOP (" + (60 - handlerCount) + ")");
                    SmartCellsApplication app = ((SmartCellsApplication) getApplicationContext());
                    measures.add(app.cellManager.createMeasure());
                    mHandler.postDelayed(mRunnable, 1000);
                } else if (handlerCount >= 60) {
                    Log.i("LocationActivity", "Create location footprint");
                    Button btLearnLocation = (Button) findViewById(R.id.btLearnLocation);
                    btLearnLocation.setText("LEARN");
                    SmartCellsApplication app = ((SmartCellsApplication) getApplicationContext());
                    currentLocation.footprint = app.cellManager.createFootPrint(measures);
                    Toast.makeText(app, "Footprint created !", Toast.LENGTH_SHORT).show();
                    currentLocation.isLearning = false;
                    drawLocationFootprint();
                    showAnalysis();
                }
            }
        }
    };

    public void onButtonLearnClicked(View v) {
        // if entering learn mode
        if (!currentLocation.isLearning) {
            currentLocation.isLearning = true;
            Toast.makeText(this, "Learn location for 60 seconds", Toast.LENGTH_SHORT).show();
            handlerCount = 0;
            measures = new LinkedList<Measure>();

            mHandler.postDelayed(mRunnable, 1000);
        }
        // if exiting learn mode
        else {
            currentLocation.isLearning = false;
            handlerCount = 0;
            measures = new LinkedList<Measure>();
            Button btLearnLocation=(Button)findViewById(R.id.btLearnLocation);
            btLearnLocation.setText("LEARN");
        }

    }

    public void onButtonRemoveClicked(View v) {
        SmartCellsApplication app = ((SmartCellsApplication) getApplicationContext());
        app.locationAdapter.remove(currentLocation);
        app.locationAdapter.notifyDataSetChanged();
        saveLocations();
        finish();
    }

    public void onButtonDetailsClicked(View v) {
        ListView lv=(ListView)findViewById(R.id.lv);
        if (lv.getVisibility()==View.VISIBLE) {
            lv.setVisibility(View.INVISIBLE);
        }
        else {
            lv.setVisibility(View.VISIBLE);
        }
    }

    private void drawLocationFootprint() {
        FootPrint footPrint = currentLocation.footprint;
        if (footPrint == null) {
            return; // no footprint, no draw
        }
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

        DecoView decoView = (DecoView) findViewById(R.id.locationArcView);
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

    @Override
    protected void onStop() {
        super.onStop();
        saveLocations();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //disableMeasureReception();
        saveLocations();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //disableMeasureReception();
        saveLocations();
    }

    private void saveLocations() {
        // Save location values
        SmartCellsApplication app = ((SmartCellsApplication) getApplicationContext());
        EditText etLocationName = (EditText) findViewById(R.id.locationName);
        currentLocation.name = etLocationName.getText().toString();
        Switch swW = (Switch) findViewById(R.id.switchWifi);
        currentLocation.enableWifi = swW.isChecked();
        Switch swB = (Switch) findViewById(R.id.switchBluetooth);
        currentLocation.enableBluetooth = swB.isChecked();
        SeekBar sbVolumeLevel = (SeekBar) findViewById(R.id.volumeLevel);
        currentLocation.volumeLevel = sbVolumeLevel.getProgress();
        app.locationAdapter.notifyDataSetChanged();
    }

}
