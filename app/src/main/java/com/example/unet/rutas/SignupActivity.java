package com.example.unet.rutas;

        import android.app.ProgressDialog;
        import android.content.Intent;
        import android.content.SharedPreferences;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.preference.PreferenceManager;
        import android.support.v7.app.AppCompatActivity;
        import android.util.Log;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
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

        import butterknife.ButterKnife;
        import butterknife.BindView;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    SharedPreferences preferencias;
    ProgressDialog progressDialog;
    String URLServer;
    String name;
    String email;
    String password;
    String parcial1;
    String autor;
    String lat;
    String lon;

    @BindView(R.id.input_name) EditText _nameText;
    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_parcial1) TextView _parcial1Text;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_signup) Button _signupButton;
    @BindView(R.id.link_login) TextView _loginLink;
    @BindView(R.id.sprinner) Spinner _autoresSpinner;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        preferencias = PreferenceManager.getDefaultSharedPreferences(this);
        ButterKnife.bind(this);

        startService(new Intent(this, Myservice.class).putExtra("id","registro")); //actualizar ubicacion para registro

        URLServer = preferencias.getString("ET_URL","cualquiera");

       lat = Float.toString(preferencias.getFloat("Lat",0.0F)) ;
       lon = Float.toString(preferencias.getFloat("Lon",0.0F));


        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // tienes cuenta? logueate
                finish();
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.autoresarray, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        _autoresSpinner.setAdapter(adapter);

        _autoresSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id)
            {
                autor = (String) adapterView.getItemAtPosition(pos);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {    }
        });

    }

    public void signup() {
        Log.e(TAG, "Signup");

        if ("cualquiera".equals(URLServer)){
            Toast.makeText(getBaseContext(), "Introduzca la Direccion del Servidor", Toast.LENGTH_LONG).show();
            return;
        }

        if (!validate()) {
            _signupButton.setEnabled(true);
            return;
        }

        _signupButton.setEnabled(false);



        name = _nameText.getText().toString();
        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();
        parcial1 = _parcial1Text.getText().toString();


        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Myservice.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {

            ArmarJSONRegistro();
            new TareaAsincrona().execute(URLServer,preferencias.getString("jregistro",""));;

        } else {
            Toast.makeText(getBaseContext(), "No hay conexion a la red", Toast.LENGTH_LONG).show();

        }

    }//singup



    public void onSignupSuccess() {
        Log.e(TAG, "onSignupSuccess");
        Toast.makeText(getBaseContext(), "Registro Exitoso, Por favor introduzca sus Datos", Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(intent, 1);

        finish();
    }

    public void onSignupFailed() {

        Toast.makeText(getApplicationContext(), "Registro fallido", Toast.LENGTH_LONG).show();
        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        String parcial = _parcial1Text.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("al menos 3 caracteres");
            valid = false;
        } else {
            _nameText.setError(null);
        }


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("email invalido");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("entre 4 y 10 caracteres alfanumericos");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (parcial.isEmpty()) {
            _parcial1Text.setError("introduzca nota");
            valid = false;
        } else {
            _parcial1Text.setError(null);
        }

        return valid;
    }

    protected void ArmarJSONRegistro(){
        SharedPreferences.Editor editor = preferencias.edit();

        JSONArray jregistro = new JSONArray();

        try{
            JSONObject track = new JSONObject();
            track.put("name", name);
            track.put("email", email);
            track.put("clave", password);
            track.put("parcial1", parcial1);
            track.put("autor", autor);
            track.put("lat",lat);
            track.put("lon",lon);

            jregistro.put(track);

            editor.putString("jregistro",jregistro.toString());
            Log.e(TAG, "jregistro: " + jregistro.toString());
            editor.apply();
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }
    public void ProcesarRespuestaDeServidor(String respuesta){

        if ("aceptado".equals(respuesta)){
            onSignupSuccess();
        }else{
            onSignupFailed();
        }

    }

    public class TareaAsincrona extends AsyncTask<String, Boolean, String> {

        String current  = "";


        public TareaAsincrona() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

             progressDialog = new ProgressDialog(SignupActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Creando cuenta...");
            progressDialog.show();

        }

        @Override
        protected String doInBackground(String... datos) {

            String URLServer = datos[0];
            String jregistro = datos[1];

            Log.e("onbackground", "URLServer: "+URLServer);
            Log.e("onbackground", "jregistro: "+jregistro);

            try {
                URL e = new URL(URLServer + "/controlador/AndroidRegistro.php");
                HttpURLConnection urlConnection = (HttpURLConnection)e.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                OutputStreamWriter streamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                streamWriter.write(jregistro);
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
            Log.e(TAG, "respuesta (registro):"+ current);
            ProcesarRespuestaDeServidor(current);

        }

    }//tareaasincrona
///LOALIZACION


}