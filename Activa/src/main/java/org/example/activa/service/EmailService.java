package org.example.activa.service;

import javafx.scene.control.Alert;
import org.example.activa.util.AlertUtil;

import java.util.Random;

public class EmailService {

    // Genera un código numérico aleatorio de 6 dígitos formateado con ceros a la izquierda si es necesario.
    public static String generarCodigo() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    // Simula el envío de un correo electrónico mostrando el código generado en la consola y en una ventana emergente.
    public static boolean enviarCodigo(String destinatario, String codigo, String asunto) {
        // Imprimir en consola
        System.out.println("\n==========================================");
        System.out.println("📧 SIMULACIÓN DE CORREO ENVIADO");
        System.out.println("Para: " + destinatario);
        System.out.println("Asunto: " + asunto);
        System.out.println("CÓDIGO SECRETO: " + codigo);
        System.out.println("==========================================\n");

        AlertUtil.crear(Alert.AlertType.INFORMATION,
                        "Correo enviado",
                        "Hemos enviado un código a " + destinatario + ".\nTu código de verificación es: " + codigo)
                .showAndWait();

        return true;
    }
}