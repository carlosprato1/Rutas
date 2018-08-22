package com.example.unet.rutas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.preference.PreferenceManager;


public class PowerReceiver extends BroadcastReceiver {
    SharedPreferences preferencias;
    @Override
    public void onReceive(Context context, Intent intent) {

        preferencias = PreferenceManager.getDefaultSharedPreferences(context);

        Intent intent1 = context.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = intent1.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        Integer cargando = 0;
       if (status == BatteryManager.BATTERY_PLUGGED_AC){
           cargando = 1;
       }
       if (status == BatteryManager.BATTERY_PLUGGED_USB){
           cargando = 1;
       }
       if (status == BatteryManager.BATTERY_PLUGGED_WIRELESS){
           cargando = 1;
       }

        SharedPreferences.Editor editor = preferencias.edit();
        Boolean estadoServicio = preferencias.getBoolean("estadoServicio",false);

        if (estadoServicio){
            editor.putInt("ACC", cargando);
            editor.apply();
        }

    }


}





