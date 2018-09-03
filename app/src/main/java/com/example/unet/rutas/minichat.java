package com.example.unet.rutas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by root on 31/08/18.
 */

public class minichat extends AppCompatActivity{

    //el mmainactvity le hace referencia al dato
    ArrayList<item_mensaje> item_mensaje;
    RecyclerView recycler;
    SharedPreferences preferencias;
    String sharedJsonmensaje;
    String fechaGPS;
    BroadcastReceiver receiver;


    @Override
    protected void onCreate (Bundle savedInstanceState){
        this.setContentView(R.layout.activity_message_list);
        IntentFilter filter = new IntentFilter();
        filter.addAction("test.UPDATEMENSAJE");
        BroadcastReceiver receiver = new llamadaDelServicio();
        registerReceiver(receiver, filter);



        //Button btn = (Button) findViewById(R.id.button_chatbox_send);
        View alertView = getLayoutInflater().inflate(R.layout.activity_message_list, null, false);
        Button btn = (Button) alertView.findViewById(R.id.button_chatbox_send);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boton_chatbox(v);
            }
        });

        super.onCreate(savedInstanceState);
        preferencias= PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_message_list);
        //setContentView(R.layout.minichat);

        recycler = findViewById(R.id.reyclerview_message_list);
        recycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));

        item_mensaje = new ArrayList <> ();
        llenar_mensajes();


        chat_adaptador adapter = new chat_adaptador(item_mensaje);
        recycler.setAdapter(adapter);

    }
    private void llenar_mensajes(){//agarraro el acumulado y lo imprimo todos (solo cuando abro este activity)

        sharedJsonmensaje= preferencias.getString("sharedJsonmensaje","");

        if ("".equals(sharedJsonmensaje)){
            Log.e("carlos", "no hay mensajes" );
        }else{
           try{
               JSONArray jtodos_mensajes = new JSONArray(sharedJsonmensaje);
               Log.e("llenar_mensajes", "sharedjsonmensajes: " +jtodos_mensajes);
               for (int i = 0; i < jtodos_mensajes.length(); i++) {
                   JSONObject junmensaje = jtodos_mensajes.getJSONObject(i);
                   item_mensaje.add(new item_mensaje(junmensaje.getString("texto"), junmensaje.getString("creado"), junmensaje.getString("tiempo")));
               }//for
           } catch(JSONException e) {
               Log.e("carlos", "Error Json, llenar_mensajes: " + e);
           }
        }

        }//llenar_mensajes

    public void refrescarchat(){ //como ya estan impresos solo imprimo la diferencia, en el caso el chatbox y de mensajes del servidor.

       try{
           String current = preferencias.getString("sharedJsonmensaje","");
           JSONArray jtodos_mensajes_current = new JSONArray(current);
           Integer anterior = preferencias.getInt("anterior", 1);


               for (int i = anterior; i < jtodos_mensajes_current.length(); i++) {
                   JSONObject junmensaje = jtodos_mensajes_current.getJSONObject(i);
                   item_mensaje.add(new item_mensaje(junmensaje.getString("texto"), junmensaje.getString("creado"), junmensaje.getString("tiempo")));
               }//for


       } catch(JSONException e) {
           Log.e("carlos", "Error Json, refrescarchat: " + e);
    }

        chat_adaptador adapter = new chat_adaptador(item_mensaje);
        recycler.setAdapter(adapter);

    }


    public void boton_chatbox(View v) {

        SharedPreferences.Editor editor = preferencias.edit();
        JSONArray jtodos_mensajes = new JSONArray();
        JSONArray mensajesToServidor = new JSONArray();
        try {

            JSONObject track = new JSONObject();


            EditText editText = this.findViewById(R.id.edittext_chatbox);
            String textEdittext = editText.getText().toString();
            editText.setText("");

            if ("".equals(textEdittext)){

                return;}
            if ("borrarchat".equals(textEdittext)){
                editor.putBoolean("primermensaje", true);
                editor.putString("sharedJsonmensaje", "");
                editor.putBoolean("MenjToServidor_acum", true);
                editor.apply();
                Toast.makeText(this.getApplicationContext(), "Chat Borrado", Toast.LENGTH_SHORT).show();
                return;}

            boolean primermensaje = preferencias.getBoolean("primermensaje", true);
            boolean MenjToServidor_acum = preferencias.getBoolean("MenjToServidor_acum", true);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",java.util.Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getTimeZone("America/Caracas"));
            Date fecha = Calendar.getInstance().getTime();

            try {
                fechaGPS = URLEncoder.encode(dateFormat.format(fecha), "UTF-8");
            } catch (UnsupportedEncodingException var19) {
                var19.printStackTrace();
            }

            track.put("texto", textEdittext);
            track.put("tiempo", fechaGPS);
            track.put("creado", "c");


            if (primermensaje) {//nuevo o borrar
                editor.putBoolean("primermensaje", false);
                Log.e("carlos", "primer mensaje");
                editor.putInt("anterior",0);
            } else {//acumular
                String sharedJsonmensaje = preferencias.getString("sharedJsonmensaje", "");
                jtodos_mensajes = new JSONArray(sharedJsonmensaje);
                Log.e("chatbox", "jsonacumuladoantes: " + jtodos_mensajes);
                editor.putInt("anterior",jtodos_mensajes.length());
            }
            if (MenjToServidor_acum){
                editor.putBoolean("MenjToServidor_acum", false);
            }else{//acumular
                String mensajesToServidorString = preferencias.getString("mensajesToServidor", "");
                mensajesToServidor = new JSONArray(mensajesToServidorString);
            }

            jtodos_mensajes.put(track);
            mensajesToServidor.put(track);

            editor.putString("sharedJsonmensaje", jtodos_mensajes.toString());
            editor.putString("mensajesToServidor", mensajesToServidor.toString());
            editor.apply();
            refrescarchat();
            Log.e("chatbox", "jsonDespues: " + jtodos_mensajes.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }//chat_box

    public class llamadaDelServicio extends BroadcastReceiver {
        public llamadaDelServicio() {
            super();
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            refrescarchat();
        }//Onreceiver
    }

    @Override
    protected void onPause() {
        super.onPause();
      try {
          unregisterReceiver(receiver);
      } catch (IllegalArgumentException e){

      }



    }

}//clase
