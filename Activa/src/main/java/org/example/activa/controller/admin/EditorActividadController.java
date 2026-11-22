package org.example.activa.controller.admin;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.example.activa.exception.ActividadException;
import org.example.activa.model.Actividad;
import org.example.activa.service.ActividadService;
import org.example.activa.util.AlertUtil;
import org.example.activa.util.EstiloActividad;
import org.example.activa.util.Iconos;
import org.example.activa.util.Navegador;
import org.example.activa.util.Sesion;
import org.example.activa.util.Vista;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import org.example.activa.util.Etiquetas;
import java.util.Map;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditorActividadController {

    // Formatos en español
    private static final Locale ES = Locale.forLanguageTag("es-ES");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("EEE d MMM", ES);
    private static final NumberFormat FORMATO_PRECIO = NumberFormat.getCurrencyInstance(ES);

    private static final int PLAZAS_MAX = 500;

    // Elementos del FXML
    @FXML private VBox contenido;
    @FXML private Text txtTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtLugar;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<LocalTime> cbInicio;
    @FXML private ComboBox<LocalTime> cbFin;
    @FXML private Spinner<Integer> spnPlazas;
    @FXML private TextField txtPrecio;
    @FXML private CheckBox chkGratis;
    @FXML private Label lblAvisoPlazas;
    @FXML private Button btnGuardar;
    @FXML private VBox vistaPrevia;
    @FXML private VBox cajaEtiquetas;
    @FXML private Label lblContadorEtiquetas;
    @FXML private StackPane cajaFoto;
    @FXML private Button btnQuitarFoto;

    // Services
    private final ActividadService actividadService = new ActividadService();

    // Actividad que se edita (null si se está creando una nueva)
    private Actividad actividadEditada;

    // Un botón por cada etiqueta
    private final List<ToggleButton> chips = new ArrayList<>();
    // Foto elegida (null si no hay)
    private byte[] imagenElegida;

    @FXML
    private void initialize() {
        // Solo pueden entrar administradores
        if (!Sesion.haySesion() || !Sesion.isAdmin()) {
            contenido.getChildren().setAll(new Label("Esta sección solo está disponible para administradores."));
            return;
        }

        prepararCampos();
        escucharCambios();
        nueva();
    }

    // Modos

    // Formulario vacío para crear
    public void nueva() {
        actividadEditada = null;
        txtTitulo.setText("Crear ");
        btnGuardar.setText("Crear actividad");

        txtNombre.clear();
        txtDescripcion.clear();
        txtLugar.clear();
        dpFecha.setValue(LocalDate.now().plusDays(7));
        cbInicio.setValue(LocalTime.of(10, 0));
        cbFin.setValue(LocalTime.of(11, 0));
        prepararPlazas(1, 20);
        chkGratis.setSelected(true);
        txtPrecio.clear();
        mostrarAvisoPlazas(0);
        marcarEtiquetas(List.of());
        imagenElegida = null;
        actualizarVistaPrevia();
    }

    // Formulario relleno para editar
    public void editar(Actividad a) {
        actividadEditada = a;
        txtTitulo.setText("Editar ");
        btnGuardar.setText("Guardar cambios");

        txtNombre.setText(a.getNombreActividad());
        txtDescripcion.setText(a.getDescripcion());
        txtLugar.setText(a.getLugar());
        dpFecha.setValue(a.getFechaActividad());
        asegurarHora(cbInicio, a.getHoraInicio());
        asegurarHora(cbFin, a.getHoraFin());
        prepararPlazas(Math.max(1, a.getCupoActual()), a.getCupoMax());
        chkGratis.setSelected(a.esGratis());
        txtPrecio.setText(a.esGratis() ? "" : a.getPrecio().toPlainString().replace('.', ','));
        mostrarAvisoPlazas(a.getCupoActual());
        marcarEtiquetas(a.getEtiquetas());
        imagenElegida = a.getImagen();
        actualizarVistaPrevia();
    }

    // Botones

    @FXML
    private void onGuardar() {
        BigDecimal precio = leerPrecio();
        if (precio == null) {
            mostrarError("El precio no es válido. Escríbelo como 12 o 12,50.");
            return;
        }

        try {
            if (actividadEditada == null) {
                Actividad nueva = new Actividad(
                        txtNombre.getText().trim(), txtDescripcion.getText().trim(), txtLugar.getText().trim(),
                        dpFecha.getValue(), cbInicio.getValue(), cbFin.getValue(),
                        precio, spnPlazas.getValue(), Sesion.getIdUsuario());
                nueva.setEtiquetas(etiquetasElegidas());
                nueva.setImagen(imagenElegida);
                actividadService.crear(nueva);
                mostrarInfo("Actividad creada: \"" + nueva.getNombreActividad() + "\".");
            } else {
                actividadEditada.setNombreActividad(txtNombre.getText().trim());
                actividadEditada.setDescripcion(txtDescripcion.getText().trim());
                actividadEditada.setLugar(txtLugar.getText().trim());
                actividadEditada.setFechaActividad(dpFecha.getValue());
                actividadEditada.setHoraInicio(cbInicio.getValue());
                actividadEditada.setHoraFin(cbFin.getValue());
                actividadEditada.setPrecio(precio);
                actividadEditada.setCupoMax(spnPlazas.getValue());
                actividadEditada.setEtiquetas(etiquetasElegidas());
                actividadEditada.setImagen(imagenElegida);
                actividadService.editar(actividadEditada);
                mostrarInfo("Cambios guardados.");
            }
            Navegador.irA(Vista.PANEL_ADMIN);
        } catch (ActividadException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void onCancelar() {
        Navegador.irA(Vista.PANEL_ADMIN);
    }

    @FXML
    private void onElegirFoto() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Elegir foto de la actividad");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes (JPG, PNG)", "*.jpg", "*.jpeg", "*.png"));

        File archivo = selector.showOpenDialog(contenido.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        if (archivo.length() > ActividadService.TAMANO_MAX_IMAGEN) {
            mostrarError("La foto pesa demasiado. El máximo son 2 MB.");
            return;
        }
        try {
            imagenElegida = Files.readAllBytes(archivo.toPath());
            actualizarVistaPrevia();
        } catch (IOException e) {
            mostrarError("No se pudo leer la foto.");
        }
    }

    @FXML
    private void onQuitarFoto() {
        imagenElegida = null;
        actualizarVistaPrevia();
    }

    // Preparar controles

    private void prepararCampos() {
        // Horas cada 30 minutos, de 07:00 a 23:30
        List<LocalTime> horas = new ArrayList<>();
        for (int minutos = 7 * 60; minutos <= 23 * 60 + 30; minutos += 30) {
            horas.add(LocalTime.of(minutos / 60, minutos % 60));
        }
        cbInicio.getItems().setAll(horas);
        cbFin.getItems().setAll(horas);

        // No dejar elegir días pasados en el calendario
        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setDisable(vacia || fecha.isBefore(LocalDate.now()));
            }
        });

        // Guardar lo escrito en el Spinner al salir de él
        spnPlazas.focusedProperty().addListener((obs, antes, ahora) -> {
            if (!ahora) {
                spnPlazas.increment(0);
            }
        });

        // Si es gratis, el precio no se puede escribir
        txtPrecio.disableProperty().bind(chkGratis.selectedProperty());

        crearChipsEtiquetas();
    }

    private void prepararPlazas(int minimo, int valor) {
        spnPlazas.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(minimo, PLAZAS_MAX, Math.max(minimo, valor)));
    }

    // Si la hora de la actividad no está en la lista (ej. 08:45), se añade
    private void asegurarHora(ComboBox<LocalTime> combo, LocalTime hora) {
        if (!combo.getItems().contains(hora)) {
            combo.getItems().add(hora);
            combo.getItems().sort(null);
        }
        combo.setValue(hora);
    }

    private void mostrarAvisoPlazas(int inscritos) {
        boolean hay = inscritos > 0;
        lblAvisoPlazas.setText(hay
                ? "Ya hay " + inscritos + (inscritos == 1 ? " persona inscrita" : " personas inscritas")
                + ": no puedes poner menos plazas."
                : "");
        lblAvisoPlazas.setVisible(hay);
        lblAvisoPlazas.setManaged(hay);
    }

    // Etiquetas

    // Un grupo (Deporte, Arte...) con sus botones
    private void crearChipsEtiquetas() {
        for (Map.Entry<String, List<String>> grupo : Etiquetas.POR_GRUPO.entrySet()) {
            Label titulo = new Label(grupo.getKey().toUpperCase());
            titulo.getStyleClass().add("eyebrow");

            FlowPane botones = new FlowPane(8, 8);
            for (String etiqueta : grupo.getValue()) {
                ToggleButton chip = new ToggleButton(etiqueta);
                chip.getStyleClass().add("chip");
                chip.setOnAction(e -> alPulsarChip(chip));
                chips.add(chip);
                botones.getChildren().add(chip);
            }
            cajaEtiquetas.getChildren().add(new VBox(6, titulo, botones));
        }
    }

    // No deja marcar más del máximo
    private void alPulsarChip(ToggleButton chip) {
        if (chip.isSelected() && etiquetasElegidas().size() > Etiquetas.MAXIMO_POR_ACTIVIDAD) {
            chip.setSelected(false);
        }
        actualizarContadorEtiquetas();
        actualizarVistaPrevia();
    }

    private void marcarEtiquetas(List<String> etiquetas) {
        for (ToggleButton chip : chips) {
            chip.setSelected(etiquetas.contains(chip.getText()));
        }
        actualizarContadorEtiquetas();
    }

    private List<String> etiquetasElegidas() {
        return chips.stream()
                .filter(ToggleButton::isSelected)
                .map(ToggleButton::getText)
                .toList();
    }

    private void actualizarContadorEtiquetas() {
        lblContadorEtiquetas.setText(etiquetasElegidas().size() + " / " + Etiquetas.MAXIMO_POR_ACTIVIDAD);
    }

    // Lee el precio del campo (null si no es un número válido)
    private BigDecimal leerPrecio() {
        if (chkGratis.isSelected()) {
            return BigDecimal.ZERO;
        }
        String texto = txtPrecio.getText().trim().replace(',', '.');
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Vista previa

    private void escucharCambios() {
        txtNombre.textProperty().addListener((o, a, n) -> actualizarVistaPrevia());
        txtLugar.textProperty().addListener((o, a, n) -> actualizarVistaPrevia());
        txtPrecio.textProperty().addListener((o, a, n) -> actualizarVistaPrevia());
        dpFecha.valueProperty().addListener((o, a, n) -> actualizarVistaPrevia());
        cbInicio.valueProperty().addListener((o, a, n) -> actualizarVistaPrevia());
        cbFin.valueProperty().addListener((o, a, n) -> actualizarVistaPrevia());
        chkGratis.selectedProperty().addListener((o, a, n) -> actualizarVistaPrevia());
        spnPlazas.valueProperty().addListener((o, a, n) -> actualizarVistaPrevia());
    }

    private void actualizarVistaPrevia() {
        // Actividad "de mentira" con lo que hay escrito, solo para dibujarla
        Actividad previa = new Actividad();
        previa.setIdActividad(actividadEditada == null ? 0 : actividadEditada.getIdActividad());
        previa.setNombreActividad(txtNombre.getText().isBlank() ? "Nombre de la actividad" : txtNombre.getText().trim());

        previa.setImagen(imagenElegida);

        // Foto pequeña junto a los botones
        cajaFoto.getChildren().setAll(EstiloActividad.portada(previa, 160, 100));
        btnQuitarFoto.setDisable(imagenElegida == null);

        BigDecimal precio = leerPrecio();
        Label lblPrecio = new Label(chkGratis.isSelected() ? "Gratis"
                : precio == null ? "Precio" : FORMATO_PRECIO.format(precio));
        lblPrecio.getStyleClass().add("etiqueta-precio");
        if (chkGratis.isSelected()) {
            lblPrecio.getStyleClass().add("gratis");
        }

        Label nombre = new Label(previa.getNombreActividad());
        nombre.getStyleClass().add("tarjeta-nombre");
        nombre.setWrapText(true);

        String fecha = dpFecha.getValue() == null ? "Fecha" : FORMATO_FECHA.format(dpFecha.getValue());
        String horas = (cbInicio.getValue() == null ? "--:--" : cbInicio.getValue())
                + " – " + (cbFin.getValue() == null ? "--:--" : cbFin.getValue());
        Label cuando = new Label(fecha + " · " + horas, Iconos.crear("calendar", 14));
        cuando.getStyleClass().add("tarjeta-dato");

        Label donde = new Label(txtLugar.getText().isBlank() ? "Lugar" : txtLugar.getText().trim(),
                Iconos.crear("pin", 14));
        donde.getStyleClass().add("tarjeta-dato");
        donde.setWrapText(true);

        Integer plazas = spnPlazas.getValue();
        Label lblPlazas = new Label((plazas == null ? 0 : plazas) + " plazas");
        lblPlazas.getStyleClass().add("texto-plazas");

        StackPane miniatura = EstiloActividad.portada(previa, 248, 140);

        FlowPane etiquetasPrevia = new FlowPane(6, 6);
        etiquetasPrevia.getChildren().setAll(EstiloActividad.pastillas(etiquetasElegidas(), 5));

        VBox tarjeta = new VBox(10, miniatura, lblPrecio, nombre, etiquetasPrevia, cuando, donde, lblPlazas);        tarjeta.setAlignment(Pos.TOP_LEFT);
        tarjeta.getStyleClass().addAll("tarjeta-actividad", "sin-cursor");
        vistaPrevia.getChildren().setAll(tarjeta);
    }

    // Alertas
    private void mostrarInfo(String mensaje) {
        AlertUtil.info(mensaje);
    }

    private void mostrarError(String mensaje) {
        AlertUtil.error(mensaje);
    }
}
