package org.example.activa.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.example.activa.model.Notificacion;

import java.util.List;

// Ventana emergente (modal) que muestra las notificaciones del usuario
public class ModalNotificaciones {

    // Abre el modal centrado sobre la ventana principal y carga la lista de avisos
    public static void mostrar(Window ventanaPadre, List<Notificacion> notificaciones) {
        Stage modal = new Stage(StageStyle.TRANSPARENT);
        modal.initOwner(ventanaPadre);
        modal.initModality(Modality.APPLICATION_MODAL);

        // Fondo oscurecido que cubre toda la ventana principal
        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("notif-overlay");
        overlay.setPadding(new Insets(32));

        // Tarjeta principal donde se muestra el contenido del modal
        VBox tarjeta = new VBox(18);
        tarjeta.getStyleClass().add("notif-modal");
        tarjeta.setMaxWidth(580);
        tarjeta.setMaxHeight(520);

        // Cabecera con el icono de campana, el título y el botón de cerrar
        StackPane iconoCabecera = new StackPane(Iconos.crear("bell", 20));
        iconoCabecera.getStyleClass().add("notif-header-icon");

        Label lblTitulo = new Label("Notificaciones");
        lblTitulo.getStyleClass().add("notif-header-title");
        HBox.setHgrow(lblTitulo, Priority.ALWAYS);
        lblTitulo.setMaxWidth(Double.MAX_VALUE);

        Button btnCerrar = new Button();
        btnCerrar.setGraphic(Iconos.crear("x", 16));
        btnCerrar.getStyleClass().add("notif-close-btn");
        btnCerrar.setOnAction(e -> modal.close());

        HBox cabecera = new HBox(14, iconoCabecera, lblTitulo, btnCerrar);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        // Lista desplazable con las tarjetas de aviso o un texto si está vacía
        VBox listaBox = new VBox(12);
        if (notificaciones.isEmpty()) {
            Label vacio = new Label("No tienes notificaciones nuevas.");
            vacio.getStyleClass().add("notif-empty");
            listaBox.getChildren().add(vacio);
        } else {
            for (Notificacion n : notificaciones) {
                listaBox.getChildren().add(crearTarjetaNotificacion(n, modal));
            }
        }

        ScrollPane scroll = new ScrollPane(listaBox);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("notif-scroll");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        tarjeta.getChildren().addAll(cabecera, scroll);
        overlay.getChildren().add(tarjeta);

        // Cierra el modal si el usuario hace clic fuera de la tarjeta
        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                modal.close();
            }
        });

        // Crea la escena transparente heredando los estilos y posición de la ventana padre
        Scene scene = new Scene(overlay, ventanaPadre.getWidth(), ventanaPadre.getHeight());
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().addAll(ventanaPadre.getScene().getStylesheets());

        modal.setScene(scene);
        modal.setX(ventanaPadre.getX());
        modal.setY(ventanaPadre.getY());

        // Aplica el tema (claro/oscuro), tamaño de fuente y color activo
        GestorApariencia.aplicar(overlay);
        modal.showAndWait();
    }

    // Construye la fila visual de cada notificación y redirige a su pantalla al pulsarla
    private static HBox crearTarjetaNotificacion(Notificacion n, Stage modal) {
        StackPane cajaIcono = new StackPane(Iconos.crear(n.getIcono(), 18));
        cajaIcono.getStyleClass().add("notif-item-icon");

        Label lblTitulo = new Label(n.getTitulo());
        lblTitulo.getStyleClass().add("notif-item-title");

        Label lblDetalle = new Label(n.getDetalle());
        lblDetalle.getStyleClass().add("notif-item-desc");
        lblDetalle.setWrapText(true);

        VBox textos = new VBox(3, lblTitulo, lblDetalle);
        HBox.setHgrow(textos, Priority.ALWAYS);

        Label lblTiempo = new Label(n.getTiempo());
        lblTiempo.getStyleClass().add("notif-item-time");

        HBox fila = new HBox(14, cajaIcono, textos, lblTiempo);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("notif-item");

        // Si tiene una vista asociada, cierra el modal y navega hacia ella
        if (n.getVistaDestino() != null) {
            fila.setOnMouseClicked(e -> {
                modal.close();
                Navegador.irA(n.getVistaDestino());
            });
        }

        return fila;
    }
}