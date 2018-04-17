package com.example.unet.rutas;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by root on 04/04/18.
 */

public class AlarmReceiver extends WakefulBroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        Log.e("AlarmReceiver", "onReceive");
        context.startService(new Intent(context, Myservice.class));
    }

}