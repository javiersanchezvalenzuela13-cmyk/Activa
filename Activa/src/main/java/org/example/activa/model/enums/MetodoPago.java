package org.example.activa.model.enums;
//ENUM con los tipos de pago disponibles
public enum MetodoPago {
    TARJETA("Tarjeta"),
    BIZUM("Bizum"),
    TRANSFERENCIA("Transferencia");

    private final String etiqueta;
    MetodoPago(String etiqueta) {
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