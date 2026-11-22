package org.example.activa.controller.usuario;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.activa.dao.MetodoPagoDAO;
import org.example.activa.model.MetodoPago;
import org.example.activa.util.Navegador;
import org.example.activa.util.Sesion;
import org.example.activa.util.Vista;

import java.util.List;
import java.util.Optional;

public class MetodosPagoController {

    @FXML
    private ToggleGroup grupoPestanas;

    @FXML
    private VBox contenedorMetodos;

    @FXML
    private ComboBox<String> cmbTipo;

    @FXML
    private Label lblIdentificador;

    @FXML
    private TextField txtIdentificador;

    @FXML
    private Label lblTitular;

    @FXML
    private TextField txtTitular;

    @FXML
    private Label lblCaducidad;

    @FXML
    private TextField txtCaducidad;

    @FXML
    private CheckBox chkPredeterminado;

    private final MetodoPagoDAO metodoPagoDAO = new MetodoPagoDAO();

    // Inicializa el selector de tipo y carga los métodos de pago del usuario
    @FXML
    public void initialize() {
        cmbTipo.getItems().addAll("TARJETA", "BIZUM");
        cmbTipo.setValue("TARJETA");
        cmbTipo.setOnAction(e -> actualizarCamposFormulario());

        actualizarCamposFormulario();
        cargarMetodosPago();
    }

    // Adapta las etiquetas y visibilidad del formulario según sea Tarjeta o Bizum
    private void actualizarCamposFormulario() {
        boolean esTarjeta = "TARJETA".equals(cmbTipo.getValue());

        lblIdentificador.setText(esTarjeta ? "Número de tarjeta" : "Teléfono Bizum");
        txtIdentificador.setPromptText(esTarjeta ? "1234 5678 9012 3456" : "600123456");

        lblTitular.setVisible(esTarjeta);
        lblTitular.setManaged(esTarjeta);
        txtTitular.setVisible(esTarjeta);
        txtTitular.setManaged(esTarjeta);

        lblCaducidad.setVisible(esTarjeta);
        lblCaducidad.setManaged(esTarjeta);
        txtCaducidad.setVisible(esTarjeta);
        txtCaducidad.setManaged(esTarjeta);
    }

    // Consulta la base de datos y renderiza la lista de métodos guardados
    private void cargarMetodosPago() {
        if (!Sesion.haySesion()) {
            return;
        }

        contenedorMetodos.getChildren().clear();
        List<MetodoPago> metodos = metodoPagoDAO.obtenerMetodosPorUsuario(Sesion.getIdUsuario());

        if (metodos.isEmpty()) {
            Label lblVacio = new Label("Aún no tienes ningún método de pago guardado.");
            lblVacio.getStyleClass().add("fila-datos");
            contenedorMetodos.getChildren().add(lblVacio);
            return;
        }

        for (MetodoPago metodo : metodos) {
            contenedorMetodos.getChildren().add(crearFilaMetodo(metodo));
        }
    }

    // Construye visualmente cada tarjeta/Bizum guardado con sus acciones
    private HBox crearFilaMetodo(MetodoPago metodo) {
        HBox fila = new HBox(16);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12; -fx-padding: 16; -fx-border-color: #E2E8F0; -fx-border-radius: 12;");

        VBox info = new VBox(4);
        String titulo = metodo.getTipo() + " · " + metodo.getIdentificador();
        if (metodo.isEsPredeterminado()) {
            titulo += "  (Predeterminado)";
        }

        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("fila-nombre");

        String detalle = "TARJETA".equals(metodo.getTipo())
                ? "Titular: " + metodo.getTitular() + " · Caduca: " + metodo.getFechaCaducidad()
                : "Pago rápido mediante Bizum";

        Label lblDetalle = new Label(detalle);
        lblDetalle.getStyleClass().add("fila-datos");

        info.getChildren().addAll(lblTitulo, lblDetalle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        fila.getChildren().addAll(info, spacer);

        if (!metodo.isEsPredeterminado()) {
            Button btnPredeterminado = new Button("Hacer predeterminado");
            btnPredeterminado.getStyleClass().add("boton-ghost-borde");
            btnPredeterminado.setOnAction(e -> onMarcarPredeterminado(metodo.getIdMetodoPago()));
            fila.getChildren().add(btnPredeterminado);
        }

        Button btnEliminar = new Button("Eliminar");
        btnEliminar.getStyleClass().add("boton-cancelar");
        btnEliminar.setOnAction(e -> onEliminarMetodo(metodo));
        fila.getChildren().add(btnEliminar);

        return fila;
    }

    // Valida los datos introducidos y guarda el nuevo método de pago en BD
    @FXML
    void onAgregarMetodo(ActionEvent event) {
        String tipo = cmbTipo.getValue();
        String identificadorRaw = txtIdentificador.getText().replaceAll("\\s+", "");
        String titular = txtTitular.getText().trim();
        String caducidad = txtCaducidad.getText().trim();
        boolean predeterminado = chkPredeterminado.isSelected();

        String identificadorGuardado;

        if ("TARJETA".equals(tipo)) {
            if (titular.isBlank()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Campo obligatorio", "Introduce el nombre del titular de la tarjeta.");
                return;
            }
            if (!identificadorRaw.matches("\\d{16}")) {
                mostrarAlerta(Alert.AlertType.WARNING, "Tarjeta no válida", "El número de tarjeta debe contener 16 dígitos.");
                return;
            }
            if (!caducidad.matches("^(0[1-9]|1[0-2])/\\d{4}$")) {
                mostrarAlerta(Alert.AlertType.WARNING, "Caducidad no válida", "Usa el formato MM/AAAA (ejemplo: 08/2028).");
                return;
            }
            String ultimos4 = identificadorRaw.substring(12);
            identificadorGuardado = "•••• •••• •••• " + ultimos4;
        } else {
            if (!identificadorRaw.matches("\\d{9}")) {
                mostrarAlerta(Alert.AlertType.WARNING, "Teléfono no válido", "El teléfono de Bizum debe tener 9 dígitos.");
                return;
            }
            identificadorGuardado = identificadorRaw;
            titular = null;
            caducidad = null;
        }

        MetodoPago nuevo = new MetodoPago(
                Sesion.getIdUsuario(),
                tipo,
                titular,
                identificadorGuardado,
                caducidad,
                predeterminado
        );

        if (metodoPagoDAO.agregarMetodoPago(nuevo)) {
            limpiarFormulario();
            cargarMetodosPago();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Método guardado", "El método de pago se ha añadido correctamente.");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar el método de pago.");
        }
    }

    // Establece un método existente como predeterminado y refresca la lista
    private void onMarcarPredeterminado(int idMetodoPago) {
        if (metodoPagoDAO.establecerPredeterminado(Sesion.getIdUsuario(), idMetodoPago)) {
            cargarMetodosPago();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el método predeterminado.");
        }
    }

    // Solicita confirmación y elimina el método de pago seleccionado
    private void onEliminarMetodo(MetodoPago metodo) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar método de pago");
        confirmacion.setHeaderText("¿Quieres eliminar " + metodo.getTipo() + " (" + metodo.getIdentificador() + ")?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            if (metodoPagoDAO.eliminarMetodoPago(metodo.getIdMetodoPago(), Sesion.getIdUsuario())) {
                cargarMetodosPago();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el método de pago.");
            }
        }
    }

    // Restablece los campos del formulario tras guardar
    private void limpiarFormulario() {
        txtIdentificador.clear();
        txtTitular.clear();
        txtCaducidad.clear();
        chkPredeterminado.setSelected(false);
    }

    // Navega de vuelta a la pestaña de datos personales
    @FXML
    void onIrADatos(ActionEvent event) {
        Navegador.irA(Vista.PERFIL);
    }

    // Navega a la pestaña de gestión de etiquetas
    @FXML
    void onIrAEtiquetas(ActionEvent event) {
        Navegador.irA(Vista.ETIQUETAS);
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