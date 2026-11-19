package org.example.activa.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class Busqueda {

    // Texto del buscador de la barra superior, compartido por toda la app
    private static final StringProperty texto = new SimpleStringProperty("");

    private Busqueda() {
    }

    public static StringProperty textoProperty() {
        return texto;
    }

    public static String getTexto() {
        return texto.get() == null ? "" : texto.get();
    }
}