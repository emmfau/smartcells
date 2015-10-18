package com.lesfriches.smartcells.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PowerManager;

import com.lesfriches.smartcells.SmartCellsApplication;
import com.lesfriches.smartcells.service.SchedulingService;

/**
 * Receiver for alarm events : launched at each interval to start SchedulingService
 */
public class AlarmReceiver extends BroadcastReceiver {

    private AlarmManager alarmMgr;
    private PendingIntent alarmPendingIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        //Acquire the lock
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, AlarmReceiver.class.getName());
        wl.acquire();

        context.startService(new Intent(context, SchedulingService.class));

        // Do the repeat alarm manually (since android does not give setExactRepeating())
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intentAlarm = new Intent(context, AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 0, intentAlarm, 0);
        // TODO externalize param the delay of the alarm
        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, alarmPendingIntent);
        //Release the lock
        wl.release();
    }

    public void setAlarm(Context context) {
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 10000, alarmPendingIntent);
        //alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10000, alarmPendingIntent);
        // Enable {@code BootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        SmartCellsApplication app = ((SmartCellsApplication) context.getApplicationContext());
        app.alarmSetted = true;
    }

    public void cancelAlarm(Context context) {
        // If the alarm has been set, cancel it.
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmPendingIntent);
        }
        // Disable {@code BootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        SmartCellsApplication app = ((SmartCellsApplication) context.getApplicationContext());
        app.alarmSetted = false;
    }

}