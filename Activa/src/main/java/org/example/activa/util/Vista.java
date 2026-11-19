package org.example.activa.util;

public enum Vista {

    // Ventana principal con menú lateral
    MAIN("main-view.fxml"),

    // Persona 1: LOGIN, REGISTRO, PERFIL, AJUSTES...
    LOGIN("auth/login-view.fxml"),
    REGISTRO("auth/registro-view.fxml"),

    // Persona 2
    INICIO("usuario/inicio-view.fxml"),
    DETALLE_ACTIVIDAD("usuario/detalle-actividad-view.fxml"),
    MIS_INSCRIPCIONES("usuario/mis-inscripciones-view.fxml"),

    // Persona 3: CALENDARIO, PAGOS, HISTORIAL...
    CALENDARIO("usuario/calendario-view.fxml"),
    // HISTORIAL("usuario/historial-view.fxml"),
    PAGOS("usuario/pagos-view.fxml");

    private final String ruta;

    Vista(String ruta) {
        this.ruta = ruta;
    }

    public String getRuta() {
        return ruta;
    }
}