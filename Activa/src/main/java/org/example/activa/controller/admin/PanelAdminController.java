package org.example.activa.controller.admin;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.example.activa.exception.ActividadException;
import org.example.activa.model.Actividad;
import org.example.activa.service.ActividadService;
import org.example.activa.util.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PanelAdminController {

    // Formatos en español
    private static final Locale ES = Locale.forLanguageTag("es-ES");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("EEE d MMM", ES);
    private static final NumberFormat FORMATO_PRECIO = NumberFormat.getCurrencyInstance(ES);

    // Ancho de cada columna (cabecera y filas usan los mismos)
    private static final double ANCHO_INSCRITOS = 140;
    private static final double ANCHO_INGRESOS = 110;
    private static final double ANCHO_ESTADO = 100;
    private static final double ANCHO_ACCIONES = 200;

    // Elementos del FXML
    @FXML private VBox contenido;
    @FXML private Button btnCrear;
    @FXML private HBox kpis;
    @FXML private VBox panel;
    @FXML private Label lblTotal;
    @FXML private ToggleGroup grupoFiltro;
    @FXML private ToggleButton tabProximas;
    @FXML private ToggleButton tabPasadas;
    @FXML private ToggleButton tabTodas;
    @FXML private HBox cabeceraTabla;
    @FXML private VBox filas;

    // Services
    private final ActividadService actividadService = new ActividadService();

    // Datos cargados
    private List<Actividad> actividades = new ArrayList<>();

    @FXML
    private void initialize() {
        // Solo pueden entrar administradores
        if (!Sesion.haySesion() || !Sesion.isAdmin()) {
            mostrarAccesoRestringido();
            return;
        }

        btnCrear.setGraphic(Iconos.crear("plus", 16));
        lblTotal.getStyleClass().addAll("pill", "neu");
        crearCabecera();
        redondearPanel();

        grupoFiltro.selectedToggleProperty().addListener((obs, anterior, nueva) -> {
            if (nueva == null) {
                anterior.setSelected(true);
            } else {
                pintarFilas();
            }
        });

        cargarDatos();
    }

    // Lee todas las actividades de la BD
    private void cargarDatos() {
        try {
            actividades = actividadService.listarTodas();
            pintarKpis();
            pintarFilas();
        } catch (ActividadException e) {
            mostrarError(e.getMessage());
        }
    }

    // Tarjetas de resumen

    private void pintarKpis() {
        List<Actividad> proximas = actividades.stream().filter(a -> !esPasada(a)).toList();

        long estaSemana = proximas.stream()
                .filter(a -> !a.getFechaActividad().isAfter(LocalDate.now().plusDays(7)))
                .count();
        int inscritos = proximas.stream().mapToInt(Actividad::getCupoActual).sum();
        int libres = proximas.stream().mapToInt(Actividad::getPlazasLibres).sum();
        long completas = proximas.stream().filter(Actividad::estaCompleta).count();
        BigDecimal ingresos = proximas.stream()
                .map(this::ingresosDe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        kpis.getChildren().setAll(
                crearKpi("calendar", "Actividades próximas", String.valueOf(proximas.size()),
                        estaSemana + " en los próximos 7 días"),
                crearKpi("users", "Personas inscritas", String.valueOf(inscritos),
                        "en actividades próximas"),
                crearKpi("wallet", "Ingresos previstos", FORMATO_PRECIO.format(ingresos),
                        "según plazas ocupadas"),
                crearKpi("listcheck", "Plazas libres", String.valueOf(libres),
                        completas == 1 ? "1 actividad completa" : completas + " actividades completas")
        );
    }

    private VBox crearKpi(String icono, String titulo, String valor, String comentario) {
        Label lblTitulo = new Label(titulo, Iconos.crear(icono, 15));
        lblTitulo.getStyleClass().add("kpi-titulo");

        Label lblValor = new Label(valor);
        lblValor.getStyleClass().add("kpi-valor");

        Label lblComentario = new Label(comentario);
        lblComentario.getStyleClass().add("kpi-comentario");

        VBox kpi = new VBox(8, lblTitulo, lblValor, lblComentario);
        kpi.getStyleClass().add("kpi");
        kpi.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(kpi, Priority.ALWAYS);
        return kpi;
    }

    // Tabla

    private void crearCabecera() {
        Label actividad = columnaCabecera("ACTIVIDAD");
        HBox.setHgrow(actividad, Priority.ALWAYS);
        actividad.setMaxWidth(Double.MAX_VALUE);

        Label ingresos = columnaCabecera("INGRESOS");
        ingresos.setAlignment(Pos.CENTER_RIGHT);

        cabeceraTabla.getChildren().setAll(
                actividad,
                fijarAncho(columnaCabecera("INSCRITOS"), ANCHO_INSCRITOS),
                fijarAncho(ingresos, ANCHO_INGRESOS),
                fijarAncho(columnaCabecera("ESTADO"), ANCHO_ESTADO),
                fijarAncho(new Region(), ANCHO_ACCIONES));
    }

    private Label columnaCabecera(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("th");
        return label;
    }

    private void pintarFilas() {
        List<Actividad> visibles;
        if (tabPasadas.isSelected()) {
            visibles = actividades.stream().filter(this::esPasada)
                    .sorted(Comparator.comparing(Actividad::getFechaActividad).reversed())
                    .toList();
        } else if (tabTodas.isSelected()) {
            visibles = actividades;
        } else {
            visibles = actividades.stream().filter(a -> !esPasada(a)).toList();
        }

        lblTotal.setText(String.valueOf(visibles.size()));
        filas.getChildren().clear();

        if (visibles.isEmpty()) {
            Label vacio = new Label("No hay actividades en este apartado.");
            vacio.getStyleClass().add("vacio");
            HBox caja = new HBox(vacio);
            caja.getStyleClass().add("fila-tabla");
            filas.getChildren().add(caja);
            return;
        }

        for (Actividad a : visibles) {
            filas.getChildren().add(crearFila(a));
        }
    }

    private HBox crearFila(Actividad a) {
        // Actividad: miniatura, nombre y fecha
        Label nombre = new Label(a.getNombreActividad());
        nombre.getStyleClass().add("fila-nombre");
        Label datos = new Label(FORMATO_FECHA.format(a.getFechaActividad()) + " · " + a.getHoraInicio());
        datos.getStyleClass().add("fila-datos");

        HBox colActividad = new HBox(12, EstiloActividad.miniatura(a, 40), new VBox(2, nombre, datos));
        colActividad.setAlignment(Pos.CENTER_LEFT);
        colActividad.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(colActividad, Priority.ALWAYS);

        // Inscritos: número y barra
        Label numero = new Label(a.getCupoActual() + " / " + a.getCupoMax());
        numero.getStyleClass().add("numero-cupo");
        ProgressBar barra = new ProgressBar((double) a.getCupoActual() / a.getCupoMax());
        barra.setMaxWidth(Double.MAX_VALUE);
        barra.getStyleClass().add("barra-plazas");
        if (a.estaCompleta()) {
            barra.getStyleClass().add("llena");
        }
        VBox colInscritos = new VBox(6, numero, barra);
        colInscritos.setAlignment(Pos.CENTER_LEFT);

        // Ingresos previstos
        Label ingresos = new Label(a.esGratis() ? "—" : FORMATO_PRECIO.format(ingresosDe(a)));
        ingresos.getStyleClass().add("numero-cupo");
        ingresos.setAlignment(Pos.CENTER_RIGHT);

        // Estado
        HBox colEstado = new HBox(crearEstado(a));
        colEstado.setAlignment(Pos.CENTER_LEFT);

        // Acciones
        Button gestionar = new Button("Gestionar", Iconos.crear("users", 14));
        gestionar.getStyleClass().add("boton-soft-sm");
        gestionar.setOnAction(e -> onGestionar(a));

        Button editar = new Button(null, Iconos.crear("pencil", 16));
        editar.getStyleClass().add("iconbtn-sm");
        editar.setTooltip(new Tooltip("Editar"));
        editar.setOnAction(e -> onEditar(a));

        Button eliminar = new Button(null, Iconos.crear("trash", 16));
        eliminar.getStyleClass().addAll("iconbtn-sm", "peligro");
        eliminar.setTooltip(new Tooltip("Eliminar"));
        eliminar.setOnAction(e -> onEliminar(a));

        HBox colAcciones = new HBox(6, gestionar, editar, eliminar);
        colAcciones.setAlignment(Pos.CENTER_RIGHT);

        HBox fila = new HBox(16,
                colActividad,
                fijarAncho(colInscritos, ANCHO_INSCRITOS),
                fijarAncho(ingresos, ANCHO_INGRESOS),
                fijarAncho(colEstado, ANCHO_ESTADO),
                fijarAncho(colAcciones, ANCHO_ACCIONES));
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("fila-tabla");
        return fila;
    }

    private Label crearEstado(Actividad a) {
        Label pill = new Label();
        pill.getStyleClass().add("pill");
        if (esPasada(a)) {
            pill.setText("Pasada");
            pill.getStyleClass().add("neu");
        } else if (a.estaCompleta()) {
            pill.setText("Completa");
            pill.getStyleClass().add("info");
        } else {
            pill.setText("Publicada");
            pill.getStyleClass().add("ok");
        }
        return pill;
    }

    // Acciones

    @FXML
    private void onCrear() {
        Navegador.irA(Vista.EDITOR_ACTIVIDAD,
                (EditorActividadController editor) -> editor.nueva());
    }

    private void onEditar(Actividad a) {
        Navegador.irA(Vista.EDITOR_ACTIVIDAD,
                (EditorActividadController editor) -> editor.editar(a));
    }

    private void onGestionar(Actividad a) {
        Navegador.irA(Vista.GESTIONAR_INSCRITOS,
                (GestionarInscritosController gestion) -> gestion.setActividad(a));
    }

    private void onEliminar(Actividad a) {
        String aviso = "¿Seguro que quieres eliminar \"" + a.getNombreActividad() + "\"?";
        if (a.getCupoActual() > 0) {
            aviso += "\n\nHay " + a.getCupoActual() + (a.getCupoActual() == 1 ? " persona inscrita." : " personas inscritas.")
                    + " También se borrarán sus inscripciones y pagos.";
        }
        if (!AlertUtil.confirmarPeligro("Eliminar actividad", aviso, "Eliminar", "Cancelar")) {
            return;
        }
        try {
            actividadService.eliminar(a.getIdActividad());
            mostrarInfo("Actividad eliminada.");
        } catch (ActividadException e) {
            mostrarError(e.getMessage());
        }
        cargarDatos();
    }

    // Métodos privados

    private boolean esPasada(Actividad a) {
        return a.getFechaActividad().isBefore(LocalDate.now());
    }

    // Precio × plazas ocupadas
    private BigDecimal ingresosDe(Actividad a) {
        return a.getPrecio().multiply(BigDecimal.valueOf(a.getCupoActual()));
    }

    private <T extends Region> T fijarAncho(T nodo, double ancho) {
        nodo.setMinWidth(ancho);
        nodo.setPrefWidth(ancho);
        nodo.setMaxWidth(ancho);
        return nodo;
    }

    private void redondearPanel() {
        Rectangle recorte = new Rectangle();
        recorte.setArcWidth(36);
        recorte.setArcHeight(36);
        recorte.widthProperty().bind(panel.widthProperty());
        recorte.heightProperty().bind(panel.heightProperty());
        panel.setClip(recorte);
    }

    // Mensaje si entra alguien que no es admin
    private void mostrarAccesoRestringido() {
        StackPane icono = new StackPane(Iconos.crear("shield", 26));
        icono.getStyleClass().add("vacio-icono");

        Label titulo = new Label("Acceso restringido");
        titulo.getStyleClass().add("vacio-titulo");

        Label texto = new Label("Esta sección solo está disponible para administradores.");
        texto.getStyleClass().add("vacio-texto");

        VBox caja = new VBox(14, icono, titulo, texto);
        caja.setAlignment(Pos.CENTER);
        caja.getStyleClass().add("vacio-caja");
        contenido.getChildren().setAll(caja);
    }

    // Alertas
    private void mostrarInfo(String mensaje) {
        AlertUtil.info(mensaje);
    }

    private void mostrarError(String mensaje) {
        AlertUtil.error(mensaje);
    }

    private boolean confirmar(String mensaje) {
        return AlertUtil.confirmar(mensaje);
    }
}
