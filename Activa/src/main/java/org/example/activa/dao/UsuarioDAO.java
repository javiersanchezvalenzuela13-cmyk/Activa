package org.example.activa.dao;

import org.example.activa.db.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    // Comprueba si un correo ya existe en la base de datos
    public boolean existeEmail(String email) {
        String sql = "SELECT id_usuario FROM usuario WHERE email = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Retorna true si hay al menos un resultado

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Inserta un nuevo usuario en la tabla 'usuario'
    public boolean registrarUsuario(String nombreUsuario, String email, String contrasena) {
        // Se asigna el rol 'usuario' por defecto y la fecha actual
        String sql = "INSERT INTO usuario (nombre_usuario, email, contrasena, rol, fecha_registro) VALUES (?, ?, ?, 'usuario', CURRENT_TIMESTAMP)";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombreUsuario);
            pstmt.setString(2, email);
            pstmt.setString(3, contrasena);

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

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

            int filasAfectadas = pstmt.executeUpdate();
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Valida si el correo y la contraseña coinciden para el inicio de sesión
    public boolean validarCredenciales(String email, String contrasena) {
        String sql = "SELECT id_usuario FROM usuario WHERE email = ? AND contrasena = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, contrasena);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Retorna true si encuentra una coincidencia

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean guardarIntereses(String email, java.util.List<String> intereses) {
        String sqlGetId = "SELECT id_usuario FROM usuario WHERE email = ?";
        String sqlInsert = "INSERT INTO usuario_etiqueta (id_usuario, etiqueta) VALUES (?, ?)";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmtId = conn.prepareStatement(sqlGetId)) {

            // Buscar el ID del usuario
            pstmtId.setString(1, email);
            ResultSet rs = pstmtId.executeQuery();

            if (rs.next()) {
                int idUsuario = rs.getInt("id_usuario");

                // Insertar las etiquetas seleccionadas
                try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                    for (String interes : intereses) {
                        pstmtInsert.setInt(1, idUsuario);
                        pstmtInsert.setString(2, interes);
                        pstmtInsert.addBatch();
                    }
                    pstmtInsert.executeBatch();
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}