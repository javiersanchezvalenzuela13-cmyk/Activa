package org.example.activa.controller.usuario;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.activa.exception.ActividadException;
import org.example.activa.exception.InscripcionException;
import org.example.activa.model.Actividad;
import org.example.activa.model.Inscripcion;
import org.example.activa.service.ActividadService;
import org.example.activa.service.InscripcionService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class InicioController {

    // Usuario de prueba hasta que exista Sesion (Persona 1)
    private static final int ID_USUARIO_PRUEBA = 1;

    // Formatos en español
    private static final Locale ES = Locale.forLanguageTag("es-ES");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("EEE d MMM", ES);
    private static final NumberFormat FORMATO_PRECIO = NumberFormat.getCurrencyInstance(ES);

    // Elementos del FXML
    @FXML private TextField txtBuscar;
    @FXML private FlowPane panelTarjetas;
    @FXML private Label lblResumen;
    @FXML private Label lblVacio;
    @FXML private BorderPane raiz;

    // Contenido del inicio, para restaurarlo al volver del detalle
    private Node contenidoInicio;

    // Services
    private final ActividadService actividadService = new ActividadService();
    private final InscripcionService inscripcionService = new InscripcionService();

    // Datos cargados
    private List<Actividad> actividades = new ArrayList<>();
    private Set<Integer> misActividades = new HashSet<>();

    // Se ejecuta solo al cargar el FXML
    @FXML
    private void initialize() {
        txtBuscar.textProperty().addListener((obs, anterior, nuevo) -> mostrarActividades());
        cargarDatos();
    }

    // Lee actividades e inscripciones de la BD
    private void cargarDatos() {
        try {
            actividades = actividadService.listarProximas();

            misActividades.clear();
            for (Inscripcion i : inscripcionService.listarMisInscripciones(ID_USUARIO_PRUEBA)) {
                if (i.estaActiva()) {
                    misActividades.add(i.getIdActividad());
                }
            }

            mostrarActividades();
        } catch (ActividadException | InscripcionException e) {
            mostrarError(e.getMessage());
        }
    }

    // Pinta las tarjetas aplicando el filtro del buscador
    private void mostrarActividades() {
        String texto = txtBuscar.getText().trim().toLowerCase();
        panelTarjetas.getChildren().clear();

        int mostradas = 0;
        for (Actividad a : actividades) {
            boolean coincide = texto.isEmpty()
                    || a.getNombreActividad().toLowerCase().contains(texto)
                    || a.getLugar().toLowerCase().contains(texto);

            if (coincide) {
                panelTarjetas.getChildren().add(crearTarjeta(a));
                mostradas++;
            }
        }

        lblResumen.setText(mostradas == 1 ? "1 actividad" : mostradas + " actividades");
        lblVacio.setVisible(mostradas == 0);
        lblVacio.setManaged(mostradas == 0);
    }

    // Crea la tarjeta de una actividad
    private VBox crearTarjeta(Actividad a) {
        // Precio
        Label precio = new Label(a.esGratis() ? "Gratis" : FORMATO_PRECIO.format(a.getPrecio()));
        precio.getStyleClass().add("etiqueta-precio");
        if (a.esGratis()) {
            precio.getStyleClass().add("gratis");
        }

        // Nombre
        Label nombre = new Label(a.getNombreActividad());
        nombre.getStyleClass().add("tarjeta-nombre");
        nombre.setWrapText(true);

        // Fecha y lugar
        Label fecha = new Label(FORMATO_FECHA.format(a.getFechaActividad())
                + " · " + a.getHoraInicio() + " – " + a.getHoraFin());
        fecha.getStyleClass().add("tarjeta-dato");

        Label lugar = new Label(a.getLugar());
        lugar.getStyleClass().add("tarjeta-dato");
        lugar.setWrapText(true);

        // Barra de plazas
        ProgressBar barra = new ProgressBar((double) a.getCupoActual() / a.getCupoMax());
        barra.setMaxWidth(Double.MAX_VALUE);
        barra.getStyleClass().add("barra-plazas");
        if (a.estaCompleta()) {
            barra.getStyleClass().add("llena");
        } else if (a.getPlazasLibres() <= 3) {
            barra.getStyleClass().add("pocas");
        }

        Label plazas = new Label(textoPlazas(a));
        plazas.getStyleClass().add("texto-plazas");

        // Hueco para que el botón quede abajo
        Region hueco = new Region();
        VBox.setVgrow(hueco, Priority.ALWAYS);

        // Tarjeta completa
        VBox tarjeta = new VBox(precio, nombre, fecha, lugar, barra, plazas, hueco, crearBoton(a));
        tarjeta.getStyleClass().add("tarjeta-actividad");
        tarjeta.setPrefWidth(280);
        tarjeta.setMinHeight(260);
        // Clic en la tarjeta (menos en el botón) abre el detalle
        tarjeta.setOnMouseClicked(e -> {
            if (!vieneDeBoton(e.getTarget())) {
                abrirDetalle(a);
            }
        });
        return tarjeta;
    }

    // Botón según el estado: apuntarme, cancelar o completa
    private Button crearBoton(Actividad a) {
        Button boton = new Button();
        boton.setMaxWidth(Double.MAX_VALUE);

        if (misActividades.contains(a.getIdActividad())) {
            boton.setText("Cancelar inscripción");
            boton.getStyleClass().add("boton-cancelar");
            boton.setOnAction(e -> cancelar(a));
        } else if (a.estaCompleta()) {
            boton.setText("Completa");
            boton.getStyleClass().add("boton-apuntarme");
            boton.setDisable(true);
        } else {
            boton.setText("Apuntarme");
            boton.getStyleClass().add("boton-apuntarme");
            boton.setOnAction(e -> inscribir(a));
        }
        return boton;
    }

    // Acción del botón "Apuntarme"
    private void inscribir(Actividad a) {
        try {
            Inscripcion i = inscripcionService.inscribir(ID_USUARIO_PRUEBA, a.getIdActividad());

            if (i.estaPendienteDePago()) {
                mostrarInfo("Plaza reservada en \"" + a.getNombreActividad()
                        + "\". Completa el pago para confirmarla.");
            } else {
                mostrarInfo("Te has apuntado a \"" + a.getNombreActividad() + "\".");
            }
            cargarDatos();
        } catch (InscripcionException e) {
            mostrarError(e.getMessage());
            cargarDatos();
        }
    }

    // Acción del botón "Cancelar inscripción"
    private void cancelar(Actividad a) {
        if (!confirmar("¿Seguro que quieres cancelar tu inscripción en \"" + a.getNombreActividad() + "\"?")) {
            return;
        }
        try {
            inscripcionService.cancelar(ID_USUARIO_PRUEBA, a.getIdActividad());
            mostrarInfo("Has cancelado tu inscripción.");
            cargarDatos();
        } catch (InscripcionException e) {
            mostrarError(e.getMessage());
        }
    }
    // Abre la pantalla de detalle en la zona central
    private void abrirDetalle(Actividad a) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/activa/fxml/usuario/detalle-actividad-view.fxml"));
            Parent vista = loader.load();

            DetalleActividadController detalle = loader.getController();
            detalle.setActividad(a, this::volverDeDetalle);

            contenidoInicio = raiz.getCenter();
            raiz.setCenter(vista);
        } catch (IOException e) {
            mostrarError("No se pudo abrir la actividad.");
            e.printStackTrace();
        }
    }

    // Vuelve del detalle a las tarjetas
    private void volverDeDetalle() {
        raiz.setCenter(contenidoInicio);
        cargarDatos();
    }

    // Comprueba si el clic ha sido dentro de un botón
    private boolean vieneDeBoton(Object objetivo) {
        Node nodo = objetivo instanceof Node ? (Node) objetivo : null;
        while (nodo != null) {
            if (nodo instanceof Button) {
                return true;
            }
            nodo = nodo.getParent();
        }
        return false;
    }

    // Texto de plazas libres
    private String textoPlazas(Actividad a) {
        int libres = a.getPlazasLibres();
        if (libres == 0) return "Completa";
        if (libres == 1) return "Queda 1 plaza";
        return "Quedan " + libres + " plazas";
    }

    // Alertas (se moverán a AlertUtil cuando exista)
    private void mostrarInfo(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    private boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        alerta.setHeaderText(null);
        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.YES;
    }
}