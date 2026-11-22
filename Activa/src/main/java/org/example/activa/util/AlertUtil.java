package org.example.activa.util;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.Objects;
import java.util.Optional;

public final class AlertUtil {

    private static final String CSS_COLORES = "/org/example/activa/css/styles.css";
    private static final String CSS_DIALOGOS = "/org/example/activa/css/dialogos.css";

    private AlertUtil() {
    }

    // Mensajes simples

    public static void info(String mensaje) {
        crear(Alert.AlertType.INFORMATION, "Listo", mensaje).showAndWait();
    }

    public static void error(String mensaje) {
        crear(Alert.AlertType.ERROR, "Algo ha fallado", mensaje).showAndWait();
    }

    public static void aviso(String mensaje) {
        crear(Alert.AlertType.WARNING, "Atención", mensaje).showAndWait();
    }

    // Preguntas de sí o no

    public static boolean confirmar(String mensaje) {
        return confirmar("¿Estás seguro?", mensaje, "Sí", "No");
    }

    public static boolean confirmar(String titulo, String mensaje, String textoSi, String textoNo) {
        return preguntar(titulo, mensaje, textoSi, textoNo, false);
    }

    // Igual, pero con el botón en rojo (eliminar, expulsar, cancelar...)
    public static boolean confirmarPeligro(String titulo, String mensaje, String textoSi, String textoNo) {
        return preguntar(titulo, mensaje, textoSi, textoNo, true);
    }

    // Crea un Alert con el estilo y el tema de la app
    public static Alert crear(Alert.AlertType tipo, String titulo, String mensaje, ButtonType... botones) {
        Alert alerta = new Alert(tipo);
        alerta.initStyle(StageStyle.TRANSPARENT);
        alerta.setTitle("Activa");
        alerta.setContentText(mensaje);
        if (botones.length > 0) {
            alerta.getButtonTypes().setAll(botones);
        }
        // Cada botón con el ancho que necesite su texto
        for (ButtonType tipoBoton : alerta.getButtonTypes()) {
            if (alerta.getDialogPane().lookupButton(tipoBoton) instanceof Button boton) {
                ButtonBar.setButtonUniformSize(boton, false);
                boton.setMinWidth(Region.USE_PREF_SIZE);
            }
        }

        DialogPane panel = alerta.getDialogPane();
        panel.setMinHeight(Region.USE_PREF_SIZE);
        panel.setHeader(crearCabecera(tipo, titulo));
        panel.getStylesheets().addAll(
                Objects.requireNonNull(AlertUtil.class.getResource(CSS_COLORES)).toExternalForm(),
                Objects.requireNonNull(AlertUtil.class.getResource(CSS_DIALOGOS)).toExternalForm());
        panel.getStyleClass().addAll("dialogo", "dialogo-" + tipo.name().toLowerCase());

        // Fondo de la ventana transparente (para ver las esquinas redondeadas)
        if (panel.getScene() != null) {
            panel.getScene().setFill(Color.TRANSPARENT);
            panel.getScene().getRoot().setStyle("-fx-background-color: transparent;");
        }

        // Centrada sobre la app y con su mismo tema (claro/oscuro, color y letra)
        Window ventana = Window.getWindows().stream()
                .filter(Window::isFocused)
                .findFirst()
                .orElse(null);
        if (ventana != null) {
            alerta.initOwner(ventana);
            copiarApariencia(ventana, panel);
        }
        return alerta;
    }

    // Métodos privados

    private static boolean preguntar(String titulo, String mensaje, String textoSi, String textoNo, boolean peligro) {
        ButtonType si = new ButtonType(textoSi, ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType(textoNo, ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = crear(Alert.AlertType.CONFIRMATION, titulo, mensaje, si, no);
        if (peligro) {
            alerta.getDialogPane().getStyleClass().add("dialogo-peligro");
            alerta.getDialogPane().setHeader(crearCabecera(Alert.AlertType.ERROR, titulo));
        }
        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == si;
    }

    // Icono en un círculo + título
    private static HBox crearCabecera(Alert.AlertType tipo, String titulo) {
        String icono = switch (tipo) {
            case ERROR, WARNING -> "alert";
            case CONFIRMATION -> "info";
            default -> "check";
        };
        StackPane circulo = new StackPane(Iconos.crear(icono, 20));
        circulo.getStyleClass().add("dialogo-icono");

        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("dialogo-titulo");
        lblTitulo.setWrapText(true);

        HBox cabecera = new HBox(14, circulo, lblTitulo);
        cabecera.setAlignment(Pos.CENTER_LEFT);
        cabecera.getStyleClass().add("dialogo-cabecera");
        return cabecera;
    }

    // Copia el tema de la ventana principal (lo pone GestorApariencia)
    private static void copiarApariencia(Window ventana, DialogPane panel) {
        if (ventana.getScene() == null || ventana.getScene().getRoot() == null) {
            return;
        }
        Parent raiz = ventana.getScene().getRoot();
        if (raiz.getStyleClass().contains("tema-oscuro")) {
            panel.getStyleClass().add("tema-oscuro");
        }
        if (raiz.getStyle() != null) {
            panel.setStyle(raiz.getStyle());
        }
    }
}