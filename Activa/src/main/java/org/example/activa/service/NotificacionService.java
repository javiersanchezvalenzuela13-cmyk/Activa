package org.example.activa.service;

import org.example.activa.dao.ActividadDAO;
import org.example.activa.exception.InscripcionException;
import org.example.activa.model.Actividad;
import org.example.activa.model.Inscripcion;
import org.example.activa.model.Notificacion;
import org.example.activa.model.Pago;
import org.example.activa.model.enums.EstadoPago;
import org.example.activa.util.Sesion;
import org.example.activa.util.Vista;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Servicio que recopila los avisos del usuario (actividades de mañana y estado de pagos)
public class NotificacionService {

    private final InscripcionService inscripcionService = new InscripcionService();
    private final PagoService pagoService = new PagoService();
    private final ActividadDAO actividadDAO = new ActividadDAO();

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("d MMM", new Locale("es", "ES"));
    private static final Locale LOCALE_ES = new Locale("es", "ES");

    // Genera la lista de notificaciones del usuario que tiene la sesión iniciada
    public List<Notificacion> obtenerParaUsuarioActual() {
        List<Notificacion> lista = new ArrayList<>();
        if (!Sesion.haySesion()) {
            return lista;
        }

        int idUsuario = Sesion.getIdUsuario();
        LocalDate manana = LocalDate.now().plusDays(1);
        Set<Integer> pendientesAvisados = new HashSet<>();

        // 1. Actividades inscritas: recordatorio de mañana y pagos pendientes de inscripción
        try {
            for (Inscripcion ins : inscripcionService.listarMisInscripciones(idUsuario)) {
                if (!ins.estaActiva()) {
                    continue;
                }

                Actividad act = actividadDAO.buscarPorId(ins.getIdActividad());
                if (act == null) {
                    continue;
                }

                // Aviso si la actividad es mañana
                if (manana.equals(act.getFechaActividad())) {
                    String hora = act.getHoraInicio() != null ? act.getHoraInicio().format(FORMATO_HORA) : "";
                    String lugar = act.getLugar() != null ? act.getLugar() : "";
                    String detalle = act.getNombreActividad()
                            + (hora.isEmpty() ? "" : " · " + hora)
                            + (lugar.isEmpty() ? "" : " · " + lugar);

                    lista.add(new Notificacion(
                            "Mañana tienes plan",
                            detalle,
                            "mañana",
                            "clock",
                            Vista.MIS_INSCRIPCIONES
                    ));
                }

                // Aviso si la inscripción está pendiente de pago
                if (ins.estaPendienteDePago()) {
                    BigDecimal precio = act.getPrecio() != null ? act.getPrecio() : BigDecimal.ZERO;
                    String detalle = act.getNombreActividad() + " · " + String.format(LOCALE_ES, "%.2f €", precio);

                    lista.add(new Notificacion(
                            "Pago pendiente",
                            detalle,
                            formatearTiempo(ins.getFechaInscripcion(), "pendiente"),
                            "alertCircle",
                            Vista.PAGOS
                    ));
                    pendientesAvisados.add(ins.getIdInscripcion());
                }
            }
        } catch (InscripcionException | SQLException e) {
            e.printStackTrace();
        }

        // 2. Pagos registrados en la tabla pago
        for (Pago pago : pagoService.obtenerPagosDeUsuario(idUsuario)) {
            String actividad = pago.getTituloActividad() != null ? pago.getTituloActividad() : "Actividad";
            String detalle = actividad + " · " + String.format(LOCALE_ES, "%.2f €", pago.getImporte());

            if (pago.getEstado() == EstadoPago.PENDIENTE
                    && !pendientesAvisados.contains(pago.getIdInscripcion())) {
                lista.add(new Notificacion(
                        "Pago pendiente",
                        detalle,
                        formatearTiempo(pago.getFechaPago(), "pendiente"),
                        "alertCircle",
                        Vista.PAGOS
                ));
            } else if (pago.getEstado() == EstadoPago.COMPLETADO) {
                lista.add(new Notificacion(
                        "Pago realizado",
                        detalle,
                        formatearTiempo(pago.getFechaPago(), "pagado"),
                        "check",
                        Vista.PAGOS
                ));
            }
        }

        return lista;
    }

    // Comprueba si existen notificaciones para mostrar el punto naranja en la campana
    public boolean hayNotificaciones() {
        return !obtenerParaUsuarioActual().isEmpty();
    }

    // Convierte la fecha en un texto corto ("hoy", "ayer" o "13 nov")
    private String formatearTiempo(LocalDateTime fechaHora, String porDefecto) {
        if (fechaHora == null) {
            return porDefecto;
        }
        long dias = ChronoUnit.DAYS.between(fechaHora.toLocalDate(), LocalDate.now());
        if (dias == 0) {
            return "hoy";
        } else if (dias == 1) {
            return "ayer";
        }
        return fechaHora.format(FORMATO_FECHA);
    }
}