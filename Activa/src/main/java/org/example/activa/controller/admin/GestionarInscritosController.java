package org.example.activa.controller.admin;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.example.activa.exception.ActividadException;
import org.example.activa.exception.InscripcionException;
import org.example.activa.model.Actividad;
import org.example.activa.model.enums.EstadoInscripcion;
import org.example.activa.model.Inscrito;
import org.example.activa.service.ActividadService;
import org.example.activa.service.InscripcionService;
import org.example.activa.util.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GestionarInscritosController {

    // Formatos en español
    private static final Locale ES = Locale.forLanguageTag("es-ES");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ES);
    private static final DateTimeFormatter FORMATO_INSCRIPCION = DateTimeFormatter.ofPattern("d MMM, HH:mm", ES);

    // Colores de los avatares
    private static final String[] COLORES = {"lago", "terra", "mostaza", "ciruela"};

    // Ancho de las columnas
    private static final double ANCHO_FECHA = 140;
    private static final double ANCHO_ESTADO = 150;
    private static final double ANCHO_ACCIONES = 120;

    // Elementos del FXML
    @FXML private VBox contenido;
    @FXML private StackPane cajaMiniatura;
    @FXML private Label lblNombre;
    @FXML private Label lblDatos;
    @FXML private Button btnEditar;
    @FXML private HBox kpis;
    @FXML private VBox panel;
    @FXML private ToggleGroup grupoFiltro;
    @FXML private ToggleButton tabInscritos;
    @FXML private ToggleButton tabCancelados;
    @FXML private HBox cajaBuscar;
    @FXML private TextField txtBuscar;
    @FXML private HBox cabeceraTabla;
    @FXML private VBox filas;

    // Services
    private final ActividadService actividadService = new ActividadService();
    private final InscripcionService inscripcionService = new InscripcionService();

    // Datos de la pantalla
    private Actividad actividad;
    private List<Inscrito> inscritos = new ArrayList<>();

    @FXML
    private void initialize() {
        // Solo pueden entrar administradores
        if (!Sesion.haySesion() || !Sesion.isAdmin()) {
            contenido.getChildren().setAll(new Label("Esta sección solo está disponible para administradores."));
            return;
        }

        btnEditar.setGraphic(Iconos.crear("pencil", 16));
        cajaBuscar.getChildren().add(0, Iconos.crear("search", 16));
        crearCabecera();
        redondearPanel();

        grupoFiltro.selectedToggleProperty().addListener((obs, anterior, nueva) -> {
            if (nueva == null) {
                anterior.setSelected(true);
            } else {
                pintarFilas();
            }
        });
        txtBuscar.textProperty().addListener((obs, anterior, nuevo) -> pintarFilas());
    }

    // Lo llama el panel al abrir esta pantalla
    public void setActividad(Actividad actividad) {
        this.actividad = actividad;
        cargarDatos();
    }

    // Vuelve a leer la actividad y sus inscritos
    private void cargarDatos() {
        try {
            actividad = actividadService.buscarPorId(actividad.getIdActividad());
            inscritos = inscripcionService.listarInscritosConDatos(actividad.getIdActividad());
            pintarCabecera();
            pintarKpis();
            pintarFilas();
        } catch (ActividadException | InscripcionException e) {
            mostrarError(e.getMessage());
        }
    }

    // Cabecera

    private void pintarCabecera() {
        cajaMiniatura.getChildren().setAll(EstiloActividad.miniatura(actividad, 84));
        lblNombre.setText(actividad.getNombreActividad());
        String fecha = FORMATO_FECHA.format(actividad.getFechaActividad());
        lblDatos.setText(Character.toUpperCase(fecha.charAt(0)) + fecha.substring(1)
                + " · " + actividad.getHoraInicio() + " – " + actividad.getHoraFin()
                + " · " + actividad.getLugar());
    }

    // Resumen

    private void pintarKpis() {
        long activos = inscritos.stream().filter(Inscrito::estaActiva).count();
        long confirmados = inscritos.stream().filter(i -> i.estado() == EstadoInscripcion.CONFIRMADA).count();
        long pendientes = inscritos.stream().filter(i -> i.estado() == EstadoInscripcion.PENDIENTE_PAGO).count();
        long cancelados = inscritos.stream().filter(i -> i.estado() == EstadoInscripcion.CANCELADA).count();

        // Inscritos con barra
        ProgressBar barra = new ProgressBar((double) actividad.getCupoActual() / actividad.getCupoMax());
        barra.setMaxWidth(Double.MAX_VALUE);
        barra.getStyleClass().add("barra-plazas");
        if (actividad.estaCompleta()) {
            barra.getStyleClass().add("llena");
        }
        VBox kpiInscritos = crearKpi("Inscritos", actividad.getCupoActual() + " / " + actividad.getCupoMax(), null);
        kpiInscritos.getChildren().add(barra);

        VBox segundo = actividad.esGratis()
                ? crearKpi("Plazas libres", String.valueOf(actividad.getPlazasLibres()), "actividad gratuita")
                : crearKpi("Pagados", String.valueOf(confirmados), pendientes + " pendientes de pago");

        kpis.getChildren().setAll(
                kpiInscritos,
                segundo,
                crearKpi("Confirmados", String.valueOf(confirmados), "de " + activos + " inscripciones activas"),
                crearKpi("Cancelados", String.valueOf(cancelados), "bajas y expulsiones"));
    }

    private VBox crearKpi(String titulo, String valor, String comentario) {
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("kpi-titulo");
        Label lblValor = new Label(valor);
        lblValor.getStyleClass().add("kpi-valor");

        VBox kpi = new VBox(8, lblTitulo, lblValor);
        if (comentario != null) {
            Label lblComentario = new Label(comentario);
            lblComentario.getStyleClass().add("kpi-comentario");
            kpi.getChildren().add(lblComentario);
        }
        kpi.getStyleClass().add("kpi");
        kpi.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(kpi, Priority.ALWAYS);
        return kpi;
    }

    // Lista de personas

    private void crearCabecera() {
        Label persona = columnaCabecera("PERSONA");
        persona.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(persona, Priority.ALWAYS);

        cabeceraTabla.getChildren().setAll(
                persona,
                fijarAncho(columnaCabecera("INSCRIPCIÓN"), ANCHO_FECHA),
                fijarAncho(columnaCabecera("ESTADO"), ANCHO_ESTADO),
                fijarAncho(new Region(), ANCHO_ACCIONES));
    }

    private Label columnaCabecera(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("th");
        return label;
    }

    private void pintarFilas() {
        boolean verCancelados = tabCancelados.isSelected();
        String texto = txtBuscar.getText().trim().toLowerCase();

        List<Inscrito> visibles = inscritos.stream()
                .filter(i -> verCancelados ? !i.estaActiva() : i.estaActiva())
                .filter(i -> texto.isEmpty()
                        || i.nombre().toLowerCase().contains(texto)
                        || i.email().toLowerCase().contains(texto))
                .toList();

        long activos = inscritos.stream().filter(Inscrito::estaActiva).count();
        tabInscritos.setText("Inscritos · " + activos);
        tabCancelados.setText("Cancelados · " + (inscritos.size() - activos));

        filas.getChildren().clear();
        if (visibles.isEmpty()) {
            Label vacio = new Label(!texto.isEmpty() ? "Nadie coincide con la búsqueda."
                    : verCancelados ? "No hay inscripciones canceladas."
                    : "Todavía no se ha apuntado nadie.");
            vacio.getStyleClass().add("vacio");
            HBox caja = new HBox(vacio);
            caja.getStyleClass().add("fila-tabla");
            filas.getChildren().add(caja);
            return;
        }

        for (Inscrito i : visibles) {
            filas.getChildren().add(crearFila(i));
        }
    }

    private HBox crearFila(Inscrito i) {
        // Persona: avatar, nombre y email
        Label avatar = new Label(i.iniciales());
        avatar.getStyleClass().addAll("avatar-persona", "cat-" + COLORES[Math.floorMod(i.idUsuario(), COLORES.length)]);

        Label nombre = new Label(i.nombre());
        nombre.getStyleClass().add("fila-nombre");
        Label email = new Label(i.email());
        email.getStyleClass().add("fila-datos");

        HBox colPersona = new HBox(12, avatar, new VBox(2, nombre, email));
        colPersona.setAlignment(Pos.CENTER_LEFT);
        colPersona.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(colPersona, Priority.ALWAYS);

        // Fecha de inscripción
        Label fecha = new Label(i.fechaInscripcion() == null ? "—" : FORMATO_INSCRIPCION.format(i.fechaInscripcion()));
        fecha.getStyleClass().add("fila-datos");

        // Estado
        HBox colEstado = new HBox(crearEstado(i));
        colEstado.setAlignment(Pos.CENTER_LEFT);

        // Acciones
        HBox colAcciones = new HBox();
        colAcciones.setAlignment(Pos.CENTER_RIGHT);
        if (i.estaActiva()) {
            Button expulsar = new Button("Expulsar", Iconos.crear("userMinus", 14));
            expulsar.getStyleClass().add("boton-dsoft-sm");
            expulsar.setOnAction(e -> onExpulsar(i));
            colAcciones.getChildren().add(expulsar);
        }

        HBox fila = new HBox(16,
                colPersona,
                fijarAncho(fecha, ANCHO_FECHA),
                fijarAncho(colEstado, ANCHO_ESTADO),
                fijarAncho(colAcciones, ANCHO_ACCIONES));
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("fila-tabla");
        return fila;
    }

    private Label crearEstado(Inscrito i) {
        Label pill = new Label();
        pill.getStyleClass().add("pill");
        switch (i.estado()) {
            case CONFIRMADA -> {
                pill.setText(actividad.esGratis() ? "Confirmada" : "Pagado");
                pill.getStyleClass().add("ok");
            }
            case PENDIENTE_PAGO -> {
                pill.setText("Pendiente de pago");
                pill.getStyleClass().add("warn");
            }
            case CANCELADA -> {
                pill.setText("Cancelada");
                pill.getStyleClass().add("bad");
            }
        }
        return pill;
    }

    // Acciones

    @FXML
    private void onVolver() {
        Navegador.irA(Vista.PANEL_ADMIN);
    }

    @FXML
    private void onEditar() {
        Navegador.irA(Vista.EDITOR_ACTIVIDAD,
                (EditorActividadController editor) -> editor.editar(actividad));
    }

    private void onExpulsar(Inscrito i) {
        if (!AlertUtil.confirmarPeligro("Expulsar a " + i.nombre(),
                "Saldrá de \"" + actividad.getNombreActividad() + "\" y su plaza quedará libre.",
                "Expulsar", "Cancelar")) {
            return;
        }
        try {
            inscripcionService.expulsar(i.idUsuario(), actividad.getIdActividad());
            mostrarInfo(i.nombre() + " ya no está inscrito en esta actividad.");
        } catch (InscripcionException e) {
            mostrarError(e.getMessage());
        }
        cargarDatos();
    }

    // Métodos privados

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

    // Alertas
    private void mostrarInfo(String mensaje) {
        AlertUtil.info(mensaje);
    }

    private void mostrarError(String mensaje) {
        AlertUtil.error(mensaje);
    }
}
