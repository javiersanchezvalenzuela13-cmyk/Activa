package org.example.activa;

import javafx.application.Application;

/**
 * Punto de entrada para ejecutar desde el IDE sin configurar el module-path.
 * Al no heredar de Application, la JVM no exige los módulos de JavaFX al arrancar.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(ActivaApplication.class, args);
    }
}
