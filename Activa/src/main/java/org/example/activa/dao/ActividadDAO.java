package org.example.activa.dao;

import org.example.activa.db.ConexionBD;
import org.example.activa.model.Actividad;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class ActividadDAO {

    // Consultas SQL
    private static final String SELECT_TODAS =
            "SELECT * FROM actividad ORDER BY fecha_actividad, hora_inicio";

    private static final String SELECT_PROXIMAS =
            "SELECT * FROM actividad WHERE fecha_actividad >= CURDATE() "
                    + "ORDER BY fecha_actividad, hora_inicio";

    private static final String SELECT_POR_ID =
            "SELECT * FROM actividad WHERE id_actividad = ?";

    private static final String INSERT =
            "INSERT INTO actividad (nombre_actividad, descripcion, lugar, fecha_actividad, "
                    + "hora_inicio, hora_fin, precio, cupo_actual, cupo_max, id_usuario) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE actividad SET nombre_actividad = ?, descripcion = ?, lugar = ?, "
                    + "fecha_actividad = ?, hora_inicio = ?, hora_fin = ?, precio = ?, cupo_max = ? "
                    + "WHERE id_actividad = ?";

    private static final String DELETE =
            "DELETE FROM actividad WHERE id_actividad = ?";

    private static final String OCUPAR_PLAZA =
            "UPDATE actividad SET cupo_actual = cupo_actual + 1 "
                    + "WHERE id_actividad = ? AND cupo_actual < cupo_max";

    private static final String LIBERAR_PLAZA =
            "UPDATE actividad SET cupo_actual = cupo_actual - 1 "
                    + "WHERE id_actividad = ? AND cupo_actual > 0";

    // Listar todas (para el panel de admin)
    public List<Actividad> listarTodas() throws SQLException {
        return listar(SELECT_TODAS);
    }

    // Listar solo las que no han pasado (para el inicio del usuario)
    public List<Actividad> listarProximas() throws SQLException {
        return listar(SELECT_PROXIMAS);
    }

    // Buscar una por su id (devuelve null si no existe)
    public Actividad buscarPorId(int idActividad) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_POR_ID)) {

            ps.setInt(1, idActividad);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    // Insertar una nueva y devolver el id que le ha dado MySQL
    public int insertar(Actividad a) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getNombreActividad());
            ps.setString(2, a.getDescripcion());
            ps.setString(3, a.getLugar());
            ps.setDate(4, Date.valueOf(a.getFechaActividad()));
            ps.setTime(5, Time.valueOf(a.getHoraInicio()));
            ps.setTime(6, Time.valueOf(a.getHoraFin()));
            ps.setBigDecimal(7, a.getPrecio());
            ps.setInt(8, a.getCupoActual());
            ps.setInt(9, a.getCupoMax());
            ps.setInt(10, a.getIdUsuario());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    int id = claves.getInt(1);
                    a.setIdActividad(id);
                    return id;
                }
            }
            throw new SQLException("No se pudo obtener el id de la actividad creada.");
        }
    }

    // Modificar una existente (devuelve true si se ha actualizado)
    public boolean actualizar(Actividad a) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {

            ps.setString(1, a.getNombreActividad());
            ps.setString(2, a.getDescripcion());
            ps.setString(3, a.getLugar());
            ps.setDate(4, Date.valueOf(a.getFechaActividad()));
            ps.setTime(5, Time.valueOf(a.getHoraInicio()));
            ps.setTime(6, Time.valueOf(a.getHoraFin()));
            ps.setBigDecimal(7, a.getPrecio());
            ps.setInt(8, a.getCupoMax());
            ps.setInt(9, a.getIdActividad());
            return ps.executeUpdate() == 1;
        }
    }

    // Borrar una (sus inscripciones se borran solas por el ON DELETE CASCADE)
    public boolean eliminar(int idActividad) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(DELETE)) {

            ps.setInt(1, idActividad);
            return ps.executeUpdate() == 1;
        }
    }

    // Sumar 1 al cupo (devuelve false si ya estaba llena)
    public boolean ocuparPlaza(int idActividad) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(OCUPAR_PLAZA)) {

            ps.setInt(1, idActividad);
            return ps.executeUpdate() == 1;
        }
    }

    // Restar 1 al cupo (cuando alguien cancela)
    public boolean liberarPlaza(int idActividad) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(LIBERAR_PLAZA)) {

            ps.setInt(1, idActividad);
            return ps.executeUpdate() == 1;
        }
    }

    // Métodos privados

    // Ejecuta una consulta sin parámetros y devuelve la lista
    private List<Actividad> listar(String sql) throws SQLException {
        List<Actividad> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // Convierte una fila del ResultSet en un objeto Actividad
    private Actividad mapear(ResultSet rs) throws SQLException {
        Actividad a = new Actividad();
        a.setIdActividad(rs.getInt("id_actividad"));
        a.setNombreActividad(rs.getString("nombre_actividad"));
        a.setDescripcion(rs.getString("descripcion"));
        a.setLugar(rs.getString("lugar"));
        a.setFechaActividad(rs.getDate("fecha_actividad").toLocalDate());
        a.setHoraInicio(rs.getTime("hora_inicio").toLocalTime());
        a.setHoraFin(rs.getTime("hora_fin").toLocalTime());
        a.setPrecio(rs.getBigDecimal("precio"));
        a.setCupoActual(rs.getInt("cupo_actual"));
        a.setCupoMax(rs.getInt("cupo_max"));
        a.setIdUsuario(rs.getInt("id_usuario"));
        return a;
    }
}