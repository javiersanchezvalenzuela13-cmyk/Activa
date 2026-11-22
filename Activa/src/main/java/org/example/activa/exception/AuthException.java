package org.example.activa.exception;

public class AuthException extends Exception {

    // Crea la excepción con el mensaje de error especificado
    public AuthException(String mensaje) {
        super(mensaje);
    }
}