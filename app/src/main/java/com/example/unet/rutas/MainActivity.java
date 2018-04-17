package com.example.unet.rutas;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
     SharedPreferences preferencias;
     Switch switche;
     int periodo;
     ////////////CONSTANTES CONFIGURAR///////////////////////
     private static Integer LIMITE_ENTRE_RUNNABLE_Y_ALARMMANAGER = 50000; // ms
     private static Integer CONVERSION_MIN_MS = 25000; // min->ms
     ////////////////////////////////////////////////////////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,preferencias.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogoDescription("Rutas");
        preferencias= PreferenceManager.getDefaultSharedPreferences(this);

        //intanciar llamada del servicio a Actualizar IU
        IntentFilter filter = new IntentFilter();
        filter.addAction("test.UPDATE");
        BroadcastReceiver receiver = new llamadaDelServicio();
        registerReceiver(receiver, filter);

        boolean sinID = preferencias.getBoolean("sinID",true);
        if (sinID){
            SharedPreferences.Editor editor = preferencias.edit();
            editor.putBoolean("sinID",false);
            editor.putString("ID", UUID.randomUUID().toString());
            editor.apply();
        }

        switche = findViewById(R.id.SW_Servicio);
        switche.setChecked(preferencias.getBoolean("estadoServicio",false));
        switche.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                switche ();
            }
        });

    }
    @Override
    protected void onResume() {
       super.onResume();
        refrescarIU();
    }

    @SuppressLint("ResourceAsColor")
    public void refrescarIU(){
        Log.e(TAG, "refrescarIU");
        TextView TV_URL = findViewById(R.id.TV_URL);
        TextView TV_NIU = findViewById(R.id.TV_NIU);
        TextView TV_Periodo = findViewById(R.id.TV_Periodo);
        TextView TV_Unidad = findViewById(R.id.TV_Unidad);
        TextView TV_EstConfi = findViewById(R.id.TV_estadoConfi);


        String ET_URL = preferencias.getString("ET_URL","vacio");
        String ET_NIU = preferencias.getString("ET_NIU","vacio");
        String LP_periodo = preferencias.getString("LP_periodo","1");
        String ET_Unidad = preferencias.getString("ET_Unidad","vacio");
        periodo =  Integer.parseInt(LP_periodo)*CONVERSION_MIN_MS;

        TV_URL.setText(ET_URL);
        TV_NIU.setText(ET_NIU);
        TV_Periodo.setText(LP_periodo);
        TV_Unidad.setText(ET_Unidad);

        String estadoConfig = preferencias.getString("estadoConfig","Por Completar");
        switche.setChecked(preferencias.getBoolean("estadoServicio",false));
        if ("Por Verificar".equals(estadoConfig)){
            TV_EstConfi.setText(estadoConfig);
            TV_EstConfi.setTextColor(Color.parseColor("#1E71A7"));
        }
        if ("Por Completar".equals(estadoConfig)){
            TV_EstConfi.setText(estadoConfig);
            TV_EstConfi.setTextColor(Color.parseColor("#FF0000"));
        }
        if ("Incorrecto".equals(estadoConfig)){
            TV_EstConfi.setText(estadoConfig);
            TV_EstConfi.setTextColor(Color.parseColor("#FF0000"));
        }
        if ("Correcto".equals(estadoConfig)){
            TV_EstConfi.setText(estadoConfig);
            TV_EstConfi.setTextColor(Color.parseColor("#56D71E"));
        }

    }
    public void switche (){
        SharedPreferences.Editor editor = preferencias.edit();
        if(switche.isChecked()){
           if(verificarGoogleService() && !"Por Completar".equals(preferencias.getString("estadoConfig","Por Completar")) && !"Incorrecto".equals(preferencias.getString("estadoConfig","Por Completar"))){
               editor.putBoolean("estadoServicio", true);
               if(periodo > LIMITE_ENTRE_RUNNABLE_Y_ALARMMANAGER) {
                   Context context = getApplicationContext();
                   startAlarmManager(context,periodo);
               }else{
                   startService(new Intent(this, Myservice.class));
             }

           }else{switche.setChecked(false);}
        }else{
            editor.putBoolean("estadoServicio",false);
           // stopService(new Intent(this,Myservice.class));//OnDestroy
            if(periodo > LIMITE_ENTRE_RUNNABLE_Y_ALARMMANAGER){
                Context context = getApplicationContext();
                cancelAlarmManager(context);
            }
        }
        editor.apply();
    }
    private Boolean verificarGoogleService(){
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != 0) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, 9000).show();
            }
            Toast.makeText(this.getApplicationContext(), "Necesita Actualizar GooglePlay", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    public class llamadaDelServicio extends BroadcastReceiver {
        public llamadaDelServicio() {
            super();
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            refrescarIU();
        }//Onreceiver
    }

    public void startAlarmManager(Context context,int periodo) {
        Log.e(TAG, "startAlarmManager");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent activityIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, activityIntent, 0);
        assert alarmManager != null;
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), periodo, pendingIntent);
    }

    public void cancelAlarmManager(Context context) {
        Log.e(TAG, "cancelAlarmManager");
        Intent gpsTrackerIntent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, gpsTrackerIntent, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);
    }


}