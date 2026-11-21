package org.example.activa.controller.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import java.util.ArrayList;
import java.util.List;
import org.example.activa.dao.UsuarioDAO;
import org.example.activa.util.Sesion;
import org.example.activa.util.Navegador;
import org.example.activa.util.Vista;

public class InteresesController {

    @FXML
    private Label contadorLabel;

    @FXML
    private Button btnEmpezar;

    private final List<String> etiquetasSeleccionadas = new ArrayList<>();

    @FXML
    public void initialize() {
        btnEmpezar.setDisable(true);
    }

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

    private void actualizarUI() {
        contadorLabel.setText("✦ " + etiquetasSeleccionadas.size() + " elegidas");
        btnEmpezar.setDisable(etiquetasSeleccionadas.size() < 3);
    }

    @FXML
    public void handleOmitir(ActionEvent event) {
        Navegador.mostrarPantallaCompleta(Vista.MAIN);
    }

    @FXML
    public void handleFinalizar(ActionEvent event) {
        if (Sesion.haySesion()) {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            usuarioDAO.guardarIntereses(Sesion.getIdUsuario(), etiquetasSeleccionadas);
        }
        Navegador.mostrarPantallaCompleta(Vista.MAIN);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}