package org.example.activa.db;

import org.example.activa.exception.ActividadException;
import org.example.activa.exception.InscripcionException;
import org.example.activa.model.Actividad;
import org.example.activa.model.Inscripcion;
import org.example.activa.service.ActividadService;
import org.example.activa.service.InscripcionService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class PruebaCRUD {

    // Cambia este número por el id_usuario que te ha dado el SELECT
    private static final int ID_USUARIO = 1;

    public static void main(String[] args) {
        ActividadService actividadService = new ActividadService();
        InscripcionService inscripcionService = new InscripcionService();

        try {
            // Create: crear actividad
            Actividad nueva = new Actividad(
                    "Yoga de prueba", "Actividad creada desde PruebaCRUD", "Parque del Retiro",
                    LocalDate.now().plusDays(7), LocalTime.of(9, 0), LocalTime.of(10, 0),
                    BigDecimal.ZERO, 2, ID_USUARIO);
            actividadService.crear(nueva);
            int id = nueva.getIdActividad();
            System.out.println("1. Creada: " + nueva + " con id " + id);

            // Read: leer actividad
            Actividad leida = actividadService.buscarPorId(id);
            System.out.println("2. Leída: " + leida + " | plazas libres: " + leida.getPlazasLibres());

            // Update: editar actividad
            leida.setLugar("Casa de Campo");
            actividadService.editar(leida);
            System.out.println("3. Editada: nuevo lugar = " + actividadService.buscarPorId(id).getLugar());

            // Inscribirse
            Inscripcion ins = inscripcionService.inscribir(ID_USUARIO, id);
            System.out.println("4. Inscrito: " + ins.getEstado()
                    + " | plazas libres: " + actividadService.buscarPorId(id).getPlazasLibres());

            // Inscribirse otra vez (debe fallar)
            try {
                inscripcionService.inscribir(ID_USUARIO, id);
                System.out.println("5. ERROR: ha dejado inscribirse dos veces");
            } catch (InscripcionException e) {
                System.out.println("5. Correcto, no deja repetir: " + e.getMessage());
            }

            // Cancelar
            inscripcionService.cancelar(ID_USUARIO, id);
            System.out.println("6. Cancelado | plazas libres: "
                    + actividadService.buscarPorId(id).getPlazasLibres());

            // Volver a inscribirse (reactiva la fila cancelada)
            inscripcionService.inscribir(ID_USUARIO, id);
            System.out.println("7. Reinscrito | ¿inscrito? " + inscripcionService.estaInscrito(ID_USUARIO, id));

            // Validación (debe fallar)
            try {
                leida.setHoraFin(LocalTime.of(8, 0));
                actividadService.editar(leida);
                System.out.println("8. ERROR: ha dejado poner la hora de fin antes que la de inicio");
            } catch (ActividadException e) {
                System.out.println("8. Correcto, validación: " + e.getMessage());
            }

            // Delete: borrar actividad (y su inscripción por el CASCADE)
            actividadService.eliminar(id);
            System.out.println("9. Eliminada. Prueba terminada.");

        } catch (ActividadException | InscripcionException e) {
            System.out.println("FALLO: " + e.getMessage());
            if (e.getCause() != null) {
                e.getCause().printStackTrace();
            }
        }
    }
}