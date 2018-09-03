package com.example.unet.rutas;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


//cambio de config: Periodo: estadoServicio=false y stop AlarmManager
//                  NIU,URL,Unidad: si Temporal Borrar.
///////------colocar para que rastre solo cuando esta en un carro detecteActivity.
//
////----------Estado del Equipo------------///Para luego ahora probar
//broadcast: encendido, apagado y bateria en otro servicio? o hacerlo por un Thread...

public class Myservice extends Service implements ConnectionCallbacks,OnConnectionFailedListener,LocationListener {
    public Context context = this;
    public Handler handler = null;
    private Handler mHandler;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationrequest;
    public static Runnable runnable = null;
    private static String TAG = "Servicio";

 //--------Preferencias-------------------
    /////////////CONSTANTES//////////////////
 private static Integer LIMITE_ENTRE_RUNNABLE_Y_ALARMMANAGER = 120000; // ms
 private static Integer CONVERSION_MIN_MS = 60000; // min->ms
    /////////////////////////////////////////
    SharedPreferences preferencias;
    private String URLServer = "cualquiera";
    private String NIU = "cualquiera";
    private String Unidad = "0";
    private int Periodo = 60000;
    private Boolean DescartarLLamadaDeAlarManagerGPSActivo = false;
    private Boolean DescartarLLamadaDeRunnableGPSActivo= false;
    private String estadoConfig;
    private boolean estadoServicio;
    private String estadoDATAJSON;
 //------DatosdeUbicacion-------------------
    private float Distancia = 0.0F;
    private String fechaGPS = "";
    String latitud;
    String longitud;
    Double altitud;
    Float speed;
    Float precision;
    Float direccion;
    Integer ACC;

    public Myservice() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate(){
        Log.e(TAG, "Oncreate");
        preferencias = PreferenceManager.getDefaultSharedPreferences(this);


    }
    @Override
    public void onStart(Intent intent, int starid){

    }
    @Override
    public int onStartCommand (Intent intent, int flags, int starId){
        Log.e(TAG, "onStartCommand");
      //se llama a esta funcion si no se ha cerrado(stopself o servicestop) y alguien lo vuelve a llamar.
        verificarCambiosdePreferencias();

        if (Periodo > LIMITE_ENTRE_RUNNABLE_Y_ALARMMANAGER && !DescartarLLamadaDeAlarManagerGPSActivo && estadoServicio){
            if (!DescartarLLamadaDeRunnableGPSActivo) {//si runable tiene activo la ubicacionfusionada espralo que termine
                EmpezarRastreoAlarmManager();
            }
        }

        if (Periodo <= LIMITE_ENTRE_RUNNABLE_Y_ALARMMANAGER && estadoServicio ) {
            //DescartarLLamadaDeRunnableGPSActivo = false; No lo puedo hacer aqui porque cago el runnable
            //pero sin esto entra automatico en alarManager el runnable
                mHandler = new Handler();
                startRepeatingTask();
                // MainActivity main = new MainActivity();
                // main.cancelAlarmManager();

        }


        return START_REDELIVER_INTENT;
    }
    @Override
    public void onDestroy(){
        Log.e(TAG, "onDestroy");
    }

    Runnable mStatusCheker = new Runnable() {
        @Override
        public void run() {
            try{
            }finally {
    //el stop repeating no servia, esta condicion es la misma que la de acttiva el stoprepeating
                if(Periodo <= LIMITE_ENTRE_RUNNABLE_Y_ALARMMANAGER && estadoServicio) {
                    EmpezarRastreoRunnable();//llama cada periodo de tiempo
                    mHandler.postDelayed(mStatusCheker, Periodo);
                }
            }
        }
    };
    void startRepeatingTask(){
        mStatusCheker.run();
    }
    void stopRepeatingTask(){
        mHandler.removeCallbacks(mStatusCheker);//cancelo por swiche y cambio de periodo
      //  if (Periodo <= LIMITE_ENTRE_RUNNABLE_Y_ALARMMANAGER)//destruyo solo por swiche no periodo (por que ya se llamaria al AlarmManager)
        stopSelf();
    }

    protected void EmpezarRastreoAlarmManager(){
        Log.e(TAG, "EmpezarRastreo: AlarmManager, Pedir Ubicacion Fusionada");
        DescartarLLamadaDeAlarManagerGPSActivo = true;
        ConectarGoogleAPICient();
    }

    void EmpezarRastreoRunnable (){
       if(!DescartarLLamadaDeRunnableGPSActivo){
           verificarCambiosdePreferencias();
           if(Periodo <= LIMITE_ENTRE_RUNNABLE_Y_ALARMMANAGER && estadoServicio) {
               if (!DescartarLLamadaDeAlarManagerGPSActivo) {//no deja que entre si estoy esperando el GPS alarm
    //pero no deja tampoco seguir si no deje terminar y cambio a runnable
                   DescartarLLamadaDeRunnableGPSActivo = true;
                   Log.e(TAG, "EmpezarRastreo: runnable, Pedir Ubicacion Fusionada");
                   ConectarGoogleAPICient();
               }
           }else{  //estadoServicio = false en Runnable
               Log.e(TAG, "EmpezarRastreo: runnable Cancelar");
               //si hago un cambio rapido runnable->Alarm: ayuda a entrar al proximo alarm porque no se destruye
               DescartarLLamadaDeRunnableGPSActivo = true; //por si acaso no se destruye el servicio
               stopRepeatingTask();//stoptask aqui, solo runnable no alarmManager error.

           }
       }
        //Log.e(TAG, "Estoy repitiendo");

    }
    void verificarCambiosdePreferencias(){

        Periodo = Integer.parseInt(preferencias.getString("LP_periodo","1"))*CONVERSION_MIN_MS;//min->ms 60000
        URLServer = preferencias.getString("ET_URL","cualquiera");
        NIU = preferencias.getString("ET_NIU","cualquiera");
        Unidad= preferencias.getString("ET_Unidad","0");
        estadoConfig = preferencias.getString("estadoConfig","correcto");
        estadoServicio = preferencias.getBoolean("estadoServicio",false);
        estadoDATAJSON = preferencias.getString("estadoDATAJSON","NoPrimera");
        ACC = preferencias.getInt("ACC",0);

    }

    protected void ConectarGoogleAPICient(){


        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if (result != 0){
            //Servicio de Google no disponible
        }else{

           googleApiClient = (new GoogleApiClient.Builder(this))
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
                if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                    googleApiClient.connect();
                }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationrequest = LocationRequest.create();
        locationrequest.setInterval(1000L);
        locationrequest.setFastestInterval(1000L);
        locationrequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if(ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") == 0 || ActivityCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationrequest, this);
        }else{
            //Permiso Denegado
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this.getApplicationContext(), "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this.getApplicationContext(), "onConnectionFailed", Toast.LENGTH_SHORT).show();
        stopLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        SharedPreferences.Editor editor = preferencias.edit();
        //PrimeraUbicacion: no pedir Posicion Anterior y menor accuracy
        boolean PrimeraUbicacion = preferencias.getBoolean("PrimeraUbicacion",true);

        if (PrimeraUbicacion){
            editor.putBoolean("PrimeraUbicacion",false);
            editor.apply();
            if(location.getAccuracy() < 100.0F) {

                ObtenerdatosUbicacion(location);
                ArmarJSON();
                clientHTTP_POST();
                //SegundoHilo trabajando
            }
        }else{

           if (location.getAccuracy() < 30.0F) {
               TomarDistancia(location);
               if (Distancia >= 20.0F || !preferencias.getBoolean("MenjToServidor_acum", true)) {
                   if  (preferencias.getBoolean("evitar2veces", true)) {
                       editor.putBoolean("evitar2veces",false);
                       editor.commit();
                       ObtenerdatosUbicacion(location);
                       ArmarJSON();
                       clientHTTP_POST();
                       //SegundoHilo trabajando
                   }
               }else{ //si la distancia es menor "parado"

                  if ("Primera".equals(estadoDATAJSON) && "Por Verificar".equals(estadoConfig)){
                      //oprtunidad de verificar en caso que se cambien la configuracion en corta distancia
                      //esto podra lanzar varios puntos cercanos si se cambia la configuracion varias veces
                      //sobre un mismo punto

                      ObtenerdatosUbicacion(location);
                      ArmarJSON();
                      clientHTTP_POST();
                  }
                   stopLocationUpdates();
                   TerminoSegundoHilo();//caso: distancia corta
               }

           }
        }
    }

    protected void TomarDistancia(Location location){
        Location ubicacionAnterior = new Location ("");
        ubicacionAnterior.setLatitude((double)preferencias.getFloat("LatitudAnterior",0.0F));
        ubicacionAnterior.setLongitude((double)preferencias.getFloat("LongitudAnterior",0.0F));
        Distancia = location.distanceTo(ubicacionAnterior);
    }

    public void ObtenerdatosUbicacion(Location location){
        stopLocationUpdates();

        SharedPreferences.Editor editor = preferencias.edit();
        editor.putFloat("LatitudAnterior", (float)location.getLatitude());
        editor.putFloat("LongitudAnterior", (float)location.getLongitude());
        editor.apply();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",java.util.Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Caracas"));
        Date fecha = Calendar.getInstance().getTime();

        try {
            fechaGPS = URLEncoder.encode(dateFormat.format(fecha), "UTF-8");
        } catch (UnsupportedEncodingException var19) {
            var19.printStackTrace();
        }

        latitud = Double.toString(location.getLatitude());
        longitud = Double.toString(location.getLongitude());
        altitud = location.getAltitude();
        speed = location.getSpeed() * 3.6F; // m/s -> km/h
        precision = location.getAccuracy();
        direccion = location.getBearing();
    }
    protected void ArmarJSON(){
        SharedPreferences.Editor editor = preferencias.edit();

        JSONArray jArray = new JSONArray();
        JSONObject DATADEF = new JSONObject();

        try{
            JSONObject track = new JSONObject();
            track.put("s", speed);
            track.put("p", precision);
            track.put("a", altitud);
            track.put("r", direccion);
            track.put("l", latitud);
            track.put("o", longitud);
            track.put("d", Distancia);
            track.put("n", NIU);
            track.put("f", fechaGPS);
            track.put("u", Unidad);
            track.put("c", ACC);

            if ("Primera".equals(preferencias.getString("estadoDATAJSON","NoPrimera"))){
                Log.e(TAG, "Primer JSON");
            }else{
                Log.e(TAG, "JSON acumulado");
                String json= preferencias.getString("dataJSON","");//tomo en cuenta lo anterior
                jArray = new JSONArray(json);

                if(json.length() > 30000){Log.e(TAG, "JSON LLeno");return;}
            }
            jArray.put(track);
            DATADEF.put("reporteUbicacion",jArray);

            if (!preferencias.getBoolean("MenjToServidor_acum", true)) {

                    String mensajesToServidorString= preferencias.getString("mensajesToServidor","");
                    JSONArray mensajesToServidor = new JSONArray(mensajesToServidorString);
                    DATADEF.put("mensajes",mensajesToServidor);

            }


            editor.putString("dataJSON", jArray.toString());
            editor.putString("DATATOTAL",DATADEF.toString());
            Log.e(TAG, "DATATOTAL: " + DATADEF.toString());
            editor.apply();
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }


    protected void clientHTTP_POST(){
        Thread thread = new Thread(new Runnable() {
            public void run() {

                ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Myservice.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if(networkInfo != null && networkInfo.isConnected()) {
                    try {
                        URL e = new URL(URLServer + "/controlador/reporteUbicacion.php");
                        HttpURLConnection urlConnection = (HttpURLConnection)e.openConnection();
                        urlConnection.setDoOutput(true);
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        urlConnection.setRequestProperty("Accept", "application/json");
                        OutputStreamWriter streamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                        streamWriter.write(preferencias.getString("DATATOTAL",""));
                        streamWriter.flush();
                  //----Leer el response de la peticion POST
                        InputStream in1 = urlConnection.getInputStream();
                        InputStreamReader isw1 = new InputStreamReader(in1);

                        StringBuilder cadena = new StringBuilder();
                        for(int data = isw1.read(); data != -1; data = isw1.read()) {
                            cadena.append((char)data);
                        }
                        String current = cadena.toString().replaceAll("\n", "");
                        urlConnection.disconnect();
                        ProcesarRespuestaDeServidor(current);

                    }catch (IOException var8) {
                            //error en servidor?
                        // var8.printStackTrace();
                        Log.e(TAG, "Error(POST): " + var8);
                        procesarException(var8);
                    }
                } else {
                    noInternet();
                  }
            }
        });
                thread.start();
    }
    protected void procesarException(IOException e){
//UnknownHostException,ConnectException.
//no puedo tomar como noInternet si el servidor esta caido(UnknownHostException...) sino cada ves que pase
//habria que reconfigurar todos los dispositivos.
        if (e.toString().contains("MalformedURLException")){
            //Error En la URL, lo tomo como respuesta del servidor, los demas como no Internet
            ProcesarRespuestaDeServidor("Incorrecto");
        }else{
            noInternet();
        }

    }


    private void stopLocationUpdates() {
        //no pedir mas ubicaciones hasta volver a conectarse con googleAPIclient
        if(this.googleApiClient != null && this.googleApiClient.isConnected()) {
            this.googleApiClient.disconnect();
          //PROblemas aqui
        }
    }
    private void ProcesarRespuestaDeServidor(String respuesta){
       //DUDA: Puede Responder el Servidor otra cosa? ..ver como se responden los errores del servidor no de envio
       //si estadoServicio=falso: cancelar runne(servicio) y alarmManager(activity).
        Log.e(TAG, "respuesta cruda servidor: " + respuesta);

        SharedPreferences.Editor editor = preferencias.edit();

        try {
            JSONArray jRespuesta = new JSONArray(respuesta);


            if ("Incorrecto".equals(jRespuesta.getString(0))){
                editor.putString("estadoConfig", "Incorrecto");
                editor.putBoolean("estadoServicio",false);
                editor.putString("estadoDATAJSON","Primera");//BorroJson
                editor.putBoolean("MenjToServidor_acum", true);
            }
            if ("desasignado".equals(jRespuesta.getString(0))){
                editor.putString("estadoConfig", "Incorrecto");
                editor.putBoolean("estadoServicio",false);
                editor.putString("estadoDATAJSON","Primera");//BorroJson
                editor.putBoolean("desasignado", true);
                editor.putBoolean("MenjToServidor_acum", true);
            }
            if ("ERRORBD".equals(jRespuesta.getString(0))) {
                noInternet();
            }
            if ("Correcto".equals(jRespuesta.getString(0))) {
                editor.putString("estadoConfig", "Correcto");
                editor.putString("estadoDATAJSON","Primera");//BorroJson
                editor.putBoolean("MenjToServidor_acum", true);
                Log.e(TAG, "Respuesta del Servidor: Correcto");
                if (!"nada".equals(jRespuesta.getString(1))) { //si hay mensaje
                    Log.e(TAG, "servidor: " + jRespuesta);
                    JSONArray jtodos_mensajes = new JSONArray();
                    try{
                        boolean primermensaje = preferencias.getBoolean("primermensaje",true);

                        if (primermensaje) {//primera ves
                            editor.putBoolean("primermensaje", false);
                        }else{//no primera ves
                            String sharedJsonmensaje= preferencias.getString("sharedJsonmensaje","");//tomo en cuenta lo anterior
                            JSONArray ARRAYsharedJsonmensaje = new JSONArray(sharedJsonmensaje);
                            editor.putInt("anterior", ARRAYsharedJsonmensaje.length());

                            for (int i = 0; i < ARRAYsharedJsonmensaje.length(); i++) {
                                JSONObject junmensaje = ARRAYsharedJsonmensaje.getJSONObject(i);

                                JSONObject track1 = new JSONObject();
                                track1.put("texto", junmensaje.getString("texto"));
                                track1.put("tiempo", junmensaje.getString("tiempo"));
                                track1.put("creado",junmensaje.getString("creado"));

                                jtodos_mensajes.put(track1);
                            }//for

                        }//else


                        for (int i = 1; i < jRespuesta.length(); i++) {
                            JSONObject junmensaje = jRespuesta.getJSONObject(i);

                                JSONObject track1 = new JSONObject();
                                track1.put("texto", junmensaje.getString("texto"));
                                track1.put("tiempo", junmensaje.getString("tiempo"));
                                track1.put("creado","s");
                            track1.put("enviado", "f");
                            jtodos_mensajes.put(track1);
                        }//for


                        editor.putString("sharedJsonmensaje", jtodos_mensajes.toString());
                        Log.e(TAG, "servidor+acumulado: " + jtodos_mensajes.toString());
                        editor.apply();
                        actualizarActividad_mensajes();

                    }   catch(JSONException e) {
                    e.printStackTrace();
                }

                }//si hay mensaje
            }//correcto


        } catch (JSONException e) {
            Log.e(TAG, "Error Json: " + e);
        }


        editor.commit();
        actualizarActividad();
        TerminoSegundoHilo();//caso: respuesta Servidor


    }
    protected void noInternet(){
        Log.e(TAG, "No hay Internet");
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putString("estadoDATAJSON","NoPrimera");
        editor.apply();
        TerminoSegundoHilo();//caso:No Internet
    }

    protected void actualizarActividad(){
        Intent intent = new Intent();
        intent.setAction("test.UPDATE");
        getBaseContext().sendBroadcast(intent);
    }
    protected void actualizarActividad_mensajes(){
        Intent intent = new Intent();
        intent.setAction("test.UPDATEMENSAJE");
        getBaseContext().sendBroadcast(intent);
    }
    protected void TerminoSegundoHilo(){
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putBoolean("evitar2veces",true);
        editor.apply();
        Log.e(TAG, "TerminoSegundoHilo");
        if (!DescartarLLamadaDeRunnableGPSActivo){
            stopSelf();
        }

        DescartarLLamadaDeRunnableGPSActivo = false;
        DescartarLLamadaDeAlarManagerGPSActivo=false;
      // if (Periodo > LIMITE_ENTRE_RUNNABLE_Y_ALARMMANAGER){//finalizar alarmManager

 /*DescartarLLamadaDeAlarManagerGPSActivo=false; en el caso AlarmManager->runnable el periodo cambia
 y no destruye el servicio y esta variable permaneceria true. por lo que no se pudiera usar
 esta variable para hacer saber al runnable que el alarmManager sigue activo*/





       // }
    }

}