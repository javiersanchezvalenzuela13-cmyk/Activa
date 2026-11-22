package org.example.activa.controller.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.activa.exception.AuthException;
import org.example.activa.model.Usuario;
import org.example.activa.service.AuthService;
import org.example.activa.util.Sesion;
import org.example.activa.util.Navegador;
import org.example.activa.util.Vista;
import org.example.activa.service.EmailService;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    private final AuthService authService = new AuthService();

    // Inicializa la vista de inicio de sesión (actualmente sin configuración adicional requerida).
    @FXML
    public void initialize() {
    }

    // Autentica al usuario e inicia sesión, o redirige a verificación en dos pasos si es administrador.
    @FXML
    public void handleLogin() {
        try {
            String emailIngresado = emailField.getText();
            String emailReal = emailIngresado.replace(".admin@", "@");
            boolean intentoAdmin = emailIngresado.contains(".admin@");

            Usuario usuario = authService.login(emailReal, passwordField.getText());

            // Comprobamos si es admin real en la BD o si usó el truco del .admin
            if (usuario.esAdmin() || intentoAdmin) {
                String codigo2FA = EmailService.generarCodigo();
                EmailService.enviarCodigo(usuario.getEmail(), codigo2FA, "Activa - Código de acceso Administrador");

                VerificacionController.setDatosTemporales(usuario, codigo2FA);
                Navegador.mostrarPantallaCompleta(Vista.VERIFICACION);
            } else {
                Sesion.iniciar(usuario.getIdUsuario(), usuario.getNombreUsuario(), usuario.getEmail(), false);
                Navegador.mostrarPantallaCompleta(Vista.MAIN);
            }
        } catch (AuthException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    // Cierra cualquier sesión activa existente y abre la aplicación en modo invitado.
    @FXML
    public void handleExplorarSinCuenta() {
        Sesion.cerrar();
        Navegador.mostrarPantallaCompleta(Vista.MAIN);
    }

    // Redirige al usuario a la pantalla de registro para crear una nueva cuenta.
    @FXML
    public void handleCrearCuenta() {
        Navegador.mostrarPantallaCompleta(Vista.REGISTRO);
    }

    // Redirige al usuario a la pantalla de recuperación de contraseña.
    @FXML
    public void handleOlvidarContrasena() {
        Navegador.mostrarPantallaCompleta(Vista.RECUPERAR_CLAVE);
    }

    // Muestra una ventana emergente de alerta estándar con un título y mensaje personalizados.
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}