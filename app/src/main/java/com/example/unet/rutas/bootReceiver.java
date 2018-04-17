package com.example.unet.rutas;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by root on 04/04/18.
 */

public class bootReceiver extends BroadcastReceiver {

    private static final String TAG = "bootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent gpsTrackerIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);

        SharedPreferences preferencias= PreferenceManager.getDefaultSharedPreferences(context);
        int Periodo = Integer.parseInt(preferencias.getString("LP_periodo","1"))*10000;//min->ms 60000
        boolean estadoServicio = preferencias.getBoolean("estadoServicio",false);

        if (estadoServicio) {
            if (Periodo <= 120000){//Runneable
                context.startService(new Intent(context, Myservice.class));
            }else {//alarmManager
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime(),
                        Periodo,
                        pendingIntent);
            }
        } else{
            alarmManager.cancel(pendingIntent);
        }

    }
}