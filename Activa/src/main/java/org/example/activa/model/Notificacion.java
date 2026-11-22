package org.example.activa.model;

import org.example.activa.util.Vista;

public class Notificacion {

    private final String titulo;
    private final String detalle;
    private final String tiempo;
    private final String icono;
    private final Vista vistaDestino;

    public Notificacion(String titulo, String detalle, String tiempo, String icono, Vista vistaDestino) {
        this.titulo = titulo;
        this.detalle = detalle;
        this.tiempo = tiempo;
        this.icono = icono;
        this.vistaDestino = vistaDestino;
    }

    public String getTitulo() { return titulo; }
    public String getDetalle() { return detalle; }
    public String getTiempo() { return tiempo; }
    public String getIcono() { return icono; }
    public Vista getVistaDestino() { return vistaDestino; }
}