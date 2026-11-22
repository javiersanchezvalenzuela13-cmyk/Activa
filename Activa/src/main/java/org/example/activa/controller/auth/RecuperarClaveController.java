package org.example.activa.controller.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.example.activa.service.EmailService;
import org.example.activa.util.Navegador;
import org.example.activa.util.Vista;
import org.example.activa.util.AlertUtil;

public class RecuperarClaveController {

    @FXML
    private TextField emailField;

    // Inicializa el controlador de la vista de recuperación de contraseña.
    @FXML
    public void initialize() {
    }

    // Valida el correo ingresado, envía un código de recuperación y navega al formulario de nueva clave.
    @FXML
    private void handleEnviarCodigo() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo vacío", "Por favor, introduce el correo de tu cuenta.");
            return;
        }

        // Comprobamos si el email existe. Si no existe, simulamos que sí por seguridad
        // pero solo mandamos el código real si existe. En este proyecto académico, lo mandamos siempre.
        String codigo = EmailService.generarCodigo();
        EmailService.enviarCodigo(email, codigo, "Activa - Recuperación de contraseña");

        // Pasamos el email y el código al siguiente controlador usando variables estáticas
        NuevaClaveController.setDatosTemporales(email, codigo);
        Navegador.mostrarPantallaCompleta(Vista.NUEVA_CLAVE);
    }

    // Regresa a la pantalla principal de inicio de sesión.
    @FXML
    private void handleVolverLogin() {
        Navegador.mostrarPantallaCompleta(Vista.LOGIN);
    }

    // Muestra una alerta con el estilo de la app
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        AlertUtil.crear(tipo, titulo, mensaje).showAndWait();
    }
}