package com.example.unet.rutas;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    SharedPreferences preferencias;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("MApa", "onMapReady");
        this.mMap = googleMap;
        if(ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 || ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            mMap.setMyLocationEnabled(true);
        }else{
            Log.e("MApa", "sin permisos");
        }

            SharedPreferences mapapreferences = getSharedPreferences("posiciones_para_mapa", Context.MODE_PRIVATE);
            //SharedPreferences.Editor editormapa = mapapreferences.edit();

            String lat4 = mapapreferences.getString("lat4","nada");
            String lon4 = mapapreferences.getString("lon4","nada");

            String lat3 = mapapreferences.getString("lat3","nada");
            String lon3 = mapapreferences.getString("lon3","nada");

            String lat2 = mapapreferences.getString("lat2","nada");
            String lon2 = mapapreferences.getString("lon2","nada");

            String lat1 = mapapreferences.getString("lat1","nada");
            String lon1 = mapapreferences.getString("lon1","nada");

            String lat0 = mapapreferences.getString("lat0","nada");
            String lon0 = mapapreferences.getString("lon0","nada");

            if (!"nada".equals(lat4)){
                LatLng marker = new LatLng(Double.parseDouble(lat4), Double.parseDouble(lon4));
                mMap.addMarker(new MarkerOptions().position(marker).title("Posicion 1"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
            }
            if (!"nada".equals(lat3)){
                LatLng marker = new LatLng(Double.parseDouble(lat3), Double.parseDouble(lon3));
                mMap.addMarker(new MarkerOptions().position(marker).title("Posicion 2"));

            }
            if (!"nada".equals(lat2)){
                LatLng marker = new LatLng(Double.parseDouble(lat2), Double.parseDouble(lon2));
                mMap.addMarker(new MarkerOptions().position(marker).title("Posicion 3"));

            }
            if (!"nada".equals(lat1)){
                LatLng marker = new LatLng(Double.parseDouble(lat1), Double.parseDouble(lon1));
                mMap.addMarker(new MarkerOptions().position(marker).title("Posicion 4"));

            }
            if (!"nada".equals(lat0)){
                LatLng marker = new LatLng(Double.parseDouble(lat0), Double.parseDouble(lon0));
                mMap.addMarker(new MarkerOptions().position(marker).title("Posicion 5"));

            }






       /*
        LatLng marker = new LatLng(-72, 9);
        mMap.addMarker(new MarkerOptions().position(marker).title("Posicion Actual"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
        }*/
    }




}