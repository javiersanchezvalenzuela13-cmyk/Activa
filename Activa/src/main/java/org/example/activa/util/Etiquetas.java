package org.example.activa.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Etiquetas {

    // Etiquetas permitidas, agrupadas (las mismas que el prototipo)
    public static final Map<String, List<String>> POR_GRUPO = new LinkedHashMap<>();

    static {
        POR_GRUPO.put("Deporte", List.of("Yoga", "Senderismo", "Running", "Pádel", "Baile", "Deporte"));
        POR_GRUPO.put("Arte", List.of("Cerámica", "Pintura", "Fotografía", "Manualidades", "Música"));
        POR_GRUPO.put("Cultura", List.of("Lectura", "Cine", "Teatro", "Juegos de mesa", "Idiomas"));
        POR_GRUPO.put("Comunidad", List.of("Voluntariado", "Cocina", "Jardinería", "Mayores", "Familias", "Bienestar"));
    }

    // Máximo de etiquetas por actividad
    public static final int MAXIMO_POR_ACTIVIDAD = 5;

    private Etiquetas() {
    }

    // Todas en una sola lista
    public static List<String> todas() {
        List<String> lista = new ArrayList<>();
        POR_GRUPO.values().forEach(lista::addAll);
        return lista;
    }
}