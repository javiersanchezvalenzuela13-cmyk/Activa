package org.example.activa.dao;

import org.example.activa.db.ConexionBD;
import org.example.activa.model.MetodoPago;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MetodoPagoDAO {

    // Devuelve todos los métodos de pago de un usuario ordenados por predeterminado
    public List<MetodoPago> obtenerMetodosPorUsuario(int idUsuario) {
        List<MetodoPago> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuario_metodo_pago WHERE id_usuario = ? ORDER BY es_predeterminado DESC, id_metodo_pago DESC";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                lista.add(mapearMetodoPago(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Inserta un nuevo método de pago y actualiza el predeterminado si corresponde
    public boolean agregarMetodoPago(MetodoPago metodo) {
        String sqlReset = "UPDATE usuario_metodo_pago SET es_predeterminado = FALSE WHERE id_usuario = ?";
        String sqlInsert = "INSERT INTO usuario_metodo_pago (id_usuario, tipo, titular, identificador, fecha_caducidad, es_predeterminado) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConexion()) {
            boolean esPrimero = obtenerMetodosPorUsuario(metodo.getIdUsuario()).isEmpty();
            boolean marcarPredeterminado = metodo.isEsPredeterminado() || esPrimero;

            if (marcarPredeterminado) {
                try (PreparedStatement pstmtReset = conn.prepareStatement(sqlReset)) {
                    pstmtReset.setInt(1, metodo.getIdUsuario());
                    pstmtReset.executeUpdate();
                }
            }

            try (PreparedStatement pstmtInsert = conn.prepareStatement(sqlInsert)) {
                pstmtInsert.setInt(1, metodo.getIdUsuario());
                pstmtInsert.setString(2, metodo.getTipo());
                pstmtInsert.setString(3, metodo.getTitular());
                pstmtInsert.setString(4, metodo.getIdentificador());
                pstmtInsert.setString(5, metodo.getFechaCaducidad());
                pstmtInsert.setBoolean(6, marcarPredeterminado);

                return pstmtInsert.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Marca un método de pago concreto como predeterminado para el usuario
    public boolean establecerPredeterminado(int idUsuario, int idMetodoPago) {
        String sqlReset = "UPDATE usuario_metodo_pago SET es_predeterminado = FALSE WHERE id_usuario = ?";
        String sqlSet = "UPDATE usuario_metodo_pago SET es_predeterminado = TRUE WHERE id_metodo_pago = ? AND id_usuario = ?";

        try (Connection conn = ConexionBD.getConexion()) {
            try (PreparedStatement pstmtReset = conn.prepareStatement(sqlReset)) {
                pstmtReset.setInt(1, idUsuario);
                pstmtReset.executeUpdate();
            }

            try (PreparedStatement pstmtSet = conn.prepareStatement(sqlSet)) {
                pstmtSet.setInt(1, idMetodoPago);
                pstmtSet.setInt(2, idUsuario);
                return pstmtSet.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Elimina un método de pago guardado por su ID
    public boolean eliminarMetodoPago(int idMetodoPago, int idUsuario) {
        String sql = "DELETE FROM usuario_metodo_pago WHERE id_metodo_pago = ? AND id_usuario = ?";
        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idMetodoPago);
            pstmt.setInt(2, idUsuario);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Convierte una fila de resultados SQL en un objeto MetodoPago
    private MetodoPago mapearMetodoPago(ResultSet rs) throws SQLException {
        MetodoPago m = new MetodoPago();
        m.setIdMetodoPago(rs.getInt("id_metodo_pago"));
        m.setIdUsuario(rs.getInt("id_usuario"));
        m.setTipo(rs.getString("tipo"));
        m.setTitular(rs.getString("titular"));
        m.setIdentificador(rs.getString("identificador"));
        m.setFechaCaducidad(rs.getString("fecha_caducidad"));
        m.setEsPredeterminado(rs.getBoolean("es_predeterminado"));
        return m;
    }
}