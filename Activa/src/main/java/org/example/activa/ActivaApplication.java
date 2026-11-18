package org.example.activa;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
public class ActivaApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(ActivaApplication.class.getResource("/org/example/activa/fxml/inicio-view.fxml"));
        Scene scene = new Scene(loader.load(), 640, 420);
        scene.getStylesheets().add(
                Objects.requireNonNull(ActivaApplication.class.getResource("/org/example/activa/styles.css")).toExternalForm());

        stage.setTitle("Activa - Gestión de eventos comunitarios");
        stage.setMinWidth(480);
        stage.setMinHeight(360);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
