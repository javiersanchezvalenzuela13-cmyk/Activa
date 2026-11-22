package org.example.activa.controller.usuario;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
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
import org.example.activa.model.Inscripcion;
import org.example.activa.service.ActividadService;
import org.example.activa.service.InscripcionService;
import org.example.activa.util.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class MisInscripcionesController {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("EEE d MMM", Locale.forLanguageTag("es-ES"));

    // Una inscripción junto a su actividad
    private record Fila(Inscripcion inscripcion, Actividad actividad) {
    }

    // Grupos de la lista
    private enum Grupo { PROXIMAS, REALIZADAS, CANCELADAS }

    // Elementos del FXML
    @FXML private ToggleGroup grupoFiltro;
    @FXML private ToggleButton tabProximas;
    @FXML private ToggleButton tabRealizadas;
    @FXML private ToggleButton tabCanceladas;
    @FXML private ToggleButton tabTodas;
    @FXML private VBox panel;

    // Services
    private final ActividadService actividadService = new ActividadService();
    private final InscripcionService inscripcionService = new InscripcionService();

    // Datos cargados
    private final List<Fila> filas = new ArrayList<>();

    @FXML
    private void initialize() {
        if (!Sesion.haySesion()) {
            mostrarBloqueado();
            return;
        }
        // Que siempre haya una pestaña elegida y repintar al cambiar
        grupoFiltro.selectedToggleProperty().addListener((obs, anterior, nueva) -> {
            if (nueva == null) {
                anterior.setSelected(true);
            } else {
                pintar();
            }
        });

        // Esquinas redondeadas del panel
        Rectangle recorte = new Rectangle();
        recorte.setArcWidth(36);
        recorte.setArcHeight(36);
        recorte.widthProperty().bind(panel.widthProperty());
        recorte.heightProperty().bind(panel.heightProperty());
        panel.setClip(recorte);

        cargarDatos();
    }

    // Lee mis inscripciones y sus actividades
    private void cargarDatos() {
        try {
            Map<Integer, Actividad> actividadesPorId = new HashMap<>();
            for (Actividad a : actividadService.listarTodas()) {
                actividadesPorId.put(a.getIdActividad(), a);
            }

            filas.clear();
            // Usuario de prueba hasta que exista Sesion (Persona 1)
            for (Inscripcion i : inscripcionService.listarMisInscripciones(Sesion.getIdUsuario())) {
                Actividad a = actividadesPorId.get(i.getIdActividad());
                if (a != null) {
                    filas.add(new Fila(i, a));
                }
            }

            filas.sort(Comparator
                    .comparing((Fila f) -> f.actividad().getFechaActividad())
                    .thenComparing(f -> f.actividad().getHoraInicio()));

            actualizarContadores();
            pintar();
        } catch (ActividadException | InscripcionException e) {
            mostrarError(e.getMessage());
        }
    }

    // Número en cada pestaña
    private void actualizarContadores() {
        tabProximas.setText("Próximas · " + contar(Grupo.PROXIMAS));
        tabRealizadas.setText("Realizadas · " + contar(Grupo.REALIZADAS));
        tabCanceladas.setText("Canceladas · " + contar(Grupo.CANCELADAS));
        tabTodas.setText("Todas · " + filas.size());
    }

    private long contar(Grupo grupo) {
        return filas.stream().filter(f -> grupoDe(f) == grupo).count();
    }

    // Dibuja el panel según la pestaña elegida
    private void pintar() {
        panel.getChildren().clear();

        List<Grupo> grupos = gruposDeLaPestana();
        boolean hayAlguna = false;

        for (Grupo grupo : grupos) {
            List<Fila> delGrupo = filas.stream().filter(f -> grupoDe(f) == grupo).toList();
            if (delGrupo.isEmpty()) {
                continue;
            }
            hayAlguna = true;
            panel.getChildren().add(crearBanda(grupo));
            for (Fila f : delGrupo) {
                panel.getChildren().add(crearFila(f));
            }
        }

        if (!hayAlguna) {
            panel.getChildren().add(crearVacio());
        }
    }

    private List<Grupo> gruposDeLaPestana() {
        if (tabRealizadas.isSelected()) return List.of(Grupo.REALIZADAS);
        if (tabCanceladas.isSelected()) return List.of(Grupo.CANCELADAS);
        if (tabTodas.isSelected()) return List.of(Grupo.PROXIMAS, Grupo.REALIZADAS, Grupo.CANCELADAS);
        return List.of(Grupo.PROXIMAS);
    }

    // Banda de título de cada grupo
    private HBox crearBanda(Grupo grupo) {
        String texto = switch (grupo) {
            case PROXIMAS -> "PRÓXIMAS";
            case REALIZADAS -> "REALIZADAS";
            case CANCELADAS -> "CANCELADAS";
        };
        Label titulo = new Label(texto);
        titulo.getStyleClass().add("eyebrow");

        HBox banda = new HBox(titulo);
        banda.getStyleClass().add("panel-h");
        if (grupo == Grupo.PROXIMAS) {
            banda.getStyleClass().add("acento");
        }
        return banda;
    }

    // Fila de una inscripción
    private HBox crearFila(Fila f) {
        Actividad a = f.actividad();

        Label nombre = new Label(a.getNombreActividad());
        nombre.getStyleClass().add("fila-nombre");

        Label datos = new Label(FORMATO_FECHA.format(a.getFechaActividad())
                + " · " + a.getHoraInicio() + " · " + a.getLugar());
        datos.getStyleClass().add("fila-datos");

        VBox textos = new VBox(2, nombre, datos);

        Region hueco = new Region();
        HBox.setHgrow(hueco, Priority.ALWAYS);

        HBox fila = new HBox(12, EstiloActividad.miniatura(a, 44), textos, hueco, crearEstado(f));
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("fila-lista");

        if (grupoDe(f) == Grupo.PROXIMAS) {
            Button cancelar = new Button("Cancelar");
            cancelar.getStyleClass().add("boton-cancelar-pequeno");
            cancelar.setOnAction(e -> cancelar(f));
            fila.getChildren().add(cancelar);
        }

        // Clic en la fila (menos en el botón) abre el detalle
        fila.setOnMouseClicked(e -> {
            if (!vieneDeBoton(e.getTarget())) {
                Navegador.irA(Vista.DETALLE_ACTIVIDAD,
                        (DetalleActividadController detalle) ->
                                detalle.setActividad(a, () -> Navegador.irA(Vista.MIS_INSCRIPCIONES)));
            }
        });
        return fila;
    }

    // Etiqueta de estado
    private Label crearEstado(Fila f) {
        Label pill = new Label();
        pill.getStyleClass().add("pill");

        switch (grupoDe(f)) {
            case CANCELADAS -> {
                pill.setText("Cancelada");
                pill.getStyleClass().add("bad");
            }
            case REALIZADAS -> {
                pill.setText("Realizada");
                pill.getStyleClass().add("neu");
            }
            case PROXIMAS -> {
                if (f.inscripcion().estaPendienteDePago()) {
                    pill.setText("Falta pagar");
                    pill.getStyleClass().add("warn");
                } else {
                    pill.setText("Apuntado");
                    pill.getStyleClass().add("info");
                }
            }
        }
        return pill;
    }

    // Mensaje cuando no hay nada en la pestaña
    private VBox crearVacio() {
        String titulo;
        String texto;
        if (tabRealizadas.isSelected()) {
            titulo = "Todavía no has ido a ninguna actividad";
            texto = "Cuando vayas, aquí aparecerán las actividades en las que has participado.";
        } else if (tabCanceladas.isSelected()) {
            titulo = "No has cancelado nada";
            texto = "Si cancelas una inscripción, la verás aquí.";
        } else {
            titulo = "No tienes planes próximos";
            texto = "Echa un vistazo a lo que hay esta semana y apúntate a algo.";
        }

        StackPane icono = new StackPane(Iconos.crear("listcheck", 26));
        icono.getStyleClass().add("vacio-icono");

        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("vacio-titulo");

        Label lblTexto = new Label(texto);
        lblTexto.getStyleClass().add("vacio-texto");
        lblTexto.setWrapText(true);

        Button explorar = new Button("Explorar actividades");
        explorar.getStyleClass().add("boton-apuntarme");
        explorar.setOnAction(e -> Navegador.irA(Vista.INICIO));

        VBox vacio = new VBox(14, icono, lblTitulo, lblTexto, explorar);
        vacio.setAlignment(Pos.CENTER);
        vacio.getStyleClass().add("vacio-caja");
        return vacio;
    }
    // Mensaje para invitados
    private void mostrarBloqueado() {
        tabProximas.getParent().setVisible(false);
        tabProximas.getParent().setManaged(false);

        StackPane icono = new StackPane(Iconos.crear("lock", 26));
        icono.getStyleClass().add("vacio-icono");

        Label titulo = new Label("Inicia sesión para ver tus inscripciones");
        titulo.getStyleClass().add("vacio-titulo");

        Label texto = new Label("Con una cuenta podrás apuntarte a actividades y verlas todas aquí.");
        texto.getStyleClass().add("vacio-texto");
        texto.setWrapText(true);

        Button entrar = new Button("Iniciar sesión");
        entrar.getStyleClass().add("boton-apuntarme");
        entrar.setOnAction(e -> Navegador.mostrarPantallaCompleta(Vista.LOGIN));

        VBox caja = new VBox(14, icono, titulo, texto, entrar);
        caja.setAlignment(Pos.CENTER);
        caja.getStyleClass().add("vacio-caja");
        panel.getChildren().setAll(caja);
    }

    // Acción del botón "Cancelar"
    private void cancelar(Fila f) {
        String nombre = f.actividad().getNombreActividad();
        if (!confirmar("¿Seguro que quieres cancelar tu inscripción en \"" + nombre + "\"?")) {
            return;
        }
        try {
            inscripcionService.cancelar(Sesion.getIdUsuario(), f.actividad().getIdActividad());
            mostrarInfo("Has cancelado tu inscripción en \"" + nombre + "\".");
        } catch (InscripcionException e) {
            mostrarError(e.getMessage());
        }
        cargarDatos();
    }

    // A qué grupo pertenece cada fila
    private Grupo grupoDe(Fila f) {
        if (f.inscripcion().getEstado() == EstadoInscripcion.CANCELADA) {
            return Grupo.CANCELADAS;
        }
        if (f.actividad().getFechaActividad().isBefore(LocalDate.now())) {
            return Grupo.REALIZADAS;
        }
        return Grupo.PROXIMAS;
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