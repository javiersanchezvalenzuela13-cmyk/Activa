package org.example.activa.service;
import org.example.activa.dao.PagoDAO;
import org.example.activa.model.Pago;
import org.example.activa.model.enums.EstadoPago;
import org.example.activa.model.enums.MetodoPago;
import java.time.LocalDateTime;
import java.util.List;

// Clase con la  lógica de negocio para gestionar los pagos del usuario
public class PagoService {
    private final PagoDAO pagoDAO;
    public PagoService() {
        this.pagoDAO = new PagoDAO();
    }

    // Devuelve todos los pagos asociados a un usuario
    public List<Pago> obtenerPagosDeUsuario(int idUsuario) {
        return pagoDAO.listarPorUsuario(idUsuario);
    }

    // Valida y registra un nuevo pago, si no trae estado, lo crea como pendiente
    public boolean registrarPago(Pago pago) {
        if (pago == null || pago.getImporte() <= 0) {
            return false;
        }
        if (pago.getEstado() == null) {
            pago.setEstado(EstadoPago.PENDIENTE);
        }
        if (pago.getEstado() == EstadoPago.COMPLETADO && pago.getFechaPago() == null) {
            pago.setFechaPago(LocalDateTime.now());
        }
        return pagoDAO.insertarPago(pago);
    }

    // Valida que se haya elegido un metodo de pago y lo marca como completado
    public boolean abonarPagoPendiente(int idPago, MetodoPago metodo) {
        if (idPago <= 0 || metodo == null) {
            return false;
        }
        return pagoDAO.completarPago(idPago, metodo);
    }

    // Suma el importe de todos los pagos que están en estado pendiente
    public double calcularTotalPendiente(List<Pago> pagos) {
        double total = 0.0;
        for (Pago p : pagos) {
            if (p.getEstado() == EstadoPago.PENDIENTE) {
                total += p.getImporte();
            }
        }
        return total;
    }

    // Suma el importe de todos los pagos ya completados
    public double calcularTotalPagado(List<Pago> pagos) {
        double total = 0.0;
        for (Pago p : pagos) {
            if (p.getEstado() == EstadoPago.COMPLETADO) {
                total += p.getImporte();
            }
        }
        return total;
    }
}