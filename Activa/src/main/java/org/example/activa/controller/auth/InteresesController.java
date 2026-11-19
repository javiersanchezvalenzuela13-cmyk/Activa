package org.example.activa.controller.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.example.activa.dao.UsuarioDAO;
import org.example.activa.util.SesionUsuarioUtil;

public class InteresesController {

    @FXML
    private Label contadorLabel;

    @FXML
    private Button btnEmpezar;

    private final List<String> etiquetasSeleccionadas = new ArrayList<>();

    // Se ejecuta al cargar la pantalla
    @FXML
    public void initialize() {
        btnEmpezar.setDisable(true);
    }

    // Gestiona la selección y deselección de las etiquetas
    @FXML
    public void handleSeleccionEtiqueta(ActionEvent event) {
        ToggleButton botonPresionado = (ToggleButton) event.getSource();
        String textoEtiqueta = botonPresionado.getText().replace("+ ", "").trim();

        if (botonPresionado.isSelected()) {
            etiquetasSeleccionadas.add(textoEtiqueta);
            botonPresionado.getStyleClass().add("chip-selected");
        } else {
            etiquetasSeleccionadas.remove(textoEtiqueta);
            botonPresionado.getStyleClass().remove("chip-selected");
        }

        actualizarUI();
    }

    // Actualiza el contador visual y el estado del botón principal
    private void actualizarUI() {
        contadorLabel.setText("✦ " + etiquetasSeleccionadas.size() + " elegidas");
        btnEmpezar.setDisable(etiquetasSeleccionadas.size() < 3);
    }

    // Salta a la pantalla principal sin guardar etiquetas
    @FXML
    public void handleOmitir(ActionEvent event) {
        cambiarPantalla(event, "/org/example/activa/fxml/main-view.fxml", "Activa - Inicio");
    }

    // Guarda las etiquetas en la BD y avanza a la pantalla principal
    @FXML
    public void handleFinalizar(ActionEvent event) {
        String emailUsuarioActual = SesionUsuarioUtil.emailActual;

        if (emailUsuarioActual != null) {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            usuarioDAO.guardarIntereses(emailUsuarioActual, etiquetasSeleccionadas);
        }

        cambiarPantalla(event, "/org/example/activa/fxml/main-view.fxml", "Activa - Inicio");
    }

    // Método auxiliar para cambiar de escena
    private void cambiarPantalla(ActionEvent event, String rutaFxml, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(rutaFxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(titulo);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de carga", "No se pudo abrir la pantalla principal.");
        }
    }

    // Método auxiliar para mostrar ventanas emergentes
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}