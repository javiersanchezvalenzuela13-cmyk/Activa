package org.example.activa;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.example.activa.db.ConexionBD;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

public class InicioController {

    @FXML
    private Label lblEstado;

    @FXML
    private Button btnProbarConexion;

    @FXML
    private void initialize() {
        lblEstado.setText("Pulsa el botón para comprobar la conexión con la base de datos.");
    }

    /** Prueba la conexión JDBC en segundo plano para no bloquear la interfaz. */
    @FXML
    private void onProbarConexion() {
        btnProbarConexion.setDisable(true);
        setEstado("Conectando...", "estado-info");

        Task<String> tarea = new Task<>() {
            @Override
            protected String call() throws Exception {
                try (Connection con = ConexionBD.getConexion()) {
                    DatabaseMetaData meta = con.getMetaData();
                    return "Conexión correcta: " + meta.getDatabaseProductName()
                            + " " + meta.getDatabaseProductVersion();
                }
            }
        };
        tarea.setOnSucceeded(e -> {
            setEstado(tarea.getValue(), "estado-ok");
            btnProbarConexion.setDisable(false);
        });
        tarea.setOnFailed(e -> {
            Throwable error = tarea.getException();
            setEstado("Error de conexión: " + error.getMessage(), "estado-error");
            btnProbarConexion.setDisable(false);
        });

        Thread hilo = new Thread(tarea, "prueba-conexion");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void setEstado(String texto, String claseCss) {
        lblEstado.setText(texto);
        lblEstado.getStyleClass().removeAll("estado-info", "estado-ok", "estado-error");
        lblEstado.getStyleClass().add(claseCss);
    }
}
