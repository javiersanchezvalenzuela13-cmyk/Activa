package org.example.activa.util;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

import java.io.ByteArrayInputStream;

public final class Imagenes {

    private Imagenes() {
    }

    // Foto recortada en círculo (null si no hay foto o está dañada)
    public static ImageView circular(byte[] bytes, double tamano) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        Image imagen = new Image(new ByteArrayInputStream(bytes));
        if (imagen.isError() || imagen.getWidth() == 0) {
            return null;
        }

        // Coger el cuadrado central de la foto
        double lado = Math.min(imagen.getWidth(), imagen.getHeight());
        ImageView vista = new ImageView(imagen);
        vista.setViewport(new Rectangle2D(
                (imagen.getWidth() - lado) / 2, (imagen.getHeight() - lado) / 2, lado, lado));
        vista.setFitWidth(tamano);
        vista.setFitHeight(tamano);
        vista.setSmooth(true);

        // Recortar en círculo
        vista.setClip(new Circle(tamano / 2, tamano / 2, tamano / 2));
        return vista;
    }
}