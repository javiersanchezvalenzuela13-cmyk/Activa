package org.example.activa.controller.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    // Se ejecuta automáticamente al cargar la vista FXML
    @FXML
    public void initialize() {
        // Configuración inicial de la interfaz si es necesaria
    }

    // Acción asociada al botón principal de inicio de sesión
    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        // Validación para evitar campos vacíos
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos", "Por favor, introduce tu correo y contraseña.");
            return;
        }

        // Integración de la lógica con la base de datos
        System.out.println("Procesando inicio de sesión para: " + email);
    }

    // Muestra una ventana emergente
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}