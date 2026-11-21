package org.example.activa.controller.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.example.activa.util.Navegador;
import org.example.activa.util.Vista;

public class RecuperarClaveController {

    @FXML
    private TextField emailField;

    @FXML
    private Button btnEnviarCodigo;

    @FXML
    public void initialize() {
    }

    @FXML
    private void handleEnviarCodigo(ActionEvent event) {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo vacío", "Por favor, introduce el correo de tu cuenta.");
            return;
        }

        // Pasamos el email al siguiente controlador usando su variable estática
        NuevaClaveController.setEmailTemporal(email);
        Navegador.mostrarPantallaCompleta(Vista.NUEVA_CLAVE);
    }

    @FXML
    private void handleVolverLogin(ActionEvent event) {
        Navegador.mostrarPantallaCompleta(Vista.LOGIN);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}