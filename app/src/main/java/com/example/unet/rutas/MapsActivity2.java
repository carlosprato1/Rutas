package com.example.unet.rutas;

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
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback {
    SharedPreferences preferencias;
    private GoogleMap mMap;
    private static String TAG = "mapa_splash";
    ImageView iconoimg;
    Bitmap imagenFromServer;
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);



        preferencias = PreferenceManager.getDefaultSharedPreferences(this);

        String urlserver = preferencias.getString("ET_URL","cualquiera");
        String nombreimgagen = preferencias.getString("Server_name","default.gif");
        new TareaAsincrona2().execute(urlserver,nombreimgagen);


        Button _menuButton = findViewById(R.id.menuButton);
        Button _salirButton = findViewById(R.id.salir);

        _menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivityForResult(intent, 0);
            }
        });
        _salirButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(intent, 0);
            }
        });


    }
    public void llamarmapa(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        Double lat =Double.parseDouble(preferencias.getString("Server_latitud","0.1"));
        Double lon =Double.parseDouble(preferencias.getString("Server_longitud","0.1"));
        String name = preferencias.getString("Server_name","Vacio");
        String autor = preferencias.getString("Server_autores","vacio");
        String nota = preferencias.getString("Server_parcial1","0");

        LatLng pos = new LatLng(lat, lon);

            mMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(name)
                    .snippet(autor + ", Parcial1: " + nota)
                    .icon(BitmapDescriptorFactory.fromBitmap(imagenFromServer)));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));

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
                URL url = new URL(URLServer+"/vistas/imagenes/img_perfil/"+imagen+".gif");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;

            }catch (IOException var8) {
                Log.e(TAG, "Bitmap1: " + var8);
                try {
                    URL url = new URL(URLServer + "/vistas/imagenes/img_perfil/default.gif");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    return myBitmap;
                }catch (IOException var9) {Log.e(TAG, "Bitmap-default: " + var8);
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Bitmap img) {
            if(img != null){
                imagenFromServer = img;
            }
            llamarmapa();


        }
    }//tareaasincrona


}
