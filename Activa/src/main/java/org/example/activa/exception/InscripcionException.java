package org.example.activa.exception;

public class InscripcionException extends Exception {
    // Constructor con mensaje
    public InscripcionException(String mensaje) {
        super(mensaje);
    }
    // Constructor con mensaje y causa original
    public InscripcionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}