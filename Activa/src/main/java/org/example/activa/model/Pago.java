package org.example.activa.model;
import org.example.activa.model.enums.EstadoPago;
import org.example.activa.model.enums.MetodoPago;

import java.time.LocalDateTime;
//Clase que contiene los atributos,getter y setters para crear nuevos pagos
public class Pago {

    private int idPago;
    private int idInscripcion;
    private double importe;
    private MetodoPago metodoPago; //ENUM con los posibles métodos de pago
    private EstadoPago estado; // ENUM con los estados posibles de un pago
    private LocalDateTime fechaPago;
    private String tituloActividad; // Para mostrar el nombre de la actividad a la hora del pago

    //Constructores
    public Pago() {
    }
    public Pago(int idPago, int idInscripcion, double importe, MetodoPago metodoPago, EstadoPago estado, LocalDateTime fechaPago) {
        this.idPago = idPago;
        this.idInscripcion = idInscripcion;
        this.importe = importe;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.fechaPago = fechaPago;
    }

//Getters y setters
    public int getIdPago() {
        return idPago;
    }
    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }
    public int getIdInscripcion() {
        return idInscripcion;
    }
    public void setIdInscripcion(int idInscripcion) {
        this.idInscripcion = idInscripcion;
    }
    public double getImporte() {
        return importe;
    }
    public void setImporte(double importe) {
        this.importe = importe;
    }
    public MetodoPago getMetodoPago() {
        return metodoPago;
    }
    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }
    public EstadoPago getEstado() {
        return estado;
    }
    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }
    public LocalDateTime getFechaPago() {
        return fechaPago;
    }
    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
    public String getTituloActividad() {
        return tituloActividad;
    }
    public void setTituloActividad(String tituloActividad) {
        this.tituloActividad = tituloActividad;
    }
}