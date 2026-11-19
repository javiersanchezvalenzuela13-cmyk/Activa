package org.example.activa.dao;

import org.example.activa.db.ConexionBD;
import org.example.activa.model.Pago;
import org.example.activa.model.enums.EstadoPago;
import org.example.activa.model.enums.MetodoPago;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//Clase DAO que se encarga de las operaciones CRUD del pago
public class PagoDAO {

    //Al insertar el id de un usuario muestra todos los pagos de este y la actividad de cada uno
    public List<Pago> listarPorUsuario(int idUsuario) {
        List<Pago> lista = new ArrayList<>();
        String sql = """
                SELECT p.id_pago, p.id_inscripcion, p.importe, p.metodo_pago, p.estado, p.fecha_pago, a.titulo AS titulo_actividad
                FROM pago p
                INNER JOIN inscripcion i ON p.id_inscripcion = i.id_inscripcion
                INNER JOIN actividad a ON i.id_actividad = a.id_actividad
                WHERE i.id_usuario = ?
                ORDER BY p.id_pago DESC""";

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pago pago = new Pago();
                    pago.setIdPago(rs.getInt("id_pago"));
                    pago.setIdInscripcion(rs.getInt("id_inscripcion"));
                    pago.setImporte(rs.getDouble("importe"));

                    String metodoStr = rs.getString("metodo_pago");
                    if (metodoStr != null) {
                        pago.setMetodoPago(MetodoPago.valueOf(metodoStr.toUpperCase()));
                    }

                    String estadoStr = rs.getString("estado");
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

    //Metodo para guardar un nuevo pago
    public boolean insertarPago(Pago pago) {
        String sql = """
                INSERT INTO pago (id_inscripcion, importe, metodo_pago, estado, fecha_pago)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pago.getIdInscripcion());
            ps.setDouble(2, pago.getImporte());
            //Si el pago ya tiene metodo elegido pone el nombre con name(), si es null muestra null
            ps.setString(3, pago.getMetodoPago() != null ? pago.getMetodoPago().name() : null);
            ps.setString(4, pago.getEstado().name());
            //Un if q comprueba si existe fecha de pago para mostrarlo, si no null
            if (pago.getFechaPago() != null) {
                ps.setTimestamp(5, Timestamp.valueOf(pago.getFechaPago()));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Metodo que marca un pago pendiente como completado y le añade una fecha
    public boolean completarPago(int idPago, MetodoPago metodo) {
        String sql = """
                UPDATE pago
                SET estado = ?, metodo_pago = ?, fecha_pago = ?
                WHERE id_pago = ?
                """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, EstadoPago.COMPLETADO.name());
            ps.setString(2, metodo.name());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(4, idPago);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}