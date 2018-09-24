package com.example.unet.rutas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;


public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback {
    SharedPreferences preferencias;
    private GoogleMap mMap;
    private static String TAG = "mapa_splash";
    String urlserver;
    String jsonTipo;
    Boolean miposicion = true;
    String Ename[] = new String[20];
    String Elat[]= new String[20];
    String Elon[]= new String[20];
    String EEmail []= new String[20];
    String EAuditorium[]= new String[20];
    String EPregunta[]= new String[20];
    String ERespuesta[]= new String[20];
    String Eparcial1[]= new String[20];
    String Eparcial2[]= new String[20];
    String Eparcial3[]= new String[20];
    String bandera[]= new String[20];

    private HashMap<Marker, Integer> mHashMap = new HashMap<>();

    int count = 1;


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        miposicion();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMap != null) {mMap.clear();}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        preferencias = PreferenceManager.getDefaultSharedPreferences(this);

        Button _menuButton = findViewById(R.id.menuButton);
        Button _salirButton = findViewById(R.id.salir);
        Button evaluadores = findViewById(R.id.Evaluados);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        _menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null) {mMap.clear();}
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivityForResult(intent, 0);
            }
        });
        _salirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null) {mMap.clear();}
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(intent, 0);
            }
        });
        evaluadores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//disable button until finish
                if (mMap != null) {mMap.clear();}
                count = 1;
                addEvaluados();
            }
        });


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.mMap = googleMap;
        miposicion();

        mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                int pos = mHashMap.get(marker);

                if(pos != 0) {
                    if ("0".equals(bandera[pos]) || EAuditorium[pos].equals(preferencias.getString("Server_user_email", "Vacio"))){
                        Intent intent = new Intent(getApplicationContext(), formularioActivity.class)
                                .putExtra("eva_name",Ename[pos])
                                .putExtra("eva_email",EEmail[pos] )
                                .putExtra("eva_audito",EAuditorium[pos])
                                .putExtra("eva_pregun",EPregunta[pos] )
                                .putExtra("eva_resp",ERespuesta[pos])
                                .putExtra("eva_par1",Eparcial1[pos])
                                .putExtra("eva_par2",Eparcial2[pos])
                                .putExtra("eva_par3",Eparcial3[pos]);

                        startActivity(intent);
                    }else{
                        Toast.makeText(getBaseContext(), Ename[pos]+" Esta pendiente por Responder a otro Evaluador", Toast.LENGTH_LONG).show();
                    }


                }

                return false;

            }
        });

    }
    public void miposicion(){
        miposicion = true;
        urlserver = preferencias.getString("ET_URL", "cualquiera");
        String nombreimgagen = preferencias.getString("Server_name", "default");
        String URI = "/vistas/imagenes/img_perfil/" + nombreimgagen + ".gif";
        new TareaAsincrona2().execute(urlserver, URI);

    }


    public class TareaAsincrona2 extends AsyncTask<String, Boolean, Bitmap> {

        public TareaAsincrona2() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... datos) {
            String URLServer = datos[0];
            String imagen = datos[1];

            try {
                URL url = new URL(URLServer + imagen);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;

            } catch (IOException var8) {
                Log.e(TAG, "Bitmap1: " + var8);
                try {
                    URL url = new URL(URLServer + "/vistas/imagenes/img_perfil/default.gif");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    return myBitmap;
                } catch (IOException var9) {
                    Log.e(TAG, "Bitmap-default: " + var8);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap img) {
            if (img != null) {
                if (miposicion) {
                    Double lat = Double.parseDouble(preferencias.getString("Server_latitud", "0.1"));
                    Double lon = Double.parseDouble(preferencias.getString("Server_longitud", "0.1"));
                    String name = preferencias.getString("Server_name", "Vacio");
                    String autor = preferencias.getString("Server_autores", "vacio");
                    String nota = preferencias.getString("Server_parcial1", "0");


                    LatLng pos = new LatLng(lat, lon);
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .title(name)
                            .snippet("Evaluador")
                            .icon(BitmapDescriptorFactory.fromBitmap(img)));

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(6F));
                    mHashMap.put(marker,0);
                }else{

                    LatLng pos = new LatLng(Double.parseDouble(Elat[count]), Double.parseDouble(Elon[count]));
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(pos)
                            .icon(BitmapDescriptorFactory.fromBitmap(img)));

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(8F));
                    mHashMap.put(marker,count);
                    count ++;

                }
            }

        }

    }//tareaasincrona2

    public void addEvaluados() {
        if (this.mMap != null) {
            new TareaAsincrona3().execute(urlserver, "/controlador/reporteMapa.php","evaluados");
        }


    }//addEvaluadores

    public class TareaAsincrona3 extends AsyncTask<String, Boolean, String> {

        public TareaAsincrona3() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... datos) {
            String URLServer = datos[0];
            String URI = datos[1];
            String tipo = datos[2];
            try {
                JSONObject track = new JSONObject();
                track.put("tipo", tipo);
                jsonTipo = track.toString();

            } catch (JSONException e) {}
            try {
                URL e = new URL(URLServer + URI);
                HttpURLConnection urlConnection = (HttpURLConnection) e.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                OutputStreamWriter streamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                streamWriter.write(jsonTipo);
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
                Log.e(TAG, "error server: " + var8);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String evaluados) {

            if (evaluados != null) {

                try {
                    JSONArray jrespuestamapa = new JSONArray(evaluados);
                    if ("Correcto".equals(jrespuestamapa.getString(0))) {


                        for (int i = 1; i < jrespuestamapa.length(); i++) {

                            JSONObject itemEvaluados = jrespuestamapa.getJSONObject(i);

                            Ename[i] = itemEvaluados.getString("name");
                            Elat[i] = itemEvaluados.getString("latitud");
                            Elon[i] = itemEvaluados.getString("longitud");
                            EEmail[i] = itemEvaluados.getString("user_email");
                            EAuditorium[i] = itemEvaluados.getString("auditorium");
                            EPregunta[i] = itemEvaluados.getString("auditorium_pregunta");
                            ERespuesta[i] = itemEvaluados.getString("auditorium_respuesta");
                            Eparcial1[i] = itemEvaluados.getString("parcial1");
                            Eparcial2[i] = itemEvaluados.getString("parcial2");
                            Eparcial3[i] = itemEvaluados.getString("parcial3");
                            bandera[i] = itemEvaluados.getString("bandera");

                            // Alumnos

                            miposicion = false;

                            String URI = "/vistas/imagenes/img_perfil/" + Ename[i] + ".gif";
                            new TareaAsincrona2().execute(urlserver, URI);

                        }//for
                    } else {
                        //error/noexisten evaluados
                        Log.e(TAG, "onPostExecute (no-null): no existe");
                    }
                } catch (JSONException e) {Log.e(TAG, "error onPostExecute for: " + e);}
            }//if null
            // llamarmapa();

        }

    }//tareaasincrona2
}//class
