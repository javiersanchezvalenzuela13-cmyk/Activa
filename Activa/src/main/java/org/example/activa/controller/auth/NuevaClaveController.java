package org.example.activa.controller.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.activa.dao.UsuarioDAO;
import org.example.activa.util.Navegador;
import org.example.activa.util.Vista;

public class NuevaClaveController {

    @FXML
    private Label mensajeCorreoLabel;

    @FXML
    private TextField codigo1, codigo2, codigo3, codigo4, codigo5, codigo6;

    @FXML
    private PasswordField nuevaPasswordField, repetirPasswordField;

    @FXML
    private Button btnGuardar;

    private String emailRecuperacion;

    // Variable estática para recibir el correo desde RecuperarClaveController
    private static String emailTemporal;

    public static void setEmailTemporal(String email) {
        emailTemporal = email;
    }

    @FXML
    public void initialize() {
        // Recuperamos el correo y configuramos el texto visual
        if (emailTemporal != null) {
            this.emailRecuperacion = emailTemporal;
            if (mensajeCorreoLabel != null) {
                mensajeCorreoLabel.setText("Si existe una cuenta con " + censurarCorreo(emailRecuperacion) +
                        ", te hemos enviado un código de 6 dígitos. Caduca en 15 minutos.");
            }
            emailTemporal = null; // Limpiamos la variable estática
        }

        setupAutoFocus(codigo1, codigo2);
        setupAutoFocus(codigo2, codigo3);
        setupAutoFocus(codigo3, codigo4);
        setupAutoFocus(codigo4, codigo5);
        setupAutoFocus(codigo5, codigo6);
        setupAutoFocus(codigo6, null);
    }

    @FXML
    private void handleGuardarContrasena(ActionEvent event) {
        String codigoCompleto = codigo1.getText() + codigo2.getText() + codigo3.getText() +
                codigo4.getText() + codigo5.getText() + codigo6.getText();
        String nuevaClave = nuevaPasswordField.getText();
        String repetirClave = repetirPasswordField.getText();

        if (codigoCompleto.length() < 6) {
            mostrarAlerta(Alert.AlertType.WARNING, "Código incompleto", "Introduce los 6 dígitos que te hemos enviado.");
            return;
        }

        if (nuevaClave.isEmpty() || repetirClave.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos", "Por favor, introduce tu nueva contraseña en ambos campos.");
            return;
        }

        if (nuevaClave.length() < 8 || !nuevaClave.matches(".*\\d.*")) {
            mostrarAlerta(Alert.AlertType.WARNING, "Contraseña débil", "La contraseña debe tener al menos 8 caracteres y un número.");
            return;
        }

        if (!nuevaClave.equals(repetirClave)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Contraseñas no coinciden", "Las contraseñas introducidas no son iguales.");
            return;
        }

        UsuarioDAO usuarioDAO = new UsuarioDAO();

        if (usuarioDAO.existeEmail(emailRecuperacion)) {
            boolean actualizado = usuarioDAO.actualizarContrasena(emailRecuperacion, nuevaClave);
            if (actualizado) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tu contraseña se ha cambiado correctamente.");
                Navegador.mostrarPantallaCompleta(Vista.LOGIN);
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la contraseña. Inténtalo más tarde.");
            }
        } else {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Si el correo existía, tu contraseña se ha cambiado.");
            Navegador.mostrarPantallaCompleta(Vista.LOGIN);
        }
    }

    @FXML
    private void handleVolverPaso1(ActionEvent event) {
        Navegador.mostrarPantallaCompleta(Vista.RECUPERAR_CLAVE);
    }

    @FXML
    private void handleReenviarCodigo(ActionEvent event) {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Código reenviado", "Se ha enviado un nuevo código a tu correo.");
    }

    private String censurarCorreo(String email) {
        if (email == null || !email.contains("@")) return "...";
        String[] partes = email.split("@");
        if (partes[0].length() <= 1) return partes[0] + "••••@" + partes[1];
        return partes[0].charAt(0) + "••••@" + partes[1];
    }

    private void setupAutoFocus(TextField actual, TextField siguiente) {
        actual.textProperty().addListener((obs, viejoTexto, nuevoTexto) -> {
            if (nuevoTexto.length() == 1 && siguiente != null) {
                siguiente.requestFocus();
            } else if (nuevoTexto.length() > 1) {
                actual.setText(nuevoTexto.substring(0, 1));
            }
        });
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}