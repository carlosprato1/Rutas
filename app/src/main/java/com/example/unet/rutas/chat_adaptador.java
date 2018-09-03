package com.example.unet.rutas;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by root on 31/08/18.
 */

public class chat_adaptador extends RecyclerView.Adapter<chat_adaptador.ViewHolderDatos>{
//necesita una lista para mostrarla en el recycle

    ArrayList<item_mensaje> item_mensaje;

    public chat_adaptador(ArrayList<item_mensaje> item_mensaje) {
        //a este adaptador le llegan datos y se los entrega a list_datos
        this.item_mensaje = item_mensaje;
    }

    @Override
    public int getItemViewType(int position) {

       if("s".equals(item_mensaje.get(position).getCreado())){
           return 1;
       }else{
           return 2;
       }
    }

    @Override
    public ViewHolderDatos onCreateViewHolder(ViewGroup parent, int viewType) {
        //enlaza el la clase adaptador con item_massage_received.xml

    if (viewType == 1){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received,null,false);
        return new ViewHolderDatos(view);
    }else{
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent,null,false);
        return new ViewHolderDatos(view);
    }


    }

    @Override
    public void onBindViewHolder(ViewHolderDatos holder, int position) {
        //este se ecarga de establecer la comunicacion entre el adaptador y la clase ViewHolderDatos
        //holder.asignarDatos(list_datos.get(position));//le enviamos la informacion que quiero que muestre

        holder.texto.setText(item_mensaje.get(position).getTexto());
        holder.tiempo.setText(item_mensaje.get(position).getTiempo());
    }

    @Override
    public int getItemCount() {
        return item_mensaje.size();
    }

    public class ViewHolderDatos extends RecyclerView.ViewHolder{

        TextView texto,tiempo;

        public ViewHolderDatos(View itemView) {
            super(itemView);
            texto = itemView.findViewById(R.id.text_message_body);
            tiempo = itemView.findViewById(R.id.text_message_time);

        }


    }
}
