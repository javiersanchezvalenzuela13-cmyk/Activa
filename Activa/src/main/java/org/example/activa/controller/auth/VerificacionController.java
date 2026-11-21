package org.example.activa.controller.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.example.activa.model.Usuario;
import org.example.activa.util.Sesion;
import org.example.activa.util.Navegador;
import org.example.activa.util.Vista;

public class VerificacionController {

    @FXML
    private TextField codigo1, codigo2, codigo3, codigo4, codigo5, codigo6;

    @FXML
    private Button btnVerificar;

    private Usuario adminPendiente;

    // Variable estática para recibir el usuario desde el LoginController
    private static Usuario adminPendienteTemporal;

    public static void setAdminPendienteTemporal(Usuario admin) {
        adminPendienteTemporal = admin;
    }

    @FXML
    public void initialize() {
        // Recuperamos el usuario y limpiamos la variable estática
        if (adminPendienteTemporal != null) {
            this.adminPendiente = adminPendienteTemporal;
            adminPendienteTemporal = null;
        }

        setupAutoFocus(codigo1, codigo2);
        setupAutoFocus(codigo2, codigo3);
        setupAutoFocus(codigo3, codigo4);
        setupAutoFocus(codigo4, codigo5);
        setupAutoFocus(codigo5, codigo6);
        setupAutoFocus(codigo6, null);
    }

    @FXML
    private void handleVerificarAdmin(ActionEvent event) {
        String codigoCompleto = codigo1.getText() + codigo2.getText() + codigo3.getText() +
                codigo4.getText() + codigo5.getText() + codigo6.getText();

        if (codigoCompleto.length() < 6) {
            mostrarAlerta(Alert.AlertType.WARNING, "Código incompleto", "Introduce los 6 dígitos de tu app de verificación.");
            return;
        }

        if (codigoCompleto.equals("123456") && adminPendiente != null) {
            Sesion.iniciar(adminPendiente.getIdUsuario(), adminPendiente.getNombreUsuario(), adminPendiente.getEmail(), true);
            Navegador.mostrarPantallaCompleta(Vista.MAIN);
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Código incorrecto", "El código introducido no es válido o ha caducado.");
        }
    }

    @FXML
    private void handleReenviar(ActionEvent event) {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Código reenviado", "Se ha enviado un código de seguridad a tu correo electrónico.");
    }

    @FXML
    private void handleUsarOtraCuenta(ActionEvent event) {
        Sesion.cerrar();
        Navegador.mostrarPantallaCompleta(Vista.LOGIN);
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