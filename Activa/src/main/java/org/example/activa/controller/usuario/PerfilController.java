package org.example.activa.controller.usuario;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import org.example.activa.controller.auth.LoginController;
import org.example.activa.dao.UsuarioDAO;
import org.example.activa.model.Usuario;
import org.example.activa.util.Navegador;
import org.example.activa.util.Sesion;
import org.example.activa.util.Vista;
import org.example.activa.util.AlertUtil;

import java.io.File;

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

    // Inicializa la vista cargando los datos del usuario activo desde la base de datos.
    @FXML
    public void initialize() {
        if (grupoPestanas != null) {
            grupoPestanas.selectedToggleProperty().addListener((obs, anterior, actual) -> {
                if (actual == null && anterior != null) {
                    anterior.setSelected(true);
                }
            });
        }

        cargarDatosUsuario();
    }

    // Consulta la base de datos para obtener los datos del perfil y actualiza los campos visuales correspondientes.
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

    // Abre un selector de archivos para que el usuario elija una nueva foto de perfil y la guarda.
    @FXML
    void onCambiarFoto() {
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
                Sesion.cargarFoto(rutaUri);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Foto actualizada", "Tu imagen de perfil se ha guardado correctamente.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar la imagen de perfil.");
            }
        }
    }

    // Carga la imagen desde la ruta especificada y le aplica un recorte circular para mostrarla como avatar.
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

    // Valida los datos ingresados en el formulario y los actualiza en la base de datos y en la sesión activa.
    @FXML
    void onGuardarCambios() {
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

    // Restablece el formulario a los últimos datos guardados, deshaciendo los cambios no guardados.
    @FXML
    void onDescartar() {
        cargarDatosUsuario();
    }

    // Muestra un diálogo de confirmación y, si se acepta, elimina la cuenta de usuario de la base de datos de forma permanente.
    @FXML
    void onEliminarCuenta() {
        boolean eliminar = AlertUtil.confirmarPeligro(
                "¿Eliminar tu cuenta?",
                "Se borrarán tu historial, etiquetas y métodos de pago. No se puede deshacer.",
                "Eliminar cuenta", "Cancelar");
        if (eliminar) {
            String emailEliminado = Sesion.getEmail() != null ? Sesion.getEmail() : txtCorreo.getText();
            boolean exito = usuarioDAO.eliminarUsuario(Sesion.getIdUsuario());

            if (exito) {
                LoginController.limpiarRecordarme(emailEliminado);
                Sesion.cerrar();
                Navegador.mostrarPantallaCompleta(Vista.LOGIN);
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error al eliminar", "No se pudo eliminar la cuenta de la base de datos.");
            }
        }
    }

    // Cambia la vista interna de la sección de perfil a la pestaña de gestión de etiquetas.
    @FXML
    void onIrAEtiquetas() {
        Navegador.irA(Vista.ETIQUETAS);
    }

    // Cambia la vista interna de la sección de perfil a la pestaña de gestión de métodos de pago.
    @FXML
    void onIrAMetodosPago() {
        Navegador.irA(Vista.METODOS_PAGO);
    }

    // Muestra una alerta con el estilo de la app
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        AlertUtil.crear(tipo, titulo, mensaje).showAndWait();
    }
}