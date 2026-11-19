package org.example.activa.controller.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

import org.example.activa.dao.UsuarioDAO;
import org.example.activa.util.SesionUsuarioUtil;

public class RegistroController {

    @FXML
    private TextField nombreField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox terminosCheck;

    // Valida los datos, registra al usuario en la BD y avanza a intereses
    @FXML
    public void handleContinuar(ActionEvent event) {
        String nombre = nombreField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos", "Rellena todos los campos para continuar.");
            return;
        }

        if (password.length() < 8 || !password.matches(".*\\d.*")) {
            mostrarAlerta(Alert.AlertType.WARNING, "Contraseña débil", "La contraseña debe tener al menos 8 caracteres y un número.");
            return;
        }

        if (!terminosCheck.isSelected()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Términos y condiciones", "Debes aceptar las normas de la comunidad.");
            return;
        }

        UsuarioDAO usuarioDAO = new UsuarioDAO();

        if (usuarioDAO.existeEmail(email)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Correo en uso", "Este correo electrónico ya está registrado.");
            return;
        }

        boolean registrado = usuarioDAO.registrarUsuario(nombre, email, password);

        if (!registrado) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Hubo un problema al crear la cuenta. Inténtalo más tarde.");
            return;
        }

        SesionUsuarioUtil.emailActual = email;
        cambiarPantalla(event, "/org/example/activa/fxml/auth/intereses-view.fxml", "Activa - Elige tus intereses");
    }

    // Navega a la pantalla de inicio de sesión
    @FXML
    public void handleIrLogin(ActionEvent event) {
        cambiarPantalla(event, "/org/example/activa/fxml/auth/login-view.fxml", "Activa - Iniciar sesión");
    }

    // Método auxiliar para cambiar de escena
    private void cambiarPantalla(ActionEvent event, String rutaFxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo cargar la pantalla.");
        }
    }

    // Método auxiliar para mostrar ventanas emergentes
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}