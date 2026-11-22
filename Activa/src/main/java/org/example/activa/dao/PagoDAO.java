package org.example.activa.dao;

import org.example.activa.db.ConexionBD;
import org.example.activa.model.Pago;
import org.example.activa.model.enums.EstadoInscripcion;
import org.example.activa.model.enums.EstadoPago;
import org.example.activa.model.enums.MetodoPago;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Clase DAO que se encarga de las operaciones CRUD de la tabla pago
public class PagoDAO {

    // Lista todos los pagos de un usuario junto con el nombre de cada actividad
    public List<Pago> listarPorUsuario(int idUsuario) {
        List<Pago> lista = new ArrayList<>();
        String sql = """
                SELECT p.id_pago, p.id_inscripcion, p.total, p.metodo_pago, p.estado_pago, p.fecha_pago,
                       a.nombre_actividad AS titulo_actividad
                FROM pago p
                INNER JOIN inscripcion i ON p.id_inscripcion = i.id_inscripcion
                INNER JOIN actividad a ON i.id_actividad = a.id_actividad
                WHERE i.id_usuario = ?
                ORDER BY p.id_pago DESC
                """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pago pago = new Pago();
                    pago.setIdPago(rs.getInt("id_pago"));
                    pago.setIdInscripcion(rs.getInt("id_inscripcion"));
                    pago.setImporte(rs.getDouble("total"));

                    String metodoStr = rs.getString("metodo_pago");
                    if (metodoStr != null) {
                        pago.setMetodoPago(MetodoPago.valueOf(metodoStr.toUpperCase()));
                    }

                    String estadoStr = rs.getString("estado_pago");
                    if (estadoStr != null) {
                        pago.setEstado(EstadoPago.valueOf(estadoStr.toUpperCase()));
                    }

                    Timestamp ts = rs.getTimestamp("fecha_pago");
                    if (ts != null) {
                        pago.setFechaPago(ts.toLocalDateTime());
                    }

                    pago.setTituloActividad(rs.getString("titulo_actividad"));
                    lista.add(pago);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Guarda un nuevo registro de pago en la base de datos
    public boolean insertarPago(Pago pago) {
        String sql = """
                INSERT INTO pago (id_inscripcion, total, metodo_pago, estado_pago, fecha_pago)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pago.getIdInscripcion());
            ps.setDouble(2, pago.getImporte());
            ps.setString(3, pago.getMetodoPago() != null ? pago.getMetodoPago().name() : MetodoPago.TARJETA.name());
            ps.setString(4, pago.getEstado().name());

            if (pago.getFechaPago() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(pago.getFechaPago()));
            } else {
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Marca un pago pendiente como completado y confirma la inscripción asociada
    public boolean completarPago(int idPago, MetodoPago metodo) {
        String sqlPago = """
                UPDATE pago
                SET estado_pago = ?, metodo_pago = ?, fecha_pago = ?
                WHERE id_pago = ?
                """;
        String sqlInscripcion = """
                UPDATE inscripcion i
                INNER JOIN pago p ON i.id_inscripcion = p.id_inscripcion
                SET i.estado_inscripcion = ?
                WHERE p.id_pago = ?
                """;

        try (Connection conn = ConexionBD.getConexion()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psPago = conn.prepareStatement(sqlPago);
                 PreparedStatement psInsc = conn.prepareStatement(sqlInscripcion)) {

                psPago.setString(1, EstadoPago.COMPLETADO.name());
                psPago.setString(2, metodo.name());
                psPago.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                psPago.setInt(4, idPago);
                int filasPago = psPago.executeUpdate();

                psInsc.setString(1, EstadoInscripcion.CONFIRMADA.name());
                psInsc.setInt(2, idPago);
                psInsc.executeUpdate();

                conn.commit();
                return filasPago > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }
    // Elimina el pago pendiente si el usuario cancela la inscripción antes de abonarla
    public boolean eliminarPagoPendiente(int idInscripcion) {
        String sql = "DELETE FROM pago WHERE id_inscripcion = ? AND estado_pago = 'PENDIENTE'";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idInscripcion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}