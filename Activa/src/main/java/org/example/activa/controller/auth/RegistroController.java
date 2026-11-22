package org.example.activa.controller.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.activa.exception.AuthException;
import org.example.activa.model.Usuario;
import org.example.activa.service.AuthService;
import org.example.activa.util.Navegador;
import org.example.activa.util.Sesion;
import org.example.activa.util.Vista;
import org.example.activa.util.AlertUtil;

public class RegistroController {

    @FXML
    private TextField nombreField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox terminosCheck;

    private final AuthService authService = new AuthService();

    // Valida la aceptación de términos y condiciones, registra al nuevo usuario y avanza a la selección de intereses.
    @FXML
    public void handleContinuar() {
        if (!terminosCheck.isSelected()) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Términos y condiciones",
                    "Debes aceptar los términos y condiciones para crear una cuenta."
            );
            return;
        }

        try {
            Usuario usuario = authService.registrar(nombreField.getText(), emailField.getText(), passwordField.getText());
            Sesion.iniciar(usuario.getIdUsuario(), usuario.getNombreUsuario(), usuario.getEmail(), false);
            Navegador.mostrarPantallaCompleta(Vista.INTERESES);
        } catch (AuthException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error en el registro", e.getMessage());
        }
    }

    // Redirige al usuario de vuelta a la pantalla de inicio de sesión.
    @FXML
    public void handleIrLogin() {
        Navegador.mostrarPantallaCompleta(Vista.LOGIN);
    }

    // Muestra una alerta con el estilo de la app
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        AlertUtil.crear(tipo, titulo, mensaje).showAndWait();
    }
}