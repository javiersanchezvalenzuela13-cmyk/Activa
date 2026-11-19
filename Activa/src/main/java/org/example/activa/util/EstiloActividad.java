package org.example.activa.util;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.example.activa.model.Actividad;

public final class EstiloActividad {

    // Colores del prototipo (cuando exista "categoria" se usará esa columna)
    private static final String[] COLORES = {"lago", "terra", "mostaza", "ciruela"};

    private EstiloActividad() {
    }

    // Color de la actividad según su id
    public static String color(Actividad a) {
        return COLORES[Math.floorMod(a.getIdActividad(), COLORES.length)];
    }

    // Icono según palabras del nombre
    public static String icono(Actividad a) {
        String n = a.getNombreActividad().toLowerCase();
        if (contiene(n, "yoga", "pilates", "meditaci")) return "yoga";
        if (contiene(n, "cerámica", "ceramica", "pintura", "manualidad")) return "amphora";
        if (contiene(n, "lectura", "libro", "escritura")) return "book";
        if (contiene(n, "ruta", "senderismo", "excursi", "montaña")) return "mountain";
        if (contiene(n, "foto")) return "camera";
        if (contiene(n, "cocina", "receta", "gastronom")) return "chef";
        if (contiene(n, "música", "musica", "concierto", "baile", "coro")) return "music";
        if (contiene(n, "juego", "ajedrez", "rol")) return "dice";
        if (contiene(n, "deporte", "fútbol", "futbol", "running", "pádel", "padel", "gimnasia")) return "dumbbell";
        return "sprout";
    }

    // Cuadrado de color con el icono (para listas)
    public static Node miniatura(Actividad a, double tamano) {
        StackPane caja = new StackPane(Iconos.crear(icono(a), tamano * 0.5));
        caja.getStyleClass().addAll("miniatura", "cat-" + color(a));
        caja.setMinSize(tamano, tamano);
        caja.setPrefSize(tamano, tamano);
        caja.setMaxSize(tamano, tamano);
        return caja;
    }

    private static boolean contiene(String texto, String... palabras) {
        for (String p : palabras) {
            if (texto.contains(p)) {
                return true;
            }
        }
        return false;
    }
}