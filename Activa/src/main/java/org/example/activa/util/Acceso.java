package org.example.activa.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public final class Acceso {

    private Acceso() {
    }

    // Avisa al invitado y le ofrece ir al login
    public static void pedirLogin(String accion) {
        ButtonType iniciar = new ButtonType("Iniciar sesión", ButtonBar.ButtonData.OK_DONE);
        ButtonType seguir = new ButtonType("Seguir mirando", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alerta = new Alert(Alert.AlertType.INFORMATION,
                "Necesitas una cuenta para " + accion + ". Es gratis y solo tardas un minuto.",
                iniciar, seguir);
        alerta.setTitle("Inicia sesión");
        alerta.setHeaderText("Inicia sesión para " + accion);

        Optional<ButtonType> respuesta = alerta.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == iniciar) {
            Navegador.mostrarPantallaCompleta(Vista.LOGIN);
        }
    }
}