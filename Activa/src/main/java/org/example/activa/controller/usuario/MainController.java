package org.example.activa.controller.usuario;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import org.example.activa.util.Busqueda;
import org.example.activa.util.GestorApariencia;
import org.example.activa.util.Iconos;
import org.example.activa.util.Navegador;
import org.example.activa.util.Sesion;
import org.example.activa.util.Vista;
import org.example.activa.util.Etiquetas;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.image.ImageView;
import org.example.activa.util.Imagenes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Controlador principal de la aplicación: gestiona la navegación lateral, la barra superior y la sesión
public class MainController {

    // Elementos del FXML
    @FXML private BorderPane contenedor;
    @FXML private StackPane marca;
    @FXML private Button btnInicio;
    @FXML private Button btnMisInscripciones;
    @FXML private Button btnCalendario;
    @FXML private Button btnPagos;
    @FXML private VBox grupoAdmin;
    @FXML private StackPane ayuda;
    @FXML private Button btnAsistente;
    @FXML private HBox cajaBuscar;
    @FXML private TextField txtBuscar;
    @FXML private Button btnNotificaciones;
    @FXML private HBox botonesInvitado;
    @FXML private MenuButton menuUsuario;
    @FXML private Label lblUsuario;
    @FXML private Label lblRol;
    @FXML private Label lblInicial;
    @FXML private Button btnPanelAdmin;
    @FXML private Button btnAjustes;
    @FXML private MenuButton menuEtiquetas;

    // Pantalla que se está viendo actualmente
    private Vista vistaActual;
    // Se ejecuta cuando cambia la foto de perfil
    private final ChangeListener<byte[]> alCambiarFoto = (obs, antes, ahora) -> pintarAvatar();
    // Chips del filtro de etiquetas
    private final List<ToggleButton> chipsFiltro = new ArrayList<>();

    // Inicializa iconos, sesión, buscador y la apariencia global
    @FXML
    private void initialize() {
        ponerIconos();
        redondearAyuda();
        configurarUsuario();

        // Buscador de la barra superior: redirige a Inicio al escribir
        txtBuscar.textProperty().bindBidirectional(Busqueda.textoProperty());
        txtBuscar.textProperty().addListener((obs, anterior, nuevo) -> {
            if (!nuevo.isBlank() && vistaActual != Vista.INICIO) {
                Navegador.irA(Vista.INICIO);
            }
        });
        crearFiltroEtiquetas();

        Navegador.setContenedor(contenedor, this::marcarActivo);
        Navegador.irA(Vista.INICIO);

        // Aplica las preferencias guardadas en cuanto la vista está montada en la Scene
        Platform.runLater(() -> GestorApariencia.aplicar(contenedor));
    }

    // Botones de la barra lateral

    @FXML
    private void onInicio() {
        Navegador.irA(Vista.INICIO);
    }

    @FXML
    private void onMisInscripciones() {
        Navegador.irA(Vista.MIS_INSCRIPCIONES);
    }

    @FXML
    private void onCalendario() {
        Navegador.irA(Vista.CALENDARIO);
    }

    @FXML
    private void onPagos() {
        Navegador.irA(Vista.PAGOS);
    }

    @FXML
    private void onPanelAdmin() {
        Navegador.irA(Vista.PANEL_ADMIN);
    }

    @FXML
    private void onAjustes() {
        Navegador.irA(Vista.AJUSTES);
    }

    // Botones de invitado

    @FXML
    private void onIniciarSesion() {
        Navegador.mostrarPantallaCompleta(Vista.LOGIN);
    }

    @FXML
    private void onCrearCuenta() {
        Navegador.mostrarPantallaCompleta(Vista.REGISTRO);
    }


    // Menú del usuario

    @FXML
    private void onCerrarSesion() {
        Sesion.cerrar();
        Busqueda.textoProperty().set("");
        Busqueda.etiquetas().clear();
        Navegador.mostrarPantallaCompleta(Vista.LOGIN);
    }

    // Métodos privados

    // Muestra el perfil del usuario conectado o los botones de invitado según haya sesión
    private void configurarUsuario() {
        boolean conCuenta = Sesion.haySesion();

        mostrar(menuUsuario, conCuenta);
        mostrar(btnNotificaciones, conCuenta);
        mostrar(botonesInvitado, !conCuenta);
        mostrar(grupoAdmin, conCuenta && Sesion.isAdmin());

        if (conCuenta) {
            lblUsuario.setText(Sesion.getNombre());
            lblRol.setText(Sesion.isAdmin() ? "Administración" : "Miembro de Activa");
            pintarAvatar();
            Sesion.fotoProperty().addListener(new WeakChangeListener<>(alCambiarFoto));
        }
    }

    // Carga los iconos SVG en los botones de navegación y barras
    private void ponerIconos() {
        marca.getChildren().add(Iconos.crear("leaf", 18));
        btnInicio.setGraphic(Iconos.crear("house", 18));
        btnMisInscripciones.setGraphic(Iconos.crear("listcheck", 18));
        btnCalendario.setGraphic(Iconos.crear("calendar", 18));
        btnPagos.setGraphic(Iconos.crear("card", 18));
        btnAsistente.setGraphic(Iconos.crear("chat", 15));
        btnNotificaciones.setGraphic(Iconos.crear("bell", 18));
        cajaBuscar.getChildren().add(0, Iconos.crear("search", 18));
        btnPanelAdmin.setGraphic(Iconos.crear("shield", 18));
        btnAjustes.setGraphic(Iconos.crear("settings", 18));
    }

    // Aplica esquinas redondeadas a la tarjeta lateral de ayuda
    private void redondearAyuda() {
        Rectangle recorte = new Rectangle();
        recorte.setArcWidth(32);
        recorte.setArcHeight(32);
        recorte.widthProperty().bind(ayuda.widthProperty());
        recorte.heightProperty().bind(ayuda.heightProperty());
        ayuda.setClip(recorte);
    }

    // Resalta en el menú lateral el botón correspondiente a la pantalla activa
    private void marcarActivo(Vista vista) {
        vistaActual = vista;
        btnInicio.getStyleClass().remove("activo");
        btnMisInscripciones.getStyleClass().remove("activo");
        btnCalendario.getStyleClass().remove("activo");
        btnPagos.getStyleClass().remove("activo");
        btnPanelAdmin.getStyleClass().remove("activo");
        btnAjustes.getStyleClass().remove("activo");

        switch (vista) {
            case INICIO, DETALLE_ACTIVIDAD -> btnInicio.getStyleClass().add("activo");
            case MIS_INSCRIPCIONES -> btnMisInscripciones.getStyleClass().add("activo");
            case CALENDARIO -> btnCalendario.getStyleClass().add("activo");
            case PAGOS -> btnPagos.getStyleClass().add("activo");
            case PANEL_ADMIN, EDITOR_ACTIVIDAD, GESTIONAR_INSCRITOS -> btnPanelAdmin.getStyleClass().add("activo");
            case AJUSTES -> btnAjustes.getStyleClass().add("activo");
            default -> { }
        }
    }

    // Muestra u oculta un nodo sin que ocupe espacio en el layout cuando está oculto
    private void mostrar(Node nodo, boolean visible) {
        nodo.setVisible(visible);
        nodo.setManaged(visible);
    }

    // Panel con las etiquetas agrupadas y con scroll
    private void crearFiltroEtiquetas() {
        menuEtiquetas.setGraphic(Iconos.crear("tag", 16));

        // Cabecera: título + "Quitar filtro"
        Label titulo = new Label("Filtrar por etiquetas");
        titulo.getStyleClass().add("filtro-titulo");

        Button quitar = new Button("Quitar filtro");
        quitar.getStyleClass().add("filtro-quitar");
        quitar.setOnAction(e -> chipsFiltro.forEach(c -> c.setSelected(false)));

        Region hueco = new Region();
        HBox.setHgrow(hueco, Priority.ALWAYS);
        HBox cabecera = new HBox(8, titulo, hueco, quitar);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        // Grupos de chips (Deporte, Arte...)
        VBox grupos = new VBox(14);
        for (Map.Entry<String, List<String>> grupo : Etiquetas.POR_GRUPO.entrySet()) {
            Label nombreGrupo = new Label(grupo.getKey().toUpperCase());
            nombreGrupo.getStyleClass().add("eyebrow");

            FlowPane chips = new FlowPane(6, 6);
            chips.setPrefWrapLength(270);
            for (String etiqueta : grupo.getValue()) {
                ToggleButton chip = new ToggleButton(etiqueta);
                chip.getStyleClass().addAll("chip", "chip-pequeno");
                chip.selectedProperty().addListener((obs, antes, marcada) -> alCambiarEtiqueta(etiqueta, marcada));
                chipsFiltro.add(chip);
                chips.getChildren().add(chip);
            }
            grupos.getChildren().add(new VBox(6, nombreGrupo, chips));
        }

        // Scroll con altura máxima
        ScrollPane scroll = new ScrollPane(grupos);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefViewportHeight(260);
        scroll.getStyleClass().add("filtro-scroll");

        VBox panel = new VBox(12, cabecera, scroll);
        panel.setPrefWidth(320);
        panel.getStyleClass().add("filtro-panel");

        // Todo el panel va dentro del menú, y no se cierra al pulsar un chip
        CustomMenuItem item = new CustomMenuItem(panel, false);
        item.getStyleClass().add("filtro-item");
        menuEtiquetas.getItems().setAll(item);
    }

    // Al marcar o desmarcar un chip
    private void alCambiarEtiqueta(String etiqueta, boolean marcada) {
        if (marcada) {
            Busqueda.etiquetas().add(etiqueta);
        } else {
            Busqueda.etiquetas().remove(etiqueta);
        }
        actualizarBotonEtiquetas();
        if (vistaActual != Vista.INICIO) {
            Navegador.irA(Vista.INICIO);
        }
    }

    // "Etiquetas · 2" y resaltado si hay alguna marcada
    private void actualizarBotonEtiquetas() {
        int marcadas = Busqueda.etiquetas().size();
        menuEtiquetas.setText(marcadas == 0 ? "Etiquetas" : "Etiquetas · " + marcadas);
        menuEtiquetas.getStyleClass().remove("activo");
        if (marcadas > 0) {
            menuEtiquetas.getStyleClass().add("activo");
        }
    }
    // Foto de perfil si la hay; si no, las iniciales
    private void pintarAvatar() {
        ImageView foto = Imagenes.circular(Sesion.getFoto(), 38);
        if (foto != null) {
            lblInicial.setText("");
            lblInicial.setGraphic(foto);
            lblInicial.getStyleClass().add("con-foto");
        } else {
            lblInicial.setGraphic(null);
            lblInicial.setText(Sesion.getIniciales());
            lblInicial.getStyleClass().remove("con-foto");
        }
    }
}