package org.example.activa.dao;

import org.example.activa.db.ConexionBD;
import org.example.activa.model.Inscrito;
import org.example.activa.model.enums.EstadoInscripcion;
import org.example.activa.model.Inscripcion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class InscripcionDAO {

    // Consultas SQL
    private static final String SELECT_POR_USUARIO_Y_ACTIVIDAD =
            "SELECT * FROM inscripcion WHERE id_usuario = ? AND id_actividad = ?";

    private static final String SELECT_POR_USUARIO =
            "SELECT * FROM inscripcion WHERE id_usuario = ? ORDER BY fecha_inscripcion DESC";

    private static final String SELECT_POR_ACTIVIDAD =
            "SELECT * FROM inscripcion WHERE id_actividad = ? ORDER BY fecha_inscripcion";

    private static final String INSERT =
            "INSERT INTO inscripcion (estado_inscripcion, id_actividad, id_usuario) VALUES (?, ?, ?)";

    private static final String UPDATE_ESTADO =
            "UPDATE inscripcion SET estado_inscripcion = ? WHERE id_inscripcion = ?";

    private static final String DELETE =
            "DELETE FROM inscripcion WHERE id_inscripcion = ?";
    private static final String SELECT_INSCRITOS_CON_USUARIO =
            "SELECT i.id_inscripcion, i.id_usuario, i.fecha_inscripcion, i.estado_inscripcion, "
                    + "u.nombre_usuario, u.email "
                    + "FROM inscripcion i JOIN usuario u ON u.id_usuario = i.id_usuario "
                    + "WHERE i.id_actividad = ? "
                    + "ORDER BY i.fecha_inscripcion";

    // Buscar la inscripción de un usuario en una actividad (null si no tiene)
    public Inscripcion buscarPorUsuarioYActividad(int idUsuario, int idActividad) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_POR_USUARIO_Y_ACTIVIDAD)) {

            ps.setInt(1, idUsuario);
            ps.setInt(2, idActividad);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    // Todas las inscripciones de un usuario (pantalla "Mis inscripciones")
    public List<Inscripcion> listarPorUsuario(int idUsuario) throws SQLException {
        return listar(SELECT_POR_USUARIO, idUsuario);
    }

    // Todas las inscripciones de una actividad (pantalla "Gestionar inscritos" del admin)
    public List<Inscripcion> listarPorActividad(int idActividad) throws SQLException {
        return listar(SELECT_POR_ACTIVIDAD, idActividad);
    }
    // Inscritos de una actividad con su nombre y email (pantalla "Gestionar inscritos")
    public List<Inscrito> listarInscritosConUsuario(int idActividad) throws SQLException {
        List<Inscrito> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_INSCRITOS_CON_USUARIO)) {

            ps.setInt(1, idActividad);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp fecha = rs.getTimestamp("fecha_inscripcion");
                    lista.add(new Inscrito(
                            rs.getInt("id_inscripcion"),
                            rs.getInt("id_usuario"),
                            rs.getString("nombre_usuario"),
                            rs.getString("email"),
                            fecha == null ? null : fecha.toLocalDateTime(),
                            EstadoInscripcion.valueOf(rs.getString("estado_inscripcion"))));
                }
            }
        }
        return lista;
    }

    // Insertar una nueva y devolver el id que le ha dado MySQL
    public int insertar(Inscripcion i) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, i.getEstado().name());
            ps.setInt(2, i.getIdActividad());
            ps.setInt(3, i.getIdUsuario());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    int id = claves.getInt(1);
                    i.setIdInscripcion(id);
                    return id;
                }
            }
            throw new SQLException("No se pudo obtener el id de la inscripción creada.");
        }
    }

    // Cambiar el estado (cancelar, confirmar tras el pago, volver a apuntarse)
    public boolean cambiarEstado(int idInscripcion, EstadoInscripcion nuevoEstado) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(UPDATE_ESTADO)) {

            ps.setString(1, nuevoEstado.name());
            ps.setInt(2, idInscripcion);
            return ps.executeUpdate() == 1;
        }
    }

    // Borrar una inscripción (expulsar del todo)
    public boolean eliminar(int idInscripcion) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(DELETE)) {

            ps.setInt(1, idInscripcion);
            return ps.executeUpdate() == 1;
        }
    }

    // Métodos privados

    // Ejecuta una consulta con un parámetro entero y devuelve la lista
    private List<Inscripcion> listar(String sql, int id) throws SQLException {
        List<Inscripcion> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    // Convierte una fila del ResultSet en un objeto Inscripcion
    private Inscripcion mapear(ResultSet rs) throws SQLException {
        Inscripcion i = new Inscripcion();
        i.setIdInscripcion(rs.getInt("id_inscripcion"));

        Timestamp fecha = rs.getTimestamp("fecha_inscripcion");
        if (fecha != null) {
            i.setFechaInscripcion(fecha.toLocalDateTime());
        }

        i.setEstado(EstadoInscripcion.valueOf(rs.getString("estado_inscripcion")));
        i.setIdActividad(rs.getInt("id_actividad"));
        i.setIdUsuario(rs.getInt("id_usuario"));
        return i;
    }
}