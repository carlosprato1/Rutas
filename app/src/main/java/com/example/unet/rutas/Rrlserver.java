package com.example.unet.rutas;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by root on 18/09/18.
 */

public class Rrlserver extends AppCompatActivity {
    SharedPreferences preferencias;
    String URLServer;
    EditText url;
    Button _texto;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.urlserver);
        url = findViewById(R.id.ip1);
        _texto = findViewById(R.id.boton);
        preferencias = PreferenceManager.getDefaultSharedPreferences(this);

        URLServer = preferencias.getString("ET_URL","cualquiera");
        if (!"cualquiera".equals(URLServer)){
            url.setText(URLServer);
        }

        _texto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actualizar(v);
            }
        });


    }

    public void actualizar(View v) {
        URLServer = url.getText().toString();
        if (!validate()) {
            Toast.makeText(getBaseContext(), "Por Favor Introduzca una URL Valida", Toast.LENGTH_LONG).show();
            return;
        }

        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString("ET_URL",URLServer );

        editor.apply();
        Toast.makeText(getBaseContext(), "URL actualizada", Toast.LENGTH_LONG).show();



        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(intent, 0);
    }
    public boolean validate() {
        boolean valid = true;

        if (URLServer.isEmpty() || URLServer.length() < 10 ) {
            valid = false;
        }

        return valid;
    }

}
