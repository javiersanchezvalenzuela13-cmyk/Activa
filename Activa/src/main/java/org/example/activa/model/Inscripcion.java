package org.example.activa.model;

import org.example.activa.model.enums.EstadoInscripcion;

import java.time.LocalDateTime;

public class Inscripcion {

    // Atributos
    private int idInscripcion;
    private LocalDateTime fechaInscripcion;
    private EstadoInscripcion estado;
    private int idActividad;
    private int idUsuario;

    // Constructores

    // Vacío
    public Inscripcion() {
    }

    // Sin id ni fecha
    public Inscripcion(int idActividad, int idUsuario, EstadoInscripcion estado) {
        this.idActividad = idActividad;
        this.idUsuario = idUsuario;
        this.estado = estado;
    }

    // Completo
    public Inscripcion(int idInscripcion, LocalDateTime fechaInscripcion,
                       EstadoInscripcion estado, int idActividad, int idUsuario) {
        this(idActividad, idUsuario, estado);
        this.idInscripcion = idInscripcion;
        this.fechaInscripcion = fechaInscripcion;
    }

    // Métodos de ayuda

    public boolean estaActiva() {
        return estado == EstadoInscripcion.CONFIRMADA
                || estado == EstadoInscripcion.PENDIENTE_PAGO;
    }

    public boolean estaPendienteDePago() {
        return estado == EstadoInscripcion.PENDIENTE_PAGO;
    }

    // Getters y Setters

    public int getIdInscripcion() { return idInscripcion; }
    public void setIdInscripcion(int idInscripcion) { this.idInscripcion = idInscripcion; }

    public LocalDateTime getFechaInscripcion() { return fechaInscripcion; }
    public void setFechaInscripcion(LocalDateTime fechaInscripcion) { this.fechaInscripcion = fechaInscripcion; }

    public EstadoInscripcion getEstado() { return estado; }
    public void setEstado(EstadoInscripcion estado) { this.estado = estado; }

    public int getIdActividad() { return idActividad; }
    public void setIdActividad(int idActividad) { this.idActividad = idActividad; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    @Override
    public String toString() {
        return "Inscripción #" + idInscripcion + " (" + estado + ")";
    }
}