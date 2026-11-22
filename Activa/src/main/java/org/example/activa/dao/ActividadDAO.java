package org.example.activa.dao;

import org.example.activa.db.ConexionBD;
import org.example.activa.model.Actividad;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActividadDAO {

    // Consultas SQL de actividad
    private static final String SELECT_TODAS =
            "SELECT * FROM actividad ORDER BY fecha_actividad, hora_inicio";

    private static final String SELECT_PROXIMAS =
            "SELECT * FROM actividad WHERE fecha_actividad >= CURDATE() "
                    + "ORDER BY fecha_actividad, hora_inicio";

    private static final String SELECT_POR_ID =
            "SELECT * FROM actividad WHERE id_actividad = ?";

    private static final String INSERT =
            "INSERT INTO actividad (nombre_actividad, descripcion, lugar, fecha_actividad, "
                    + "hora_inicio, hora_fin, precio, cupo_actual, cupo_max, id_usuario, imagen) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE actividad SET nombre_actividad = ?, descripcion = ?, lugar = ?, "
                    + "fecha_actividad = ?, hora_inicio = ?, hora_fin = ?, precio = ?, cupo_max = ?, imagen = ? "
                    + "WHERE id_actividad = ?";

    private static final String DELETE =
            "DELETE FROM actividad WHERE id_actividad = ?";

    private static final String OCUPAR_PLAZA =
            "UPDATE actividad SET cupo_actual = cupo_actual + 1 "
                    + "WHERE id_actividad = ? AND cupo_actual < cupo_max";

    private static final String LIBERAR_PLAZA =
            "UPDATE actividad SET cupo_actual = cupo_actual - 1 "
                    + "WHERE id_actividad = ? AND cupo_actual > 0";

    // Consultas SQL de etiquetas
    private static final String SELECT_TODAS_ETIQUETAS =
            "SELECT id_actividad, etiqueta FROM actividad_etiqueta ORDER BY etiqueta";

    private static final String SELECT_ETIQUETAS_DE =
            "SELECT etiqueta FROM actividad_etiqueta WHERE id_actividad = ? ORDER BY etiqueta";

    private static final String DELETE_ETIQUETAS =
            "DELETE FROM actividad_etiqueta WHERE id_actividad = ?";

    private static final String INSERT_ETIQUETA =
            "INSERT INTO actividad_etiqueta (id_actividad, etiqueta) VALUES (?, ?)";


    // Listar todas (panel del admin)
    public List<Actividad> listarTodas() throws SQLException {
        return listar(SELECT_TODAS);
    }

    // Listar solo las que no han pasado (inicio del usuario)
    public List<Actividad> listarProximas() throws SQLException {
        return listar(SELECT_PROXIMAS);
    }

    // Buscar una por su id (null si no existe)
    public Actividad buscarPorId(int idActividad) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_POR_ID)) {

            ps.setInt(1, idActividad);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Actividad a = mapear(rs);
                a.setEtiquetas(leerEtiquetasDe(con, idActividad));
                return a;
            }
        }
    }

    // Insertar una nueva con sus etiquetas y devolver su id
    public int insertar(Actividad a) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
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
                ponerImagen(ps, 11, a.getImagen());
                ps.executeUpdate();

                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (!claves.next()) {
                        throw new SQLException("No se pudo obtener el id de la actividad creada.");
                    }
                    a.setIdActividad(claves.getInt(1));
                }

                guardarEtiquetas(con, a.getIdActividad(), a.getEtiquetas());
                con.commit();
                return a.getIdActividad();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // Modificar una existente y sus etiquetas
    public boolean actualizar(Actividad a) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(UPDATE)) {
                ps.setString(1, a.getNombreActividad());
                ps.setString(2, a.getDescripcion());
                ps.setString(3, a.getLugar());
                ps.setDate(4, Date.valueOf(a.getFechaActividad()));
                ps.setTime(5, Time.valueOf(a.getHoraInicio()));
                ps.setTime(6, Time.valueOf(a.getHoraFin()));
                ps.setBigDecimal(7, a.getPrecio());
                ps.setInt(8, a.getCupoMax());
                ponerImagen(ps, 9, a.getImagen());
                ps.setInt(10, a.getIdActividad());
                boolean cambiada = ps.executeUpdate() == 1;

                guardarEtiquetas(con, a.getIdActividad(), a.getEtiquetas());
                con.commit();
                return cambiada;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // Borrar una (sus inscripciones y etiquetas se borran solas por el CASCADE)
    public boolean eliminar(int idActividad) throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(DELETE)) {

            ps.setInt(1, idActividad);
            return ps.executeUpdate() == 1;
        }
    }

    // Sumar 1 al cupo (false si ya estaba llena)
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

    // Ejecuta una consulta de actividades y les añade sus etiquetas
    private List<Actividad> listar(String sql) throws SQLException {
        List<Actividad> lista = new ArrayList<>();
        try (Connection con = ConexionBD.getConexion()) {

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }

            // Todas las etiquetas de una vez y se reparten por id
            Map<Integer, Actividad> porId = new HashMap<>();
            for (Actividad a : lista) {
                porId.put(a.getIdActividad(), a);
            }
            try (PreparedStatement ps = con.prepareStatement(SELECT_TODAS_ETIQUETAS);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Actividad a = porId.get(rs.getInt("id_actividad"));
                    if (a != null) {
                        a.getEtiquetas().add(rs.getString("etiqueta"));
                    }
                }
            }
        }
        return lista;
    }

    // Etiquetas de una actividad
    private List<String> leerEtiquetasDe(Connection con, int idActividad) throws SQLException {
        List<String> etiquetas = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(SELECT_ETIQUETAS_DE)) {
            ps.setInt(1, idActividad);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    etiquetas.add(rs.getString("etiqueta"));
                }
            }
        }
        return etiquetas;
    }

    // Sustituye las etiquetas de una actividad por las nuevas
    private void guardarEtiquetas(Connection con, int idActividad, List<String> etiquetas) throws SQLException {
        try (PreparedStatement borrar = con.prepareStatement(DELETE_ETIQUETAS)) {
            borrar.setInt(1, idActividad);
            borrar.executeUpdate();
        }
        if (etiquetas == null || etiquetas.isEmpty()) {
            return;
        }
        try (PreparedStatement insertar = con.prepareStatement(INSERT_ETIQUETA)) {
            for (String etiqueta : etiquetas) {
                insertar.setInt(1, idActividad);
                insertar.setString(2, etiqueta);
                insertar.addBatch();
            }
            insertar.executeBatch();
        }
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
        a.setImagen(rs.getBytes("imagen"));
        return a;
    }
    // Pone la foto en la consulta (o NULL si no hay)
    private void ponerImagen(PreparedStatement ps, int posicion, byte[] imagen) throws SQLException {
        if (imagen == null || imagen.length == 0) {
            ps.setNull(posicion, Types.BLOB);
        } else {
            ps.setBytes(posicion, imagen);
        }
    }
}