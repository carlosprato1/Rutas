package com.example.unet.rutas;


        import android.app.ProgressDialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.SharedPreferences;
        import android.location.LocationManager;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.preference.PreferenceManager;
        import android.provider.Settings;
        import android.support.v7.app.AlertDialog;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;

        import android.content.Intent;
        import android.view.View;
        import android.widget.Button;
        import android.widget.EditText;
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
        import java.util.ArrayList;

        import butterknife.ButterKnife;
        import butterknife.BindView;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    ProgressDialog progressDialog;
    SharedPreferences preferencias;
    String URLServer;
    String email;
    String password;


    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.link_signup) TextView _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        preferencias = PreferenceManager.getDefaultSharedPreferences(this);
        URLServer = preferencias.getString("ET_URL","cualquiera");


        LocationManager GPSStatus = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert GPSStatus != null;
        if (!GPSStatus.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Por favor Habilite el GPS")
                    .setCancelable(false)
                    .setPositiveButton("Habilitar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        startService(new Intent(this, Myservice.class).putExtra("id","registro")); //actualizar ubicacion para registro

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Start the Signup activity
               Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
               startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    public void login() {
        Log.e(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }
        if ("cualquiera".equals(URLServer)){
            Toast.makeText(getBaseContext(), "Introduzca la Ip del Servidor", Toast.LENGTH_LONG).show();
            return;
        }

        _loginButton.setEnabled(false);

        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();

        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Myservice.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            ArmarJSONlogin();
            new TareaAsincrona1().execute(URLServer,preferencias.getString("jlogin",""));
        } else {
            Toast.makeText(getBaseContext(), "No hay conexion a la red", Toast.LENGTH_LONG).show();

        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {


                // By default we just finish the Activity and log them in automatically
                //Toast.makeText(getBaseContext(), "LOgeate por favor", Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(intent, REQUEST_SIGNUP);

        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Acceso fallido", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    protected void ArmarJSONlogin(){
        SharedPreferences.Editor editor = preferencias.edit();

        JSONArray jlogin = new JSONArray();

        try{
            JSONObject track = new JSONObject();
            track.put("email", email);
            track.put("clave", password);

            jlogin.put(track);
            editor.putString("jlogin",jlogin.toString());
            Log.e(TAG, "jlogin: " + jlogin.toString());
            editor.apply();
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }



    public class TareaAsincrona1 extends AsyncTask<String, Boolean, String> {

        String current  = "";

        public TareaAsincrona1() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Identificando...");
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... datos) {

            String URLServer = datos[0];
            String jlogin = datos[1];

            Log.e("onbackground", "URLServer: "+URLServer);
            Log.e("onbackground", "jlogin: "+jlogin);

            try {
                URL e = new URL(URLServer + "/controlador/AndroidLogin.php");
                HttpURLConnection urlConnection = (HttpURLConnection)e.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                OutputStreamWriter streamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                streamWriter.write(jlogin);
                streamWriter.flush();
                //----Leer el response de la peticion POST
                InputStream in1 = urlConnection.getInputStream();
                InputStreamReader isw1 = new InputStreamReader(in1);

                StringBuilder cadena = new StringBuilder();
                for(int data = isw1.read(); data != -1; data = isw1.read()) {
                    cadena.append((char)data);
                }
                current = cadena.toString().replaceAll("\n", "");
                urlConnection.disconnect();


            }catch (IOException var8) {

                Log.e(TAG, "Error(POST): " + var8);
                current = "error";
            }

            return current;
        }

        @Override
        protected void onPostExecute(String respuesta) {
            super.onPostExecute(respuesta);
            progressDialog.dismiss();
            ProcesarRespuestaDeServidor(current);

        }

    }//tareaasincrona

    public void ProcesarRespuestaDeServidor(String respuesta){
        Log.e(TAG, "respuesta servidor: "+respuesta);
        if ("identificado".equals(respuesta)){
            onLoginSuccess();
        }else{
            onLoginFailed();
        }

    }

}