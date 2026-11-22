package org.example.activa.controller.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.activa.dao.UsuarioDAO;
import org.example.activa.service.EmailService;
import org.example.activa.util.Navegador;
import org.example.activa.util.Vista;

public class NuevaClaveController {

    @FXML
    private Label mensajeCorreoLabel;

    @FXML
    private TextField codigo1, codigo2, codigo3, codigo4, codigo5, codigo6;

    @FXML
    private PasswordField nuevaPasswordField, repetirPasswordField;

    private String emailRecuperacion;
    private String codigoValido;

    // Variables estáticas para recibir datos desde RecuperarClaveController
    private static String emailTemporal;
    private static String codigoGeneradoTemporal;

    // Almacena temporalmente el correo y el código generado para pasarlos a esta vista.
    public static void setDatosTemporales(String email, String codigo) {
        emailTemporal = email;
        codigoGeneradoTemporal = codigo;
    }

    // Recupera los datos temporales, actualiza el aviso visual y configura el salto automático entre casillas.
    @FXML
    public void initialize() {
        if (emailTemporal != null) {
            this.emailRecuperacion = emailTemporal;
            this.codigoValido = codigoGeneradoTemporal;
            if (mensajeCorreoLabel != null) {
                mensajeCorreoLabel.setText("Si existe una cuenta con " + censurarCorreo(emailRecuperacion) +
                        ", te hemos enviado un código de 6 dígitos. Caduca en 15 minutos.");
            }
            emailTemporal = null;
            codigoGeneradoTemporal = null;
        }

        setupAutoFocus(codigo1, codigo2);
        setupAutoFocus(codigo2, codigo3);
        setupAutoFocus(codigo3, codigo4);
        setupAutoFocus(codigo4, codigo5);
        setupAutoFocus(codigo5, codigo6);
        setupAutoFocus(codigo6, null);
    }

    // Valida el código de verificación y las nuevas contraseñas antes de actualizar la clave en base de datos.
    @FXML
    private void handleGuardarContrasena() {
        String codigoCompleto = codigo1.getText() + codigo2.getText() + codigo3.getText() +
                codigo4.getText() + codigo5.getText() + codigo6.getText();
        String nuevaClave = nuevaPasswordField.getText();
        String repetirClave = repetirPasswordField.getText();

        if (codigoCompleto.length() < 6) {
            mostrarAlerta(Alert.AlertType.WARNING, "Código incompleto", "Introduce los 6 dígitos que te hemos enviado.");
            return;
        }

        if (!codigoCompleto.equals(codigoValido)) {
            mostrarAlerta(Alert.AlertType.ERROR, "Código incorrecto", "El código introducido no coincide con el enviado.");
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

    // Regresa a la pantalla inicial del proceso de recuperación de contraseña.
    @FXML
    private void handleVolverPaso1() {
        Navegador.mostrarPantallaCompleta(Vista.RECUPERAR_CLAVE);
    }

    // Genera un nuevo código de recuperación y lo reenvía al correo electrónico del usuario.
    @FXML
    private void handleReenviarCodigo() {
        if (emailRecuperacion != null) {
            this.codigoValido = EmailService.generarCodigo();
            EmailService.enviarCodigo(emailRecuperacion, this.codigoValido, "Nuevo código de recuperación");
        }
    }

    // Oculta parcialmente el nombre del correo electrónico por motivos de privacidad.
    private String censurarCorreo(String email) {
        if (email == null || !email.contains("@")) return "...";
        String[] partes = email.split("@");
        if (partes[0].length() <= 1) return partes[0] + "••••@" + partes[1];
        return partes[0].charAt(0) + "••••@" + partes[1];
    }

    // Limita el campo a un solo dígito y mueve automáticamente el foco al siguiente cuadro de texto.
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