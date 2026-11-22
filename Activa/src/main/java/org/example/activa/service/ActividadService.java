package org.example.activa.service;

import org.example.activa.dao.ActividadDAO;
import org.example.activa.exception.ActividadException;
import org.example.activa.model.Actividad;
import org.example.activa.util.Etiquetas;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ActividadService {

    // Límites según la tabla
    private static final int MAX_NOMBRE = 100;
    private static final int MAX_LUGAR = 100;
    private static final BigDecimal PRECIO_MAX = new BigDecimal("9999.99");
    // Tamaño máximo de la foto (2 MB)
    public static final long TAMANO_MAX_IMAGEN = 2 * 1024 * 1024;

    private final ActividadDAO actividadDAO = new ActividadDAO();

    // Actividades que aún no han pasado (inicio del usuario)
    public List<Actividad> listarProximas() throws ActividadException {
        try {
            return actividadDAO.listarProximas();
        } catch (SQLException e) {
            throw new ActividadException("Error al cargar las actividades.", e);
        }
    }

    // Todas las actividades (panel del admin)
    public List<Actividad> listarTodas() throws ActividadException {
        try {
            return actividadDAO.listarTodas();
        } catch (SQLException e) {
            throw new ActividadException("Error al cargar las actividades.", e);
        }
    }

    // Una actividad por su id
    public Actividad buscarPorId(int idActividad) throws ActividadException {
        try {
            Actividad actividad = actividadDAO.buscarPorId(idActividad);
            if (actividad == null) {
                throw new ActividadException("La actividad no existe.");
            }
            return actividad;
        } catch (SQLException e) {
            throw new ActividadException("Error al cargar la actividad.", e);
        }
    }

    // Crear una actividad nueva
    public Actividad crear(Actividad actividad) throws ActividadException {
        validar(actividad);

        if (actividad.getFechaActividad().isBefore(LocalDate.now())) {
            throw new ActividadException("La fecha no puede ser anterior a hoy.");
        }

        actividad.setCupoActual(0);

        try {
            actividadDAO.insertar(actividad);
            return actividad;
        } catch (SQLException e) {
            throw new ActividadException("Error al crear la actividad.", e);
        }
    }

    // Modificar una actividad existente
    public void editar(Actividad actividad) throws ActividadException {
        validar(actividad);

        Actividad original = buscarPorId(actividad.getIdActividad());

        if (actividad.getCupoMax() < original.getCupoActual()) {
            throw new ActividadException("No puedes poner menos plazas que personas inscritas ("
                    + original.getCupoActual() + ").");
        }

        try {
            actividadDAO.actualizar(actividad);
        } catch (SQLException e) {
            throw new ActividadException("Error al guardar los cambios.", e);
        }
    }

    // Borrar una actividad (sus inscripciones se borran también)
    public void eliminar(int idActividad) throws ActividadException {
        try {
            if (!actividadDAO.eliminar(idActividad)) {
                throw new ActividadException("La actividad no existe.");
            }
        } catch (SQLException e) {
            throw new ActividadException("Error al eliminar la actividad.", e);
        }
    }

    // Métodos privados

    // Comprueba que los datos del formulario son correctos
    private void validar(Actividad a) throws ActividadException {
        if (a.getNombreActividad() == null || a.getNombreActividad().isBlank()) {
            throw new ActividadException("El nombre es obligatorio.");
        }
        if (a.getNombreActividad().length() > MAX_NOMBRE) {
            throw new ActividadException("El nombre no puede tener más de " + MAX_NOMBRE + " caracteres.");
        }
        if (a.getLugar() == null || a.getLugar().isBlank()) {
            throw new ActividadException("El lugar es obligatorio.");
        }
        if (a.getLugar().length() > MAX_LUGAR) {
            throw new ActividadException("El lugar no puede tener más de " + MAX_LUGAR + " caracteres.");
        }
        if (a.getFechaActividad() == null) {
            throw new ActividadException("La fecha es obligatoria.");
        }
        if (a.getHoraInicio() == null || a.getHoraFin() == null) {
            throw new ActividadException("Las horas de inicio y fin son obligatorias.");
        }
        if (!a.getHoraFin().isAfter(a.getHoraInicio())) {
            throw new ActividadException("La hora de fin debe ser posterior a la de inicio.");
        }
        if (a.getPrecio() == null || a.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new ActividadException("El precio no puede ser negativo.");
        }
        if (a.getPrecio().compareTo(PRECIO_MAX) > 0) {
            throw new ActividadException("El precio no puede ser mayor de 9999,99 €.");
        }
        if (a.getCupoMax() <= 0) {
            throw new ActividadException("Debe haber al menos 1 plaza.");
        }
        if (a.getEtiquetas().size() > Etiquetas.MAXIMO_POR_ACTIVIDAD) {
            throw new ActividadException("Puedes poner como máximo "
                    + Etiquetas.MAXIMO_POR_ACTIVIDAD + " etiquetas.");
        }
        if (a.tieneImagen() && a.getImagen().length > TAMANO_MAX_IMAGEN) {
            throw new ActividadException("La foto no puede pesar más de 2 MB.");
        }
    }
}