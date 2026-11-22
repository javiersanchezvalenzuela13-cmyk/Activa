package org.example.activa.controller.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.example.activa.model.Usuario;
import org.example.activa.service.EmailService;
import org.example.activa.util.Sesion;
import org.example.activa.util.Navegador;
import org.example.activa.util.Vista;

public class VerificacionController {

    @FXML
    private TextField codigo1, codigo2, codigo3, codigo4, codigo5, codigo6;

    private Usuario adminPendiente;
    private String codigoValido;

    // Variables estáticas para recibir datos desde el LoginController
    private static Usuario adminPendienteTemporal;
    private static String codigoGeneradoTemporal;

    // Almacena temporalmente el usuario administrador y el código 2FA para pasarlos a esta vista.
    public static void setDatosTemporales(Usuario admin, String codigo) {
        adminPendienteTemporal = admin;
        codigoGeneradoTemporal = codigo;
    }

    // Recupera los datos temporales del administrador y configura el salto automático entre casillas.
    @FXML
    public void initialize() {
        if (adminPendienteTemporal != null) {
            this.adminPendiente = adminPendienteTemporal;
            this.codigoValido = codigoGeneradoTemporal;
            adminPendienteTemporal = null;
            codigoGeneradoTemporal = null;
        }

        setupAutoFocus(codigo1, codigo2);
        setupAutoFocus(codigo2, codigo3);
        setupAutoFocus(codigo3, codigo4);
        setupAutoFocus(codigo4, codigo5);
        setupAutoFocus(codigo5, codigo6);
        setupAutoFocus(codigo6, null);
    }

    // Comprueba el código de 6 dígitos e inicia la sesión como administrador si es correcto.
    @FXML
    private void handleVerificarAdmin() {
        String codigoCompleto = codigo1.getText() + codigo2.getText() + codigo3.getText() +
                codigo4.getText() + codigo5.getText() + codigo6.getText();

        if (codigoCompleto.length() < 6) {
            mostrarAlerta(Alert.AlertType.WARNING, "Código incompleto", "Introduce los 6 dígitos que te hemos enviado.");
            return;
        }

        if (codigoCompleto.equals(codigoValido) && adminPendiente != null) {
            Sesion.iniciar(adminPendiente.getIdUsuario(), adminPendiente.getNombreUsuario(), adminPendiente.getEmail(), true);
            Sesion.cargarFoto(adminPendiente.getFotoPerfil());
            Navegador.mostrarPantallaCompleta(Vista.MAIN);
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Código incorrecto", "El código introducido no es válido o ha caducado.");
        }
    }

    // Genera un nuevo código de verificación y lo reenvía al correo del administrador.
    @FXML
    private void handleReenviar() {
        if (adminPendiente != null) {
            this.codigoValido = EmailService.generarCodigo();
            EmailService.enviarCodigo(adminPendiente.getEmail(), this.codigoValido, "Nuevo código de acceso Admin");
        }
    }

    // Cierra la sesión actual y redirige de vuelta a la pantalla de inicio de sesión.
    @FXML
    private void handleUsarOtraCuenta() {
        Sesion.cerrar();
        Navegador.mostrarPantallaCompleta(Vista.LOGIN);
    }

    // Limita el campo a un carácter y mueve el foco automáticamente al siguiente cuadro de texto.
    private void setupAutoFocus(TextField actual, TextField siguiente) {
        actual.textProperty().addListener((obs, viejoTexto, nuevoTexto) -> {
            if (nuevoTexto.length() == 1 && siguiente != null) {
                siguiente.requestFocus();
            } else if (nuevoTexto.length() > 1) {
                actual.setText(nuevoTexto.substring(0, 1));
            }
        });
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