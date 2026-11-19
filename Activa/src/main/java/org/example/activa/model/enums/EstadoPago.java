package org.example.activa.model.enums;
//ENUM con los posibles estados que puede tener un pago
public enum EstadoPago {
    PENDIENTE("Pendiente"),
    COMPLETADO("Completado"),
    REEMBOLSADO("Reembolsado");

    private final String etiqueta;


    EstadoPago(String etiqueta) {
        this.etiqueta = etiqueta;
    }
    public String getEtiqueta() {
        return etiqueta;
    }
    @Override
    public String toString() {
        return etiqueta;
    }
}