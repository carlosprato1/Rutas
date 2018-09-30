package com.example.unet.rutas;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

/**
 * Created by root on 22/09/18.
 */

public class formularioActivity extends AppCompatActivity
{
    SharedPreferences preferencias;
    int bandera = 0; //libre
    String eva_resp;
    String jsonPro_alum;
    String eva_email;
    String eva_par1;
    String eva_par2 ;
    String eva_par3;
    String eva_pregun;
    String autor;
    String auditorium;


    @BindView(R.id.output_nombre) TextView _nombre;
    @BindView(R.id.output_correo) TextView _correo;
    @BindView(R.id.pregunta) EditText _pregunta;
    @BindView(R.id.respuesta) EditText _respuesta;
    @BindView(R.id.input_parcial1) EditText _parcial1;
    @BindView(R.id.input_parcial2) EditText _parcial2;
    @BindView(R.id.input_parcial3) EditText _parcial3;
    @BindView(R.id.output_definitiva) TextView _definitiva;
    @BindView(R.id.btn_aceptar) Button button;
    @BindView(R.id.btn_cancelar) Button button_cancelar;

    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent(getApplicationContext(), MapsActivity2.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.formulario_activity);
        ButterKnife.bind(this);
        preferencias = PreferenceManager.getDefaultSharedPreferences(this);

        autor = preferencias.getString("Server_autores","Evaluador");

        String eva_name = getIntent().getStringExtra("eva_name");
        String eva_adress = getIntent().getStringExtra("eva_address");

        if ("Evaluador".equals(autor)){
         auditorium =  preferencias.getString("Server_user_email", "Vacio");
         eva_email = getIntent().getStringExtra("eva_email");
         eva_par1 = getIntent().getStringExtra("eva_par1");
         eva_par2 = getIntent().getStringExtra("eva_par2");
         eva_par3 = getIntent().getStringExtra("eva_par3");
         eva_pregun = getIntent().getStringExtra("eva_pregun");
         eva_resp = getIntent().getStringExtra("eva_resp");

         _respuesta.setEnabled(false);
            if ("".equals(eva_resp)){
                _respuesta.setText("No disponible");
                bandera  = 1;
            }else{
                _respuesta.setText(eva_resp);
                 button.setText("Evaluar");

            }

        }else{
            auditorium = getIntent().getStringExtra("eva_email");
            eva_email = preferencias.getString("Server_user_email","");
            eva_par1 = preferencias.getString("Server_parcial1","");
            eva_par2 = preferencias.getString("Server_parcial2","");
            eva_par3 = preferencias.getString("Server_parcial3","");
            eva_pregun = preferencias.getString("Server_auditorium_pregunta","");
            eva_resp = preferencias.getString("Server_auditorium_respuesta","");
            _pregunta.setEnabled(false);
            _parcial1.setEnabled(false);
            _parcial2.setEnabled(false);
            _parcial3.setEnabled(false);

            _respuesta.setText(eva_resp);
            button.setText("Responder");
        }

        _nombre.setText(eva_name);
        _correo.setText(eva_adress);
        _parcial1.setText(eva_par1);
        _parcial2.setText(eva_par2);
        _parcial3.setText(eva_par3);
        _pregunta.setText(eva_pregun);



        _parcial3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ponernotas();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        _parcial2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ponernotas();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        _parcial1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ponernotas();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        button_cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity2.class);
                startActivity(intent);
                finish();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!validate()) {
                    return;
                }
                String nota1 = _parcial1.getText().toString();
                String nota2 = _parcial2.getText().toString();
                String nota3 = _parcial3.getText().toString();
                String pregunta = _pregunta.getText().toString();
                String respuesta = _respuesta.getText().toString();



                if ("No disponible".equals(respuesta)){
                    respuesta = "";
                }
                if  (!"No disponible".equals(respuesta) && "Evaluador".equals(autor)){
                    bandera = 0;
                   // Toast.makeText(getBaseContext(), "Alumno disponible para realizar otra pregunta", Toast.LENGTH_SHORT).show();
                }

                Float nota1float =Float.parseFloat(nota1);
                Float nota2float =Float.parseFloat(nota2);
                Float nota3float =Float.parseFloat(nota3);


                try {

                    JSONObject track10 = new JSONObject();
                    track10.put("auditorium", auditorium);
                    track10.put("auditorium_pregunta",pregunta);
                    track10.put("bandera", bandera);
                    track10.put("nota1", nota1);
                    track10.put("nota2", nota2);
                    track10.put("nota3", nota3);
                    track10.put("user_email", eva_email);
                    track10.put("latitud", Float.toString(preferencias.getFloat("Lat",0.0F)));
                    track10.put("longitud", Float.toString(preferencias.getFloat("Lon",0.0F)));
                    track10.put("auditorium_respuesta", respuesta);

                    jsonPro_alum = track10.toString();
                    Log.e("formularioActivity", "jsonPro_alum" +jsonPro_alum);
                } catch (JSONException e) {}

               String urlserver = preferencias.getString("ET_URL", "cualquiera");
               new guardar_servidor().execute(urlserver);

            }
        });
        ponernotas();


    }//oncreate

    public boolean validate() {
        boolean valid = true;

        String pregunta = _pregunta.getText().toString();
        String respuesta = _respuesta.getText().toString();

        if (pregunta.isEmpty()) {
            _pregunta.setError("Realiza una pregunta");
            valid = false;
        } else {
            _pregunta.setError(null);
        }

        if (respuesta.isEmpty()) {
            _respuesta.setError("Responda la Pregunta");
            valid = false;
        } else {
            _respuesta.setError(null);
        }

        return valid;
    }


    public class guardar_servidor extends AsyncTask<String, Boolean, String> {

        public guardar_servidor() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... datos) {
            String URLServer = datos[0];

            try {
                URL e = new URL(URLServer + "/controlador/llenar_Alumno1.php");
                HttpURLConnection urlConnection = (HttpURLConnection) e.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                OutputStreamWriter streamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                streamWriter.write(jsonPro_alum);
                streamWriter.flush();
                //----Leer el response de la peticion POST
                InputStream in1 = urlConnection.getInputStream();
                InputStreamReader isw1 = new InputStreamReader(in1);

                StringBuilder cadena = new StringBuilder();
                for (int data = isw1.read(); data != -1; data = isw1.read()) {
                    cadena.append((char) data);
                }
                String current = cadena.toString().replaceAll("\n", "");
                urlConnection.disconnect();

                return current;
            } catch (IOException var8) {
                Log.e("formularioActivity", "error server: " + var8);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String respuesta) {

            Log.e("formulario(servidor)", respuesta);

            if (respuesta != null){
                if ("aceptado".equals(respuesta)){
                    Toast.makeText(getBaseContext(), "Actualizado", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
                Toast.makeText(getBaseContext(), "Error, revice conexion al servidor", Toast.LENGTH_LONG).show();


        }

    }//tareaasincrona2

    private void ponernotas(){
        String nota1 = _parcial1.getText().toString();
        String nota2 = _parcial2.getText().toString();
        String nota3 = _parcial3.getText().toString();
        if("".equals(nota1)) {nota1 = "0";}
        if("".equals(nota2)) {nota2 = "0";}
        if("".equals(nota3)) {nota3 = "0";}
        if(nota1.contains("-")){nota1 = "0";Toast.makeText(getBaseContext(), "Caracter (-) No Permitido" , Toast.LENGTH_SHORT).show();}
        if(nota2.contains("-")){nota2 = "0";Toast.makeText(getBaseContext(), "Caracter (-) No Permitido" , Toast.LENGTH_SHORT).show();}
        if(nota3.contains("-")){nota3 = "0";Toast.makeText(getBaseContext(), "Caracter (-) No Permitido" , Toast.LENGTH_SHORT).show();}

        Float nota1float =Float.parseFloat(nota1);
        Float nota2float =Float.parseFloat(nota2);
        Float nota3float =Float.parseFloat(nota3);

        Float definitiva = nota1float+nota2float+nota3float;
        _definitiva.setText(String.valueOf(definitiva));
    }

}
