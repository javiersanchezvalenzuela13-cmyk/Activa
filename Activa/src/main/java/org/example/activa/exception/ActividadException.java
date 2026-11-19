package org.example.activa.exception;

public class ActividadException extends Exception {

    // Constructor con mensaje
    public ActividadException(String mensaje) {
        super(mensaje);
    }

    // Constructor con mensaje y causa original
    public ActividadException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}