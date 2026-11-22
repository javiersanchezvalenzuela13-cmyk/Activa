package org.example.activa.controller.usuario;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import org.example.activa.dao.EtiquetaDAO;
import org.example.activa.util.Navegador;
import org.example.activa.util.Sesion;
import org.example.activa.util.Vista;

import java.util.ArrayList;
import java.util.List;

public class MisEtiquetasController {

    @FXML
    private ToggleGroup grupoPestanas;

    @FXML
    private FlowPane flowDeporte;

    @FXML
    private FlowPane flowArte;

    @FXML
    private FlowPane flowCultura;

    @FXML
    private FlowPane flowComunidad;

    private final EtiquetaDAO etiquetaDAO = new EtiquetaDAO();

    private static final List<String> ETIQUETAS_DEPORTE = List.of(
            "Yoga", "Senderismo", "Running", "Pádel", "Baile", "Deporte"
    );
    private static final List<String> ETIQUETAS_ARTE = List.of(
            "Cerámica", "Pintura", "Fotografía", "Manualidades", "Música"
    );
    private static final List<String> ETIQUETAS_CULTURA = List.of(
            "Lectura", "Cine", "Teatro", "Juegos de mesa", "Idiomas"
    );
    private static final List<String> ETIQUETAS_COMUNIDAD = List.of(
            "Voluntariado", "Cocina", "Jardinería", "Mayores", "Familias", "Bienestar"
    );

    private static final String COLOR_DEPORTE = "#216382";
    private static final String COLOR_ARTE = "#B65A34";
    private static final String COLOR_CULTURA = "#9E7D1D";
    private static final String COLOR_COMUNIDAD = "#6B436B";

    // Carga las etiquetas guardadas del usuario al iniciar la pantalla
    @FXML
    public void initialize() {
        cargarEtiquetas();
    }

    // Rellena cada categoría con sus etiquetas marcando las que el usuario tiene en BD
    private void cargarEtiquetas() {
        List<String> guardadas = Sesion.haySesion()
                ? etiquetaDAO.obtenerEtiquetasPorUsuario(Sesion.getIdUsuario())
                : List.of();

        poblarCategoria(flowDeporte, ETIQUETAS_DEPORTE, COLOR_DEPORTE, guardadas);
        poblarCategoria(flowArte, ETIQUETAS_ARTE, COLOR_ARTE, guardadas);
        poblarCategoria(flowCultura, ETIQUETAS_CULTURA, COLOR_CULTURA, guardadas);
        poblarCategoria(flowComunidad, ETIQUETAS_COMUNIDAD, COLOR_COMUNIDAD, guardadas);
    }

    // Genera los botones de una categoría dentro de su FlowPane correspondiente
    private void poblarCategoria(FlowPane contenedor, List<String> etiquetas, String colorActivo, List<String> guardadas) {
        contenedor.getChildren().clear();

        for (String etiqueta : etiquetas) {
            boolean seleccionada = contieneIgnorandoMayusculas(guardadas, etiqueta);
            ToggleButton btn = crearBotonEtiqueta(etiqueta, colorActivo, seleccionada);
            contenedor.getChildren().add(btn);
        }
    }

    // Crea un botón tipo píldora que cambia de prefijo (+ / ✓) y color al pulsarlo
    private ToggleButton crearBotonEtiqueta(String nombreEtiqueta, String colorActivo, boolean seleccionada) {
        ToggleButton btn = new ToggleButton();
        btn.setUserData(nombreEtiqueta);
        btn.setSelected(seleccionada);
        actualizarAparienciaBoton(btn, nombreEtiqueta, colorActivo, seleccionada);

        btn.selectedProperty().addListener((obs, antes, ahora) ->
                actualizarAparienciaBoton(btn, nombreEtiqueta, colorActivo, ahora)
        );

        return btn;
    }

    // Aplica el texto y estilo visual según si la etiqueta está seleccionada o no
    private void actualizarAparienciaBoton(ToggleButton btn, String nombreEtiqueta, String colorActivo, boolean activa) {
        if (activa) {
            btn.setText("✓  " + nombreEtiqueta);
            btn.setStyle("-fx-background-color: " + colorActivo + "; -fx-text-fill: white; " +
                    "-fx-background-radius: 999; -fx-border-radius: 999; " +
                    "-fx-border-color: " + colorActivo + "; -fx-padding: 8 18; " +
                    "-fx-font-weight: bold; -fx-cursor: hand;");
        } else {
            btn.setText("+  " + nombreEtiqueta);
            btn.setStyle("-fx-background-color: #FAF8F3; -fx-text-fill: #2C3531; " +
                    "-fx-background-radius: 999; -fx-border-radius: 999; " +
                    "-fx-border-color: #DCD6C8; -fx-padding: 8 18; " +
                    "-fx-cursor: hand;");
        }
    }

    // Recoge todas las etiquetas marcadas, borra las anteriores en BD y guarda las actuales
    @FXML
    void onGuardarCambios(ActionEvent event) {
        List<String> seleccionadas = new ArrayList<>();

        recogerSeleccionadas(flowDeporte, seleccionadas);
        recogerSeleccionadas(flowArte, seleccionadas);
        recogerSeleccionadas(flowCultura, seleccionadas);
        recogerSeleccionadas(flowComunidad, seleccionadas);

        boolean exito = etiquetaDAO.guardarEtiquetasUsuario(Sesion.getIdUsuario(), seleccionadas);

        if (exito) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Etiquetas guardadas", "Tus preferencias se han actualizado correctamente.");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al guardar", "No se pudieron actualizar tus etiquetas en la base de datos.");
        }
    }

    // Añade a la lista los nombres limpios de los botones seleccionados de un contenedor
    private void recogerSeleccionadas(FlowPane contenedor, List<String> destino) {
        for (Node nodo : contenedor.getChildren()) {
            if (nodo instanceof ToggleButton btn && btn.isSelected()) {
                destino.add((String) btn.getUserData());
            }
        }
    }

    // Descarta las selecciones sin guardar y deja marcadas únicamente las etiquetas de la BD
    @FXML
    void onDescartar(ActionEvent event) {
        List<String> guardadas = Sesion.haySesion()
                ? etiquetaDAO.obtenerEtiquetasPorUsuario(Sesion.getIdUsuario())
                : List.of();

        restaurarCategoria(flowDeporte, COLOR_DEPORTE, guardadas);
        restaurarCategoria(flowArte, COLOR_ARTE, guardadas);
        restaurarCategoria(flowCultura, COLOR_CULTURA, guardadas);
        restaurarCategoria(flowComunidad, COLOR_COMUNIDAD, guardadas);
    }

    // Sincroniza la selección y el estilo visual de cada botón con las etiquetas guardadas en BD
    private void restaurarCategoria(FlowPane contenedor, String colorActivo, List<String> guardadas) {
        for (Node nodo : contenedor.getChildren()) {
            if (nodo instanceof ToggleButton btn) {
                String nombre = (String) btn.getUserData();
                boolean enBD = contieneIgnorandoMayusculas(guardadas, nombre);
                btn.setSelected(enBD);
                actualizarAparienciaBoton(btn, nombre, colorActivo, enBD);
            }
        }
    }

    // Navega de vuelta a la pestaña de datos personales
    @FXML
    void onIrADatos(ActionEvent event) {
        Navegador.irA(Vista.PERFIL);
    }

    // Navega a la pestaña de métodos de pago
    @FXML
    void onIrAMetodosPago(ActionEvent event) {
        Navegador.irA(Vista.METODOS_PAGO);
    }

    // Comprueba si una lista contiene un texto sin distinguir mayúsculas o minúsculas
    private boolean contieneIgnorandoMayusculas(List<String> lista, String objetivo) {
        for (String item : lista) {
            if (item.trim().equalsIgnoreCase(objetivo.trim())) {
                return true;
            }
        }
        return false;
    }

    // Muestra una ventana emergente con el mensaje indicado
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}