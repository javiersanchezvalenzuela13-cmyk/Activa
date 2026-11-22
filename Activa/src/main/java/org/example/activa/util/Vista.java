package org.example.activa.util;

public enum Vista {

    // Ventana principal con menú lateral
    MAIN("main-view.fxml"),

    // Persona 1: LOGIN, REGISTRO, PERFIL, AJUSTES DE USUARIO...
    LOGIN("auth/login-view.fxml"),
    REGISTRO("auth/registro-view.fxml"),
    INTERESES("auth/intereses-view.fxml"),
    RECUPERAR_CLAVE("auth/recuperar-clave-view.fxml"),
    NUEVA_CLAVE("auth/nueva-clave-view.fxml"),
    VERIFICACION("auth/verificacion-view.fxml"),
    PERFIL("usuario/perfil-view.fxml"),
    ETIQUETAS("usuario/mis-etiquetas-view.fxml"),
    METODOS_PAGO("usuario/metodos-pago-view.fxml"),

    // Persona 2
    INICIO("usuario/inicio-view.fxml"),
    DETALLE_ACTIVIDAD("usuario/detalle-actividad-view.fxml"),
    MIS_INSCRIPCIONES("usuario/mis-inscripciones-view.fxml"),
    PANEL_ADMIN("admin/panel-admin-view.fxml"),
    EDITOR_ACTIVIDAD("admin/editor-actividad-view.fxml"),
    GESTIONAR_INSCRITOS("admin/gestionar-inscritos-view.fxml"),

    // Persona 3: CALENDARIO, PAGOS, HISTORIAL...
    CALENDARIO("usuario/calendario-view.fxml"),
    // HISTORIAL("usuario/historial-view.fxml"),
    AJUSTES("usuario/ajustes-view.fxml"),
    PAGOS("usuario/pagos-view.fxml");

    private final String ruta;

    Vista(String ruta) {
        this.ruta = ruta;
    }

    public String getRuta() {
        return ruta;
    }
}