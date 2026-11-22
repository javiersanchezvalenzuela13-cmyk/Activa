package org.example.activa.service;

import org.example.activa.dao.ActividadDAO;
import org.example.activa.dao.InscripcionDAO;
import org.example.activa.dao.PagoDAO;
import org.example.activa.exception.InscripcionException;
import org.example.activa.model.Actividad;
import org.example.activa.model.Inscripcion;
import org.example.activa.model.Inscrito;
import org.example.activa.model.Pago;
import org.example.activa.model.enums.EstadoInscripcion;
import org.example.activa.model.enums.EstadoPago;
import org.example.activa.model.enums.MetodoPago;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class InscripcionService {

    private final ActividadDAO actividadDAO = new ActividadDAO();
    private final InscripcionDAO inscripcionDAO = new InscripcionDAO();
    private final PagoDAO pagoDAO = new PagoDAO();

    // Apuntar a un usuario a una actividad y generar el pago pendiente si tiene coste
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

            // 4. Guardar la inscripción y, si es de pago, crear el registro en la tabla pago
            try {
                Inscripcion inscripcionGuardada;
                if (existente == null) {
                    Inscripcion nueva = new Inscripcion(idActividad, idUsuario, estado);
                    inscripcionDAO.insertar(nueva);
                    inscripcionGuardada = nueva;
                } else {
                    inscripcionDAO.cambiarEstado(existente.getIdInscripcion(), estado);
                    existente.setEstado(estado);
                    inscripcionGuardada = existente;
                }

                if (!actividad.esGratis()) {
                    Pago pagoPendiente = new Pago();
                    pagoPendiente.setIdInscripcion(inscripcionGuardada.getIdInscripcion());
                    pagoPendiente.setImporte(actividad.getPrecio().doubleValue());
                    pagoPendiente.setMetodoPago(MetodoPago.TARJETA);
                    pagoPendiente.setEstado(EstadoPago.PENDIENTE);
                    pagoDAO.insertarPago(pagoPendiente);
                }

                return inscripcionGuardada;
            } catch (SQLException e) {
                actividadDAO.liberarPlaza(idActividad);
                throw e;
            }

        } catch (SQLException e) {
            throw new InscripcionException("Error al guardar la inscripción. Inténtalo de nuevo.", e);
        }
    }


    // El usuario cancela su inscripción (no permite cancelar si ya está pagada)
    public void cancelar(int idUsuario, int idActividad) throws InscripcionException {
        try {
            Actividad actividad = obtenerActividad(idActividad);

            if (actividad.getFechaActividad().isBefore(LocalDate.now())) {
                throw new InscripcionException("No puedes cancelar una actividad que ya se ha celebrado.");
            }

            Inscripcion inscripcion = obtenerInscripcionActiva(idUsuario, idActividad);

            // Si tiene precio y ya está CONFIRMADA, es que el pago ya se realizó
            if (!actividad.esGratis() && inscripcion.getEstado() == EstadoInscripcion.CONFIRMADA) {
                throw new InscripcionException("No puedes cancelar una actividad que ya has pagado.");
            }

            // Si cancela antes de pagar, borramos el cobro pendiente para que desaparezca de Pagos
            if (inscripcion.estaPendienteDePago()) {
                pagoDAO.eliminarPagoPendiente(inscripcion.getIdInscripcion());
            }

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

    // Pasar de PENDIENTE_PAGO a CONFIRMADA
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

    // Inscritos de una actividad con nombre y email (pantalla "Gestionar inscritos")
    public List<Inscrito> listarInscritosConDatos(int idActividad) throws InscripcionException {
        try {
            return inscripcionDAO.listarInscritosConUsuario(idActividad);
        } catch (SQLException e) {
            throw new InscripcionException("Error al cargar los inscritos.", e);
        }
    }

    // Métodos privados

    private Actividad obtenerActividad(int idActividad) throws SQLException, InscripcionException {
        Actividad actividad = actividadDAO.buscarPorId(idActividad);
        if (actividad == null) {
            throw new InscripcionException("La actividad no existe.");
        }
        return actividad;
    }

    private Inscripcion obtenerInscripcionActiva(int idUsuario, int idActividad)
            throws SQLException, InscripcionException {
        Inscripcion inscripcion = inscripcionDAO.buscarPorUsuarioYActividad(idUsuario, idActividad);
        if (inscripcion == null || !inscripcion.estaActiva()) {
            throw new InscripcionException("No hay ninguna inscripción activa en esta actividad.");
        }
        return inscripcion;
    }
}