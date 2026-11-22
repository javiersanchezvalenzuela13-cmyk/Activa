package org.example.activa.util;

import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.example.activa.model.Actividad;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

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

    // Cuadrado pequeño para listas (foto o color con icono)
    public static Node miniatura(Actividad a, double tamano) {
        return portada(a, tamano, tamano, 12, "miniatura");
    }

    // Imagen grande para tarjetas y detalle (foto o color con icono)
    public static StackPane portada(Actividad a, double ancho, double alto) {
        return portada(a, ancho, alto, 14, "portada");
    }

    // Etiquetas como "pastillas" (si hay más de "maximo", añade "+N")
    public static List<Label> pastillas(List<String> etiquetas, int maximo) {
        List<Label> lista = new ArrayList<>();
        for (int i = 0; i < etiquetas.size() && i < maximo; i++) {
            Label pill = new Label(etiquetas.get(i));
            pill.getStyleClass().add("pill-etiqueta");
            lista.add(pill);
        }
        if (etiquetas.size() > maximo) {
            Label mas = new Label("+" + (etiquetas.size() - maximo));
            mas.getStyleClass().add("pill-etiqueta");
            lista.add(mas);
        }
        return lista;
    }

    // Métodos privados

    private static StackPane portada(Actividad a, double ancho, double alto, double radio, String clase) {
        StackPane caja = new StackPane();
        caja.setMinSize(ancho, alto);
        caja.setPrefSize(ancho, alto);
        caja.setMaxSize(ancho, alto);
        caja.getStyleClass().add(clase);

        ImageView foto = a.tieneImagen() ? fotoRecortada(a.getImagen(), ancho, alto) : null;
        if (foto != null) {
            caja.getChildren().add(foto);
        } else {
            caja.getStyleClass().add("cat-" + color(a));
            caja.getChildren().add(Iconos.crear(icono(a), Math.min(ancho, alto) * 0.45));
        }

        // Esquinas redondeadas
        Rectangle recorte = new Rectangle(ancho, alto);
        recorte.setArcWidth(radio * 2);
        recorte.setArcHeight(radio * 2);
        caja.setClip(recorte);
        return caja;
    }

    // Recorta la foto para que llene el hueco sin deformarse (null si no se puede leer)
    private static ImageView fotoRecortada(byte[] bytes, double ancho, double alto) {
        Image imagen = new Image(new ByteArrayInputStream(bytes));
        if (imagen.isError() || imagen.getWidth() == 0) {
            return null;
        }
        double escala = Math.max(ancho / imagen.getWidth(), alto / imagen.getHeight());
        double anchoVisible = ancho / escala;
        double altoVisible = alto / escala;

        ImageView vista = new ImageView(imagen);
        vista.setViewport(new Rectangle2D(
                (imagen.getWidth() - anchoVisible) / 2,
                (imagen.getHeight() - altoVisible) / 2,
                anchoVisible, altoVisible));
        vista.setFitWidth(ancho);
        vista.setFitHeight(alto);
        vista.setSmooth(true);
        return vista;
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