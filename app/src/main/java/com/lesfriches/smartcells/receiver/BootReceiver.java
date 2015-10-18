package com.lesfriches.smartcells.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lesfriches.smartcells.SmartCellsApplication;

/**
 * When phone boots, starts engine by setting the first alarm
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            SmartCellsApplication app = ((SmartCellsApplication) context.getApplicationContext());
            if (!app.alarmSetted) {
                app.alarmReceiver.setAlarm(context);
            }
        }
    }
}
