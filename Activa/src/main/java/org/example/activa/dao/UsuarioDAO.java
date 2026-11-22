package org.example.activa.dao;

import org.example.activa.db.ConexionBD;
import org.example.activa.model.Usuario;
import org.example.activa.model.enums.Rol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

public class UsuarioDAO {

    // Comprueba si un correo electrónico ya está registrado
    public boolean existeEmail(String email) {
        String sql = "SELECT id_usuario FROM usuario WHERE email = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Inserta un nuevo usuario y lo devuelve con su ID asignado
    public Usuario registrarUsuario(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre_usuario, email, contrasena) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, usuario.getNombreUsuario());
            pstmt.setString(2, usuario.getEmail());
            pstmt.setString(3, usuario.getContrasena());

            int filasAfectadas = pstmt.executeUpdate();
            if (filasAfectadas > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    usuario.setIdUsuario(rs.getInt(1));
                    usuario.setRol(Rol.USUARIO);
                    return usuario;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Busca y devuelve un usuario si sus credenciales coinciden
    public Usuario obtenerUsuarioPorCredenciales(String email, String contrasena) {
        String sql = "SELECT * FROM usuario WHERE email = ? AND contrasena = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, contrasena);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearUsuario(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Busca y devuelve un usuario a partir de su correo
    public Usuario obtenerUsuarioPorEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearUsuario(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Busca y devuelve un usuario completo a partir de su ID
    public Usuario obtenerUsuarioPorId(int idUsuario) {
        String sql = "SELECT * FROM usuario WHERE id_usuario = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearUsuario(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Actualiza los datos editables del perfil del usuario
    public boolean actualizarPerfil(int idUsuario, String nombreUsuario, String telefono, String sobreMi) {
        String sql = "UPDATE usuario SET nombre_usuario = ?, telefono = ?, sobre_mi = ? WHERE id_usuario = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombreUsuario);
            pstmt.setString(2, (telefono == null || telefono.isBlank()) ? null : telefono.trim());
            pstmt.setString(3, (sobreMi == null || sobreMi.isBlank()) ? null : sobreMi.trim());
            pstmt.setInt(4, idUsuario);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Actualiza la ruta/URI de la foto de perfil del usuario
    public boolean actualizarFotoPerfil(int idUsuario, String fotoPerfil) {
        String sql = "UPDATE usuario SET foto_perfil = ? WHERE id_usuario = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fotoPerfil);
            pstmt.setInt(2, idUsuario);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Elimina permanentemente un usuario de la base de datos
    public boolean eliminarUsuario(int idUsuario) {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Actualiza la contraseña de un usuario existente
    public boolean actualizarContrasena(String email, String nuevaContrasena) {
        String sql = "UPDATE usuario SET contrasena = ? WHERE email = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevaContrasena);
            pstmt.setString(2, email);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Guarda las etiquetas seleccionadas asociadas al ID del usuario
    public boolean guardarIntereses(int idUsuario, List<String> intereses) {
        String sqlInsert = "INSERT INTO usuario_etiqueta (id_usuario, etiqueta) VALUES (?, ?)";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {

            for (String interes : intereses) {
                pstmtInsert.setInt(1, idUsuario);
                pstmtInsert.setString(2, interes);
                pstmtInsert.addBatch();
            }
            pstmtInsert.executeBatch();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Convierte una fila de resultados SQL en un objeto Usuario
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setNombreUsuario(rs.getString("nombre_usuario"));
        u.setEmail(rs.getString("email"));
        u.setContrasena(rs.getString("contrasena"));
        u.setTelefono(rs.getString("telefono"));
        u.setSobreMi(rs.getString("sobre_mi"));
        u.setFotoPerfil(rs.getString("foto_perfil"));
        u.setRol(Rol.valueOf(rs.getString("rol")));

        Timestamp fecha = rs.getTimestamp("fecha_registro");
        if (fecha != null) {
            u.setFechaRegistro(fecha.toLocalDateTime());
        }
        return u;
    }
}