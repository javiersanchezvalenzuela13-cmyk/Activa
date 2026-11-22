package org.example.activa.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.function.Consumer;

public final class Navegador {

    private static final String CARPETA_FXML = "/org/example/activa/fxml/";

    private static Scene escena;
    private static BorderPane contenedor;
    private static Consumer<Vista> alCambiarVista;

    // No se crean objetos de esta clase: todo es static
    private Navegador() {
    }

    // Se llama una vez al arrancar la app
    public static void iniciar(Scene escena) {
        Navegador.escena = escena;
    }

    // Lo llama MainController: dónde se cambian las pantallas y a quién avisar
    public static void setContenedor(BorderPane contenedor, Consumer<Vista> alCambiarVista) {
        Navegador.contenedor = contenedor;
        Navegador.alCambiarVista = alCambiarVista;
    }

    // Cambia la ventana entera (login, registro, o la ventana principal)
    public static void mostrarPantallaCompleta(Vista vista) {
        Parent raiz = cargar(vista).raiz();
        escena.setRoot(raiz);
        GestorApariencia.aplicar(raiz);
    }

    // Cambia solo la zona central (dejando el menú lateral)
    public static void irA(Vista vista) {
        irA(vista, null);
    }

    // Igual, pero pudiendo pasar datos al controlador de la pantalla nueva
    public static <T> T irA(Vista vista, Consumer<T> preparar) {
        Cargada<T> cargada = cargar(vista);

        if (preparar != null) {
            preparar.accept(cargada.controlador());
        }

        contenedor.setCenter(cargada.raiz());
        GestorApariencia.aplicar(cargada.raiz());

        if (alCambiarVista != null) {
            alCambiarVista.accept(vista);
        }
        return cargada.controlador();
    }

    // Métodos privados

    // Resultado de cargar un FXML: la vista y su controlador
    private record Cargada<T>(Parent raiz, T controlador) {
    }

    private static <T> Cargada<T> cargar(Vista vista) {
        String ruta = CARPETA_FXML + vista.getRuta();
        try {
            FXMLLoader loader = new FXMLLoader(Navegador.class.getResource(ruta));
            Parent raiz = loader.load();
            T controlador = loader.getController();
            return new Cargada<>(raiz, controlador);
        } catch (IOException | IllegalStateException e) {
            throw new IllegalStateException("No se pudo cargar la pantalla " + ruta, e);
        }
    }
}