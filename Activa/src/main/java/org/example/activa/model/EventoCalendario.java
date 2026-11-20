package org.example.activa.model;
import java.time.LocalDateTime;

//Clase DTO que agrupa los datos de una actividad y el estado de una inscripción para mostrarlo en el calendario
public class EventoCalendario {
    private int idActividad;
    private String titulo;
    private LocalDateTime fechaHora;
    private LocalDateTime fechaHoraFin;
    private String lugar;
    private String categoria;
    private String imagen;    // Ruta de la foto

    public EventoCalendario() {
    }
    public EventoCalendario(int idActividad, String titulo, LocalDateTime fechaHora, LocalDateTime fechaHoraFin, String lugar, String categoria, String imagen) {
        this.idActividad = idActividad;
        this.titulo = titulo;
        this.fechaHora = fechaHora;
        this.fechaHoraFin = fechaHoraFin;
        this.lugar = lugar;
        this.categoria = categoria;
        this.imagen = imagen;
    }
//Getter y setters
    public int getIdActividad() {
        return idActividad;
    }
    public void setIdActividad(int idActividad) {
        this.idActividad = idActividad;
    }
    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }
    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }
    public String getLugar() {
        return lugar;
    }
    public void setLugar(String lugar) {
        this.lugar = lugar;
    }
    public String getCategoria() {
        return categoria;
    }
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    public String getImagen() {
        return imagen;
    }
    public void setImagen(String imagen) {
        this.imagen = imagen;
    }
}