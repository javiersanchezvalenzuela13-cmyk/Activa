package org.example.activa.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

public final class Busqueda {

    // Texto del buscador de la barra superior, compartido por toda la app
    private static final StringProperty texto = new SimpleStringProperty("");
    // Etiquetas marcadas en el filtro de la barra superior
    private static final ObservableSet<String> etiquetas = FXCollections.observableSet();

    public static ObservableSet<String> etiquetas() {
        return etiquetas;
    }

    private Busqueda() {
    }

    public static StringProperty textoProperty() {
        return texto;
    }

    public static String getTexto() {
        return texto.get() == null ? "" : texto.get();
    }
}