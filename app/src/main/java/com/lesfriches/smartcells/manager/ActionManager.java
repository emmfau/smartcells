package com.lesfriches.smartcells.manager;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiManager;

import com.lesfriches.smartcells.SmartCellsApplication;

/**
 * Created by emmfau on 20/10/15.
 */
public class ActionManager {

    private SmartCellsApplication app;

    public ActionManager(SmartCellsApplication scapp) {
        app = scapp;
    }

    public void setWifi(boolean enabled) {
        final WifiManager wifi = (WifiManager) app.getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(enabled); // true or false to activate/deactivate wifi
    }

    public void setBluetooth(boolean enabled) {
        if (enabled) {
            BluetoothAdapter.getDefaultAdapter().enable();
        }
        else {
            BluetoothAdapter.getDefaultAdapter().disable();
        }
    }

    public void setVolume(int v) {
        AudioManager audio = (AudioManager) app.getSystemService(Context.AUDIO_SERVICE);
        audio.setStreamVolume(AudioManager.STREAM_RING, v, 0);
    }
}
