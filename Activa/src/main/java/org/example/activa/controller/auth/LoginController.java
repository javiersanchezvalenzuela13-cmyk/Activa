package org.example.activa.controller.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import org.example.activa.exception.AuthException;
import org.example.activa.model.Usuario;
import org.example.activa.service.AuthService;
import org.example.activa.util.Sesion;
import org.example.activa.util.Navegador;
import org.example.activa.util.Vista;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final AuthService authService = new AuthService();

    // Inicializa el controlador de la vista
    @FXML
    public void initialize() {
    }

    // Autentica al usuario y abre la sesión o pide verificación 2FA si es admin
    @FXML
    public void handleLogin(ActionEvent event) {
        try {
            String emailIngresado = emailField.getText();

            // Si el correo tiene .admin, lo quitamos para poder buscar el correo real en la BD
            String emailReal = emailIngresado.replace(".admin@", "@");
            boolean intentoAdmin = emailIngresado.contains(".admin@");

            // Pasamos el correo real a la validación
            Usuario usuario = authService.login(emailReal, passwordField.getText());

            // Comprobamos si es admin real en la BD o si usó el truco del .admin
            if (usuario.esAdmin() || intentoAdmin) {
                cargarVerificacionAdmin(usuario);
            } else {
                Sesion.iniciar(usuario.getIdUsuario(), usuario.getNombreUsuario(), usuario.getEmail(), false);
                Navegador.mostrarPantallaCompleta(Vista.MAIN);
            }
        } catch (AuthException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de acceso", e.getMessage());
        }
    }

    // Inicia la aplicación en modo invitado asegurando que no hay sesión abierta
    @FXML
    public void handleExplorarSinCuenta(ActionEvent event) {
        Sesion.cerrar();
        Navegador.mostrarPantallaCompleta(Vista.MAIN);
    }

    // Gestiona el acceso mediante cuenta de Google
    @FXML
    public void handleGoogleLogin(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Inicio de sesión con Google");
        dialog.setHeaderText("Simulación de Google Login");
        dialog.setContentText("Introduce tu correo de Gmail:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(emailGoogle -> {
            try {
                Usuario usuario = authService.loginConGoogle(emailGoogle);
                Sesion.iniciar(usuario.getIdUsuario(), usuario.getNombreUsuario(), usuario.getEmail(), usuario.esAdmin());
                Navegador.mostrarPantallaCompleta(Vista.MAIN);
            } catch (AuthException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
            }
        });
    }

    // Navega hacia la pantalla de registro
    @FXML
    public void handleCrearCuenta(ActionEvent event) {
        Navegador.mostrarPantallaCompleta(Vista.REGISTRO);
    }

    // Navega hacia la pantalla de recuperación de contraseña
    @FXML
    public void handleOlvidarContrasena(ActionEvent event) {
        Navegador.mostrarPantallaCompleta(Vista.RECUPERAR_CLAVE);
    }

    // Carga la vista de verificación 2FA pasándole los datos del administrador
    private void cargarVerificacionAdmin(Usuario admin) {
        VerificacionController.setAdminPendienteTemporal(admin);
        Navegador.mostrarPantallaCompleta(Vista.VERIFICACION);
    }

    // Muestra una ventana emergente con el mensaje indicado
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}