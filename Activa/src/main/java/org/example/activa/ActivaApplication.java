package org.example.activa;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.activa.util.Navegador;
import org.example.activa.util.Vista;

import java.util.Objects;

public class ActivaApplication extends Application {

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new StackPane(), 1200, 760);
        scene.getStylesheets().add(Objects.requireNonNull(
                ActivaApplication.class.getResource("/org/example/activa/css/styles.css")).toExternalForm());

        // El Navegador cambia de pantalla dentro de esta escena
        Navegador.iniciar(scene);

        //Login
        Navegador.mostrarPantallaCompleta(Vista.LOGIN);

        stage.setTitle("Activa - Gestión de eventos comunitarios");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}