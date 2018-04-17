package com.example.unet.rutas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;


public class preferencias extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static Integer CONVERSION_MIN_MS = 25000; // min->ms

    private int LP_periodoAux;
    SharedPreferences preferencias;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencias);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        preferencias = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.e("preferencias", "onresume");
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString("LP_periodoAux",preferencias.getString("LP_periodo","1"));
        editor.apply();

    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences preferenciaschange, String key) {
        SharedPreferences.Editor editor = preferenciaschange.edit();

        //primera ves no completo
        if ("vacio".equals(preferenciaschange.getString("ET_URL","vacio")) || "vacio".equals(preferenciaschange.getString("ET_NIU","vacio")) || "vacio".equals(preferencias.getString("ET_Unidad","vacio"))){
               editor.putString("estadoConfig","Por Completar");
        }  else {
            if("ET_URL".equals(key) || "ET_NIU".equals(key) || "ET_Unidad".equals(key)) {
                editor.putString("estadoConfig", "Por Verificar");
                editor.putString("estadoDATAJSON","Primera");//borrarjson MENSAJE
            }
        }

        if ("LP_periodo".equals(key) && preferenciaschange.getBoolean("estadoServicio",true)){
           int LP_periodo   = Integer.parseInt(preferencias.getString("LP_periodo","1"));
            LP_periodoAux = Integer.parseInt(preferencias.getString("LP_periodoAux","1"));
            if (LP_periodo < LP_periodoAux){//AlarmManager->runnable
                MainActivity main = new MainActivity();
                Context context = getApplicationContext();
                main.cancelAlarmManager(context);
                startService(new Intent(this, Myservice.class));
                finish();
            }

            if (LP_periodo > LP_periodoAux){//Runnable->AlarmManager
                    Log.e("preferencias", "condicion...");
                    MainActivity main = new MainActivity();
                    Context context = getApplicationContext();
                    main.startAlarmManager(context,LP_periodo*CONVERSION_MIN_MS);
                finish();
            }

        }

        editor.apply();

    }

}
