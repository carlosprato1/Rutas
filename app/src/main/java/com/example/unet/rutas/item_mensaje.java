package com.example.unet.rutas;

/**
 * Created by root on 01/09/18.
 */

public class item_mensaje {
    private String texto;
    private String creado;
    private String tiempo;

    public item_mensaje (){

    }

    public item_mensaje(String texto, String creado, String tiempo) {
        this.texto = texto;
        this.creado = creado;
        this.tiempo = tiempo;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getCreado() {
        return creado;
    }

    public void setCreado(String creado) {
        this.creado = creado;
    }

    public String getTiempo() {
        return tiempo;
    }

    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }
}
