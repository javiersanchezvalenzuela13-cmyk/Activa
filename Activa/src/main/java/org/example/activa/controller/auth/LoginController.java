package org.example.activa.controller.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

import org.example.activa.dao.UsuarioDAO;
import org.example.activa.util.SesionUsuarioUtil;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    // Se ejecuta al cargar la pantalla
    @FXML
    public void initialize() {
    }

    // Valida las credenciales e inicia sesión en la aplicación
    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos incompletos", "Por favor, introduce tu correo y contraseña.");
            return;
        }

        boolean esValido = usuarioDAO.validarCredenciales(email, password);

        if (esValido) {
            SesionUsuarioUtil.emailActual = email;
            cambiarPantalla(event, "/org/example/activa/fxml/main-view.fxml", "Activa - Inicio");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Acceso denegado", "El correo o la contraseña son incorrectos.");
        }
    }

    // Accede a la pantalla principal en modo invitado
    @FXML
    public void handleExplorarSinCuenta(ActionEvent event) {
        cambiarPantalla(event, "/org/example/activa/fxml/main-view.fxml", "Activa - Explorar");
    }

    // Muestra un aviso informativo para el inicio de sesión con Google
    @FXML
    public void handleGoogleLogin(ActionEvent event) {
        mostrarAlerta(Alert.AlertType.INFORMATION, "Próximamente", "El inicio de sesión con Google estará disponible pronto.");
    }

    // Navega a la pantalla de creación de cuenta
    @FXML
    public void handleCrearCuenta(ActionEvent event) {
        cambiarPantalla(event, "/org/example/activa/fxml/auth/registro-view.fxml", "Activa - Crear cuenta");
    }

    // Navega a la pantalla de recuperación de contraseña
    @FXML
    public void handleOlvidarContrasena(ActionEvent event) {
        cambiarPantalla(event, "/org/example/activa/fxml/auth/recuperar-clave-view.fxml", "Activa - Recuperar contraseña");
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
            mostrarAlerta(Alert.AlertType.ERROR, "Error de carga", "No se pudo abrir la pantalla: " + titulo + "\nRuta intentada: " + rutaFxml);
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