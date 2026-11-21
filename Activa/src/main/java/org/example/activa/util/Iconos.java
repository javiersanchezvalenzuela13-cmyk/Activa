package org.example.activa.util;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;

import java.util.HashMap;
import java.util.Map;

public final class Iconos {

    // Dibujos de los iconos (mismos que el prototipo, en una cuadrícula de 24x24)
    private static final Map<String, String> RUTAS = new HashMap<>();

    static {
        RUTAS.put("house", "M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8 M3 10a2 2 0 0 1 .709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z");
        RUTAS.put("listcheck", "M3 17 l2 2 4-4M3 7l2 2 4-4M13 6h8M13 12h8M13 18h8");
        RUTAS.put("calendar", "M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2Z M16 2v4M8 2v4M3 10h18");
        RUTAS.put("history", "M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8 M3 3v5h5 M12 7v5l4 2");
        RUTAS.put("card", "M4 5h16a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-16a2 2 0 0 1 -2 -2v-10a2 2 0 0 1 2 -2Z M2 10h20");
        RUTAS.put("settings", "M20 7h-9M14 17H5 M14 17a3 3 0 1 0 6 0a3 3 0 1 0 -6 0Z M4 7a3 3 0 1 0 6 0a3 3 0 1 0 -6 0Z");
        RUTAS.put("shield", "M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z");
        RUTAS.put("plus", "M5 12h14M12 5v14");
        RUTAS.put("users", "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2 M5 7a4 4 0 1 0 8 0a4 4 0 1 0 -8 0Z M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75");
        RUTAS.put("search", "M3 11a8 8 0 1 0 16 0a8 8 0 1 0 -16 0Z M21 21 l-4.3-4.3");
        RUTAS.put("heart", "M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z");
        RUTAS.put("bell", "M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9 M10.3 21a1.94 1.94 0 0 0 3.4 0");
        RUTAS.put("chevDown", "M6 9 l6 6 6-6");
        RUTAS.put("chat", "M7.9 20A9 9 0 1 0 4 16.1L2 22Z");
        RUTAS.put("user", "M7 8a5 5 0 1 0 10 0a5 5 0 1 0 -10 0Z M20 21a8 8 0 0 0-16 0");
        RUTAS.put("tag", "M12.586 2.586A2 2 0 0 0 11.172 2H4a2 2 0 0 0-2 2v7.172a2 2 0 0 0 .586 1.414l8.704 8.704a2.426 2.426 0 0 0 3.42 0l6.58-6.58a2.426 2.426 0 0 0 0-3.42z M7 7.5a0.5 0.5 0 1 0 1 0a0.5 0.5 0 1 0 -1 0Z");
        RUTAS.put("logout", "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4 M16 17 l5-5-5-5M21 12H9");
        RUTAS.put("leaf", "M11 20A7 7 0 0 1 9.8 6.1C15.5 5 17 4.48 19 2c1 2 2 4.18 2 8 0 5.5-4.78 10-10 10Z M2 21c0-3 1.85-5.36 5.08-6C9.5 14.52 12 13 13 12");
        RUTAS.put("pin", "M20 10c0 4.99-5.54 10.19-7.4 11.8a1 1 0 0 1-1.2 0C9.54 20.19 4 14.99 4 10a8 8 0 0 1 16 0 M9 10a3 3 0 1 0 6 0a3 3 0 1 0 -6 0Z");
        RUTAS.put("clock", "M2 12a10 10 0 1 0 20 0a10 10 0 1 0 -20 0Z M12 6v6l4 2");
        RUTAS.put("check", "M20 6 9 17l-5-5");
        RUTAS.put("arrowLeft", "M12 19 l-7-7 7-7M19 12H5");
        RUTAS.put("arrowRight", "M5 12h14M12 5l7 7-7 7");
        RUTAS.put("sparkles", "M9.937 15.5A2 2 0 0 0 8.5 14.063l-6.135-1.582a.5.5 0 0 1 0-.962L8.5 9.936A2 2 0 0 0 9.937 8.5l1.582-6.135a.5.5 0 0 1 .963 0L14.063 8.5A2 2 0 0 0 15.5 9.937l6.135 1.581a.5.5 0 0 1 0 .964L15.5 14.063a2 2 0 0 0-1.437 1.437l-1.582 6.135a.5.5 0 0 1-.963 0z");
        RUTAS.put("sun", "M8 12a4 4 0 1 0 8 0a4 4 0 1 0 -8 0Z M12 2v2M12 20v2M4.93 4.93l1.41 1.41M17.66 17.66l1.41 1.41M2 12h2M20 12h2M6.34 17.66l-1.41 1.41M19.07 4.93l-1.41 1.41");
        RUTAS.put("yoga", "M11 5a1 1 0 1 0 2 0a1 1 0 1 0 -2 0Z M9 20 l3-6 3 6 M6 8 l6 2 6-2 M12 10v4");
        RUTAS.put("amphora", "M10 2v5.632c0 .424-.272.795-.653.982A6 6 0 0 0 6 14c.006 4 3 7 5 8 M10 5H8a2 2 0 0 0 0 4h.68 M14 2v5.632c0 .424.272.795.652.982A6 6 0 0 1 18 14c0 4-3 7-5 8 M14 5h2a2 2 0 0 1 0 4h-.68 M18 22H6 M9 2h6");
        RUTAS.put("book", "M12 7v14 M3 18a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1h5a4 4 0 0 1 4 4 4 4 0 0 1 4-4h5a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1h-6a3 3 0 0 0-3 3 3 3 0 0 0-3-3z");
        RUTAS.put("dumbbell", "M14.4 14.4 9.6 9.6 M18.657 21.485a2 2 0 1 1-2.829-2.828l-1.767 1.768a2 2 0 1 1-2.829-2.829l6.364-6.364a2 2 0 1 1 2.829 2.829l-1.768 1.767a2 2 0 1 1 2.828 2.829z M21.5 21.5 l-1.4-1.4 M3.9 3.9 2.5 2.5 M6.404 12.768a2 2 0 1 1-2.829-2.829l1.768-1.767a2 2 0 1 1-2.828-2.829l2.828-2.828a2 2 0 1 1 2.829 2.828l1.767-1.768a2 2 0 1 1 2.829 2.829z");
        RUTAS.put("camera", "M14.5 4h-5L7 7H4a2 2 0 0 0-2 2v9a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-3l-2.5-3z M9 13a3 3 0 1 0 6 0a3 3 0 1 0 -6 0Z");
        RUTAS.put("mountain", "M8 3 l4 8 5-5 5 15H2L8 3z");
        RUTAS.put("sprout", "M7 20h10 M10 20c5.5-2.5.8-6.4 3-10 M9.5 9.4c1.1.8 1.8 2.2 2.3 3.7-2 .4-3.5.4-4.8-.3-1.2-.6-2.3-1.9-3-4.2 2.8-.5 4.4 0 5.5.8z M14.1 6a7 7 0 0 0-1.1 4c1.9-.1 3.3-.6 4.3-1.4 1-1 1.6-2.3 1.7-4.6-2.7.1-4 1-4.9 2z");
        RUTAS.put("dice", "M4 10h8a2 2 0 0 1 2 2v8a2 2 0 0 1 -2 2h-8a2 2 0 0 1 -2 -2v-8a2 2 0 0 1 2 -2Z M17.92 14 l3.5-3.5a2.24 2.24 0 0 0 0-3l-5-4.92a2.24 2.24 0 0 0-3 0L10 6 M6 18h.01M10 14h.01M15 6h.01M18 9h.01");
        RUTAS.put("chef", "M17 21a1 1 0 0 0 1-1v-5.35c0-.457.316-.844.727-1.041a4 4 0 0 0-2.134-7.589 5 5 0 0 0-9.186 0 4 4 0 0 0-2.134 7.588c.411.198.727.585.727 1.041V20a1 1 0 0 0 1 1Z M6 17h12");
        RUTAS.put("music", "M9 18V5l12-2v13 M3 18a3 3 0 1 0 6 0a3 3 0 1 0 -6 0Z M15 16a3 3 0 1 0 6 0a3 3 0 1 0 -6 0Z");
        RUTAS.put("pencil", "M21.17 6.81a1 1 0 0 0-3.99-3.99L3.84 16.17a2 2 0 0 0-.5.83l-1.32 4.35a.5.5 0 0 0 .62.62l4.35-1.32a2 2 0 0 0 .83-.5z");
        RUTAS.put("trash", "M3 6h18M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2");
        RUTAS.put("wallet", "M19 7V4a1 1 0 0 0-1-1H5a2 2 0 0 0 0 4h15a1 1 0 0 1 1 1v4h-3a2 2 0 0 0 0 4h3a1 1 0 0 0 1-1v-2a1 1 0 0 0-1-1 M3 5v14a2 2 0 0 0 2 2h15a1 1 0 0 0 1-1v-4");
        RUTAS.put("userMinus", "M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2 M5 7a4 4 0 1 0 8 0a4 4 0 1 0 -8 0Z M22 11h-6");
    }

    private Iconos() {
    }

    // Crea un icono del tamaño indicado (el color se pone desde el CSS con la clase "icono")
    public static Node crear(String nombre, double tamano) {
        SVGPath forma = new SVGPath();
        forma.setContent(RUTAS.getOrDefault(nombre, ""));
        forma.getStyleClass().add("icono");

        // Cuadro invisible de 24x24 para que todos los iconos ocupen lo mismo
        Rectangle base = new Rectangle(24, 24, Color.TRANSPARENT);

        Group dibujo = new Group(base, forma);
        dibujo.getTransforms().add(new Scale(tamano / 24, tamano / 24));

        StackPane caja = new StackPane(new Group(dibujo));
        caja.setMinSize(tamano, tamano);
        caja.setPrefSize(tamano, tamano);
        caja.setMaxSize(tamano, tamano);
        return caja;
    }
}