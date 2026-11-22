package org.example.activa.controller.usuario;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import org.example.activa.dao.UsuarioDAO;
import org.example.activa.model.Usuario;
import org.example.activa.util.Navegador;
import org.example.activa.util.Sesion;
import org.example.activa.util.Vista;

import java.io.File;
import java.util.Optional;

public class PerfilController {

    @FXML
    private ToggleGroup grupoPestanas;

    @FXML
    private Label lblIniciales;

    @FXML
    private Label lblNombreHeader;

    @FXML
    private ImageView imgAvatar;

    @FXML
    private TextField txtNombreUsuario;

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextArea txtSobreMi;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    public void initialize() {
        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        if (!Sesion.haySesion()) {
            return;
        }

        Usuario usuario = usuarioDAO.obtenerUsuarioPorId(Sesion.getIdUsuario());

        if (usuario != null) {
            txtNombreUsuario.setText(usuario.getNombreUsuario());
            txtCorreo.setText(usuario.getEmail());
            txtTelefono.setText(usuario.getTelefono() != null ? usuario.getTelefono() : "");
            txtSobreMi.setText(usuario.getSobreMi() != null ? usuario.getSobreMi() : "");

            if (lblNombreHeader != null) {
                lblNombreHeader.setText(usuario.getNombreUsuario());
            }
            if (lblIniciales != null) {
                lblIniciales.setText(Sesion.getIniciales());
            }

            if (usuario.getFotoPerfil() != null && !usuario.getFotoPerfil().isBlank()) {
                mostrarImagenCircular(usuario.getFotoPerfil());
            } else if (lblIniciales != null) {
                lblIniciales.setVisible(true);
                if (imgAvatar != null) {
                    imgAvatar.setImage(null);
                }
            }
        } else {
            txtNombreUsuario.setText(Sesion.getNombre());
            txtCorreo.setText(Sesion.getEmail());
            if (lblNombreHeader != null) {
                lblNombreHeader.setText(Sesion.getNombre());
            }
            if (lblIniciales != null) {
                lblIniciales.setText(Sesion.getIniciales());
            }
        }
    }

    @FXML
    void onCambiarFoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar foto de perfil");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );

        File archivo = fileChooser.showOpenDialog(txtNombreUsuario.getScene().getWindow());
        if (archivo != null) {
            String rutaUri = archivo.toURI().toString();
            if (usuarioDAO.actualizarFotoPerfil(Sesion.getIdUsuario(), rutaUri)) {
                mostrarImagenCircular(rutaUri);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Foto actualizada", "Tu imagen de perfil se ha guardado correctamente.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar la imagen de perfil.");
            }
        }
    }

    private void mostrarImagenCircular(String rutaUri) {
        if (imgAvatar == null) {
            return;
        }
        try {
            Image imagen = new Image(rutaUri, 64, 64, false, true);
            imgAvatar.setImage(imagen);
            imgAvatar.setClip(new Circle(32, 32, 32));
            if (lblIniciales != null) {
                lblIniciales.setVisible(false);
            }
        } catch (Exception e) {
            if (lblIniciales != null) {
                lblIniciales.setVisible(true);
            }
        }
    }

    @FXML
    void onGuardarCambios(ActionEvent event) {
        String nuevoNombre = txtNombreUsuario.getText().trim();
        String nuevoTelefono = txtTelefono.getText().trim();
        String nuevoSobreMi = txtSobreMi.getText().trim();

        if (nuevoNombre.isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campo obligatorio", "El nombre de usuario no puede estar vacío.");
            return;
        }

        if (!nuevoTelefono.isEmpty() && !nuevoTelefono.matches("\\d{9}")) {
            mostrarAlerta(Alert.AlertType.WARNING, "Teléfono no válido", "El teléfono debe contener exactamente 9 dígitos numéricos.");
            return;
        }

        boolean exito = usuarioDAO.actualizarPerfil(
                Sesion.getIdUsuario(),
                nuevoNombre,
                nuevoTelefono,
                nuevoSobreMi
        );

        if (exito) {
            Sesion.iniciar(Sesion.getIdUsuario(), nuevoNombre, Sesion.getEmail(), Sesion.isAdmin());
            if (lblNombreHeader != null) {
                lblNombreHeader.setText(nuevoNombre);
            }
            if (lblIniciales != null) {
                lblIniciales.setText(Sesion.getIniciales());
            }
            mostrarAlerta(Alert.AlertType.INFORMATION, "Perfil actualizado", "Tus datos se han guardado correctamente.");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar", "Hubo un problema al actualizar los datos en la base de datos.");
        }
    }

    @FXML
    void onDescartar(ActionEvent event) {
        cargarDatosUsuario();
    }

    @FXML
    void onEliminarCuenta(ActionEvent event) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar cuenta");
        confirmacion.setHeaderText("¿Estás seguro de que quieres eliminar tu cuenta?");
        confirmacion.setContentText("Se borrarán tu historial, etiquetas y métodos de pago. No se puede deshacer.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            boolean exito = usuarioDAO.eliminarUsuario(Sesion.getIdUsuario());

            if (exito) {
                Sesion.cerrar();
                Navegador.mostrarPantallaCompleta(Vista.LOGIN);
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error al eliminar", "No se pudo eliminar la cuenta de la base de datos.");
            }
        }
    }

    @FXML
    void onIrAEtiquetas(ActionEvent event) {
        Navegador.irA(Vista.ETIQUETAS);
    }

    @FXML
    void onIrAMetodosPago(ActionEvent event) {
        Navegador.irA(Vista.METODOS_PAGO);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}