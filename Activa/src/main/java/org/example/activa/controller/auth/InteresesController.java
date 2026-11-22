package org.example.activa.controller.auth;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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

    // Desactiva el botón principal al iniciar hasta que se cumpla el mínimo de etiquetas.
    @FXML
    public void initialize() {
        btnEmpezar.setDisable(true);
    }

    // Gestiona la selección y deselección de etiquetas, actualizando la lista y el diseño.
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

    // Actualiza el texto del contador y desbloquea el botón si hay al menos 3 etiquetas.
    private void actualizarUI() {
        contadorLabel.setText("✦ " + etiquetasSeleccionadas.size() + " elegidas");
        btnEmpezar.setDisable(etiquetasSeleccionadas.size() < 3);
    }

    // Salta este paso y lleva al usuario a la pantalla principal sin guardar cambios.
    @FXML
    public void handleOmitir() {
        Navegador.mostrarPantallaCompleta(Vista.MAIN);
    }

    // Guarda las etiquetas en la cuenta del usuario activo y redirige al menú principal.
    @FXML
    public void handleFinalizar() {
        if (Sesion.haySesion()) {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            usuarioDAO.guardarIntereses(Sesion.getIdUsuario(), etiquetasSeleccionadas);
        }
        Navegador.mostrarPantallaCompleta(Vista.MAIN);
    }
}