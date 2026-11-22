package org.example.activa.controller.usuario;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import org.example.activa.exception.ActividadException;
import org.example.activa.exception.InscripcionException;
import org.example.activa.model.Actividad;
import org.example.activa.model.Inscripcion;
import org.example.activa.service.ActividadService;
import org.example.activa.service.InscripcionService;
import org.example.activa.util.*;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.SetChangeListener;
import javafx.collections.WeakSetChangeListener;

public class InicioController {
    
    // Formatos en español
    private static final Locale ES = Locale.forLanguageTag("es-ES");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("EEE d MMM", ES);
    private static final NumberFormat FORMATO_PRECIO = NumberFormat.getCurrencyInstance(ES);

    // Elementos del FXML
    @FXML private FlowPane panelTarjetas;
    @FXML private Label lblResumen;
    @FXML private Label lblVacio;

    // Services
    private final ActividadService actividadService = new ActividadService();
    private final InscripcionService inscripcionService = new InscripcionService();

    // Datos cargados
    private List<Actividad> actividades = new ArrayList<>();
    private final Set<Integer> misActividades = new HashSet<>();
    // Se ejecuta cada vez que cambia el texto del buscador
    private final ChangeListener<String> alBuscar = (obs, anterior, nuevo) -> mostrarActividades();
    // Se ejecuta cada vez que cambian las etiquetas del filtro
    private final SetChangeListener<String> alFiltrar = cambio -> mostrarActividades();

    @FXML
    private void initialize() {
        Busqueda.textoProperty().addListener(new WeakChangeListener<>(alBuscar));
        Busqueda.etiquetas().addListener(new WeakSetChangeListener<>(alFiltrar));
        cargarDatos();
    }

    // Lee actividades e inscripciones de la BD
    private void cargarDatos() {
        try {
            actividades = actividadService.listarProximas();

            misActividades.clear();
            if (Sesion.haySesion()) {
                for (Inscripcion i : inscripcionService.listarMisInscripciones(Sesion.getIdUsuario())) {
                    if (i.estaActiva()) {
                        misActividades.add(i.getIdActividad());
                    }
                }
            }

            mostrarActividades();
        } catch (ActividadException | InscripcionException e) {
            mostrarError(e.getMessage());
        }
    }

    // Pinta las tarjetas aplicando el filtro del buscador
    private void mostrarActividades() {
        String texto = Busqueda.getTexto().trim().toLowerCase();
        panelTarjetas.getChildren().clear();

        int mostradas = 0;
        for (Actividad a : actividades) {
            boolean coincideTexto = texto.isEmpty()
                    || a.getNombreActividad().toLowerCase().contains(texto)
                    || a.getLugar().toLowerCase().contains(texto)
                    || a.getEtiquetas().stream().anyMatch(e -> e.toLowerCase().contains(texto));

            boolean coincideEtiquetas = Busqueda.etiquetas().isEmpty()
                    || a.getEtiquetas().stream().anyMatch(Busqueda.etiquetas()::contains);

            boolean coincide = coincideTexto && coincideEtiquetas;

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
        Label precio = new Label(a.esGratis() ? "Gratis" : FORMATO_PRECIO.format(a.getPrecio()));
        precio.getStyleClass().add("etiqueta-precio");
        if (a.esGratis()) {
            precio.getStyleClass().add("gratis");
        }

        Label nombre = new Label(a.getNombreActividad());
        nombre.getStyleClass().add("tarjeta-nombre");
        nombre.setWrapText(true);

        Label fecha = new Label(FORMATO_FECHA.format(a.getFechaActividad())
                + " · " + a.getHoraInicio() + " – " + a.getHoraFin());
        fecha.getStyleClass().add("tarjeta-dato");

        Label lugar = new Label(a.getLugar());
        lugar.getStyleClass().add("tarjeta-dato");
        lugar.setWrapText(true);

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

        Region hueco = new Region();
        VBox.setVgrow(hueco, Priority.ALWAYS);

        FlowPane etiquetas = new FlowPane(6, 6);
        etiquetas.getChildren().setAll(EstiloActividad.pastillas(a.getEtiquetas(), 3));

        // Foto con el precio encima, en la esquina
        StackPane portada = EstiloActividad.portada(a, 248, 140);
        StackPane.setAlignment(precio, Pos.TOP_RIGHT);
        StackPane.setMargin(precio, new Insets(10));
        portada.getChildren().add(precio);

        VBox tarjeta = new VBox(portada, nombre, etiquetas, fecha, lugar, barra, plazas, hueco, crearBoton(a));
        tarjeta.getStyleClass().add("tarjeta-actividad");
        tarjeta.setPrefWidth(280);
        tarjeta.setMinHeight(400);

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

    // Abre el detalle con el Navegador
    private void abrirDetalle(Actividad a) {
        Navegador.irA(Vista.DETALLE_ACTIVIDAD,
                (DetalleActividadController detalle) ->
                        detalle.setActividad(a, () -> Navegador.irA(Vista.INICIO)));
    }

    // Acción del botón "Apuntarme"
    private void inscribir(Actividad a) {
        if (!Sesion.haySesion()) {
            Acceso.pedirLogin("apuntarte");
            return;
        }
        try {
            Inscripcion i = inscripcionService.inscribir(Sesion.getIdUsuario(), a.getIdActividad());
            mostrarInfo(i.estaPendienteDePago()
                    ? "Plaza reservada en \"" + a.getNombreActividad() + "\". Completa el pago para confirmarla."
                    : "Te has apuntado a \"" + a.getNombreActividad() + "\".");
        } catch (InscripcionException e) {
            mostrarError(e.getMessage());
        }
        cargarDatos();
    }

    // Acción del botón "Cancelar inscripción"
    private void cancelar(Actividad a) {
        if (!AlertUtil.confirmarPeligro("Cancelar inscripción",
                "¿Seguro que quieres cancelar tu inscripción en \"" + a.getNombreActividad() + "\"?",
                "Cancelar inscripción", "Volver")) {
            return;
        }
        try {
            inscripcionService.cancelar(Sesion.getIdUsuario(), a.getIdActividad());
            mostrarInfo("Has cancelado tu inscripción.");
        } catch (InscripcionException e) {
            mostrarError(e.getMessage());
        }
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

    // Alertas
    private void mostrarInfo(String mensaje) {
        AlertUtil.info(mensaje);
    }

    private void mostrarError(String mensaje) {
        AlertUtil.error(mensaje);
    }
}