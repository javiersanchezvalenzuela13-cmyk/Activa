package org.example.activa.model;

import org.example.activa.model.enums.EstadoInscripcion;

import java.time.LocalDateTime;

// Una persona inscrita en una actividad: su inscripción + su nombre y email
public record Inscrito(
        int idInscripcion,
        int idUsuario,
        String nombre,
        String email,
        LocalDateTime fechaInscripcion,
        EstadoInscripcion estado) {

    public boolean estaActiva() {
        return estado == EstadoInscripcion.CONFIRMADA || estado == EstadoInscripcion.PENDIENTE_PAGO;
    }

    // Iniciales para el avatar (de "Ana Morales" sale "AM")
    public String iniciales() {
        String[] partes = nombre.trim().split("\\s+");
        String ini = partes[0].substring(0, 1);
        if (partes.length > 1) {
            ini += partes[1].substring(0, 1);
        }
        return ini.toUpperCase();
    }
}