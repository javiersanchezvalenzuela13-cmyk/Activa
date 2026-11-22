package org.example.activa.dao;

import org.example.activa.db.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EtiquetaDAO {

    // Obtiene todas las etiquetas guardadas de un usuario ordenadas alfabéticamente
    public List<String> obtenerEtiquetasPorUsuario(int idUsuario) {
        List<String> etiquetas = new ArrayList<>();
        String sql = "SELECT etiqueta FROM usuario_etiqueta WHERE id_usuario = ? ORDER BY etiqueta ASC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                etiquetas.add(rs.getString("etiqueta"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return etiquetas;
    }

    // Elimina las etiquetas previas del usuario e inserta las nuevas seleccionadas
    public boolean guardarEtiquetasUsuario(int idUsuario, List<String> etiquetas) {
        String sqlDelete = "DELETE FROM usuario_etiqueta WHERE id_usuario = ?";
        String sqlInsert = "INSERT INTO usuario_etiqueta (id_usuario, etiqueta) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = ConexionBD.getConexion();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtDelete = conn.prepareStatement(sqlDelete)) {
                pstmtDelete.setInt(1, idUsuario);
                pstmtDelete.executeUpdate();
            }

            if (etiquetas != null && !etiquetas.isEmpty()) {
                try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                    for (String etiqueta : etiquetas) {
                        pstmtInsert.setInt(1, idUsuario);
                        pstmtInsert.setString(2, etiqueta);
                        pstmtInsert.addBatch();
                    }
                    pstmtInsert.executeBatch();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}