package org.example.activa.service;

import org.example.activa.dao.ActividadDAO;
import org.example.activa.dao.InscripcionDAO;
import org.example.activa.exception.InscripcionException;
import org.example.activa.model.Actividad;
import org.example.activa.model.EstadoInscripcion;
import org.example.activa.model.Inscripcion;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class InscripcionService {

    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();

    // Apuntar a un usuario a una actividad
    public Inscripcion inscribir(int idUsuario, int idActividad) throws InscripcionException {
        try {
            // 1. Comprobaciones
            Actividad actividad = obtenerActividad(idActividad);

            if (actividad.getFechaActividad().isBefore(LocalDate.now())) {
                throw new InscripcionException("Esta actividad ya se ha celebrado.");
            }

            Inscripcion existente = inscripcionDAO.buscarPorUsuarioYActividad(idUsuario, idActividad);
            if (existente != null && existente.estaActiva()) {
                throw new InscripcionException("Ya estás inscrito en esta actividad.");
            }

            if (actividad.estaCompleta()) {
                throw new InscripcionException("No quedan plazas en esta actividad.");
            }

            // 2. Estado según el precio
            EstadoInscripcion estado = actividad.esGratis()
                    ? EstadoInscripcion.CONFIRMADA
                    : EstadoInscripcion.PENDIENTE_PAGO;

            // 3. Reservar la plaza (si otro ha cogido la última, falla aquí)
            if (!actividadDAO.ocuparPlaza(idActividad)) {
                throw new InscripcionException("Se acaba de ocupar la última plaza.");
            }

            // 4. Guardar la inscripción (si falla, devolvemos la plaza)
            try {
                if (existente == null) {
                    Inscripcion nueva = new Inscripcion(idActividad, idUsuario, estado);
                    inscripcionDAO.insertar(nueva);
                    return nueva;
                } else {
                    inscripcionDAO.cambiarEstado(existente.getIdInscripcion(), estado);
                    existente.setEstado(estado);
                    return existente;
                }
            } catch (SQLException e) {
                actividadDAO.liberarPlaza(idActividad);
                throw e;
            }

        } catch (SQLException e) {
            throw new InscripcionException("Error al guardar la inscripción. Inténtalo de nuevo.", e);
        }
    }

    // El usuario cancela su inscripción
    public void cancelar(int idUsuario, int idActividad) throws InscripcionException {
        try {
            Actividad actividad = obtenerActividad(idActividad);

            if (actividad.getFechaActividad().isBefore(LocalDate.now())) {
                throw new InscripcionException("No puedes cancelar una actividad que ya se ha celebrado.");
            }

            Inscripcion inscripcion = obtenerInscripcionActiva(idUsuario, idActividad);

            inscripcionDAO.cambiarEstado(inscripcion.getIdInscripcion(), EstadoInscripcion.CANCELADA);
            actividadDAO.liberarPlaza(idActividad);

        } catch (SQLException e) {
            throw new InscripcionException("Error al cancelar la inscripción. Inténtalo de nuevo.", e);
        }
    }

    // El admin expulsa a un usuario de una actividad
    public void expulsar(int idUsuario, int idActividad) throws InscripcionException {
        try {
            Inscripcion inscripcion = obtenerInscripcionActiva(idUsuario, idActividad);

            inscripcionDAO.cambiarEstado(inscripcion.getIdInscripcion(), EstadoInscripcion.CANCELADA);
            actividadDAO.liberarPlaza(idActividad);

        } catch (SQLException e) {
            throw new InscripcionException("Error al expulsar al usuario. Inténtalo de nuevo.", e);
        }
    }

    // Pasar de PENDIENTE_PAGO a CONFIRMADA (lo llamará PagoService de la Persona 3)
    public void confirmarPago(int idUsuario, int idActividad) throws InscripcionException {
        try {
            Inscripcion inscripcion = obtenerInscripcionActiva(idUsuario, idActividad);

            if (!inscripcion.estaPendienteDePago()) {
                throw new InscripcionException("Esta inscripción no tiene ningún pago pendiente.");
            }

            inscripcionDAO.cambiarEstado(inscripcion.getIdInscripcion(), EstadoInscripcion.CONFIRMADA);

        } catch (SQLException e) {
            throw new InscripcionException("Error al confirmar el pago de la inscripción.", e);
        }
    }

    // Saber si un usuario está apuntado (para mostrar "Apuntarme" o "Cancelar")
    public boolean estaInscrito(int idUsuario, int idActividad) throws InscripcionException {
        try {
            Inscripcion i = inscripcionDAO.buscarPorUsuarioYActividad(idUsuario, idActividad);
            return i != null && i.estaActiva();
        } catch (SQLException e) {
            throw new InscripcionException("Error al consultar la inscripción.", e);
        }
    }
    // Inscripción activa de un usuario en una actividad (null si no tiene)
    public Inscripcion obtenerInscripcion(int idUsuario, int idActividad) throws InscripcionException {
        try {
            Inscripcion i = inscripcionDAO.buscarPorUsuarioYActividad(idUsuario, idActividad);
            return (i != null && i.estaActiva()) ? i : null;
        } catch (SQLException e) {
            throw new InscripcionException("Error al consultar la inscripción.", e);
        }
    }

    // Inscripciones de un usuario (pantalla "Mis inscripciones")
    public List<Inscripcion> listarMisInscripciones(int idUsuario) throws InscripcionException {
        try {
            return inscripcionDAO.listarPorUsuario(idUsuario);
        } catch (SQLException e) {
            throw new InscripcionException("Error al cargar tus inscripciones.", e);
        }
    }

    // Inscritos de una actividad (pantalla "Gestionar inscritos" del admin)
    public List<Inscripcion> listarInscritos(int idActividad) throws InscripcionException {
        try {
            return inscripcionDAO.listarPorActividad(idActividad);
        } catch (SQLException e) {
            throw new InscripcionException("Error al cargar los inscritos.", e);
        }
    }

    // Métodos privados

    // Busca la actividad o lanza error si no existe
    private Actividad obtenerActividad(int idActividad) throws SQLException, InscripcionException {
        Actividad actividad = actividadDAO.buscarPorId(idActividad);
        if (actividad == null) {
            throw new InscripcionException("La actividad no existe.");
        }
        return actividad;
    }

    // Busca la inscripción activa o lanza error si no la hay
    private Inscripcion obtenerInscripcionActiva(int idUsuario, int idActividad)
            throws SQLException, InscripcionException {
        Inscripcion inscripcion = inscripcionDAO.buscarPorUsuarioYActividad(idUsuario, idActividad);
        if (inscripcion == null || !inscripcion.estaActiva()) {
            throw new InscripcionException("No hay ninguna inscripción activa en esta actividad.");
        }
        return inscripcion;
    }
}