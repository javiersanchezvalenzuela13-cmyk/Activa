package org.example.activa.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Actividad {

    // Atributos (uno por columna de la tabla "actividad")
    private int idActividad;
    private String nombreActividad;
    private String descripcion;
    private String lugar;
    private LocalDate fechaActividad;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private BigDecimal precio;
    private int cupoActual;
    private int cupoMax;
    private int idUsuario;
    private List<String> etiquetas = new ArrayList<>();
    private byte[] imagen;   // foto en bytes (null si no tiene)

    // Constructores

    // 1. Vacío
    public Actividad() {
    }

    // 2. Sin id
    public Actividad(String nombreActividad, String descripcion, String lugar,
                     LocalDate fechaActividad, LocalTime horaInicio, LocalTime horaFin,
                     BigDecimal precio, int cupoMax, int idUsuario) {
        this.nombreActividad = nombreActividad;
        this.descripcion = descripcion;
        this.lugar = lugar;
        this.fechaActividad = fechaActividad;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.precio = precio;
        this.cupoActual = 0;        // recién creada: nadie inscrito
        this.cupoMax = cupoMax;
        this.idUsuario = idUsuario;
    }

    // 3. Completo
    public Actividad(int idActividad, String nombreActividad, String descripcion, String lugar,
                     LocalDate fechaActividad, LocalTime horaInicio, LocalTime horaFin,
                     BigDecimal precio, int cupoActual, int cupoMax, int idUsuario) {
        this(nombreActividad, descripcion, lugar, fechaActividad, horaInicio, horaFin,
                precio, cupoMax, idUsuario);
        this.idActividad = idActividad;
        this.cupoActual = cupoActual;
    }

    // Métodos de ayuda (lógica sencilla de la propia actividad)

    public int getPlazasLibres() {
        return cupoMax - cupoActual;
    }

    public boolean estaCompleta() {
        return cupoActual >= cupoMax;
    }

    public boolean esGratis() {
        return precio == null || precio.compareTo(BigDecimal.ZERO) == 0;
    }

    // Getters y Setters

    public int getIdActividad() { return idActividad; }
    public void setIdActividad(int idActividad) { this.idActividad = idActividad; }

    public String getNombreActividad() { return nombreActividad; }
    public void setNombreActividad(String nombreActividad) { this.nombreActividad = nombreActividad; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public LocalDate getFechaActividad() { return fechaActividad; }
    public void setFechaActividad(LocalDate fechaActividad) { this.fechaActividad = fechaActividad; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public int getCupoActual() { return cupoActual; }
    public void setCupoActual(int cupoActual) { this.cupoActual = cupoActual; }

    public int getCupoMax() { return cupoMax; }
    public void setCupoMax(int cupoMax) { this.cupoMax = cupoMax; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public List<String> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<String> etiquetas) { this.etiquetas = etiquetas; }

    public byte[] getImagen() { return imagen; }
    public void setImagen(byte[] imagen) { this.imagen = imagen; }

    public boolean tieneImagen() {
        return imagen != null && imagen.length > 0;
    }

    @Override
    public String toString() {
        return nombreActividad + " (" + fechaActividad + ")";
    }
}