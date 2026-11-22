package org.example.activa.util;

import javafx.scene.Parent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.prefs.Preferences;

public class GestorApariencia {

    private static final Preferences PREFS = Preferences.userNodeForPackage(GestorApariencia.class);

    private static final String KEY_TEMA = "tema";
    private static final String KEY_TAMANO = "tamano";
    private static final String KEY_COLOR = "color";
    private static final String KEY_DENSIDAD = "densidad";

    public static String getTema() { return PREFS.get(KEY_TEMA, "claro"); }
    public static void setTema(String tema) { PREFS.put(KEY_TEMA, tema); }

    public static String getTamano() { return PREFS.get(KEY_TAMANO, "normal"); }
    public static void setTamano(String tamano) { PREFS.put(KEY_TAMANO, tamano); }

    public static String getColor() { return PREFS.get(KEY_COLOR, "#1B4632"); }
    public static void setColor(String hexColor) { PREFS.put(KEY_COLOR, hexColor); }

    public static String getDensidad() { return PREFS.get(KEY_DENSIDAD, "comoda"); }
    public static void setDensidad(String densidad) { PREFS.put(KEY_DENSIDAD, densidad); }

    public static void aplicar(Parent nodo) {
        if (nodo == null) return;

        Parent root = (nodo.getScene() != null && nodo.getScene().getRoot() != null)
                ? nodo.getScene().getRoot()
                : nodo;

        root.getStyleClass().removeAll("tema-oscuro", "densidad-compacta");
        if (nodo != root) {
            nodo.getStyleClass().removeAll("tema-oscuro", "densidad-compacta");
        }

        String tema = getTema();
        boolean oscuro = "oscuro".equals(tema) || ("sistema".equals(tema) && esSistemaOscuro());
        if (oscuro) {
            root.getStyleClass().add("tema-oscuro");
        }

        if ("compacta".equals(getDensidad())) {
            root.getStyleClass().add("densidad-compacta");
        }

        String colorEfectivo = oscuro ? obtenerColorModoOscuro(getColor()) : getColor();
        int fontSizePx = switch (getTamano()) {
            case "pequeno" -> 12;
            case "grande" -> 16;
            case "extragrande" -> 18;
            default -> 14;
        };

        root.setStyle(
                "-forest: " + colorEfectivo + ";" +
                        "-forest-hover: derive(" + colorEfectivo + ", -15%);" +
                        "-color-primario: " + colorEfectivo + ";" +
                        "-fx-font-size: " + fontSizePx + "px;"
        );
    }

    private static String obtenerColorModoOscuro(String hexClaro) {
        return switch (hexClaro.toUpperCase()) {
            case "#1B4632" -> "#3b9c76";
            case "#256B9A" -> "#4ea3db";
            case "#C45A32" -> "#e07a54";
            case "#7A3E7A" -> "#b87bb2";
            default -> hexClaro;
        };
    }

    private static boolean esSistemaOscuro() {
        try {
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                Process process = new ProcessBuilder(
                        "reg", "query",
                        "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                        "/v", "AppsUseLightTheme"
                ).start();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("AppsUseLightTheme")) {
                            return line.trim().endsWith("0x0");
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}