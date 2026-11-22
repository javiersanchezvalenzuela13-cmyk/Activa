package org.example.activa.controller.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.activa.exception.AuthException;
import org.example.activa.model.Usuario;
import org.example.activa.service.AuthService;
import org.example.activa.service.EmailService;
import org.example.activa.util.Navegador;
import org.example.activa.util.Sesion;
import org.example.activa.util.Vista;

import java.util.prefs.Preferences;

public class LoginController {

    private static final Preferences PREFS = Preferences.userNodeForPackage(LoginController.class);
    private static final String PREF_RECORDAR = "login_recordar";
    private static final String PREF_EMAIL = "login_email";
    private static final String PREF_CLAVE = "login_clave";

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox recordarmeCheck;

    private final AuthService authService = new AuthService();

    // Rellena los datos guardados si el usuario marcó "Recordarme" en el último inicio de sesión.
    @FXML
    public void initialize() {
        if (recordarmeCheck != null && PREFS.getBoolean(PREF_RECORDAR, false)) {
            emailField.setText(PREFS.get(PREF_EMAIL, ""));
            passwordField.setText(PREFS.get(PREF_CLAVE, ""));
            recordarmeCheck.setSelected(true);
        }
    }

    // Autentica al usuario e inicia sesión, o redirige a verificación en dos pasos si es administrador.
    @FXML
    public void handleLogin() {
        try {
            String emailIngresado = emailField.getText();
            String claveIngresada = passwordField.getText();
            String emailReal = emailIngresado.replace(".admin@", "@");
            boolean intentoAdmin = emailIngresado.contains(".admin@");

            Usuario usuario = authService.login(emailReal, claveIngresada);
            guardarPreferenciasLogin(emailIngresado, claveIngresada);

            // Comprobamos si es admin real en la BD o si usó el truco del .admin
            if (usuario.esAdmin() || intentoAdmin) {
                String codigo2FA = EmailService.generarCodigo();
                EmailService.enviarCodigo(usuario.getEmail(), codigo2FA, "Activa - Código de acceso Administrador");

                VerificacionController.setDatosTemporales(usuario, codigo2FA);
                Navegador.mostrarPantallaCompleta(Vista.VERIFICACION);
            } else {
                Sesion.iniciar(usuario.getIdUsuario(), usuario.getNombreUsuario(), usuario.getEmail(), false);
                Sesion.cargarFoto(usuario.getFotoPerfil());
                Navegador.mostrarPantallaCompleta(Vista.MAIN);
            }
        } catch (AuthException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    // Guarda o borra las credenciales localmente según esté marcada la casilla "Recordarme".
    private void guardarPreferenciasLogin(String email, String clave) {
        if (recordarmeCheck != null && recordarmeCheck.isSelected()) {
            PREFS.putBoolean(PREF_RECORDAR, true);
            PREFS.put(PREF_EMAIL, email);
            PREFS.put(PREF_CLAVE, clave);
        } else {
            PREFS.remove(PREF_RECORDAR);
            PREFS.remove(PREF_EMAIL);
            PREFS.remove(PREF_CLAVE);
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