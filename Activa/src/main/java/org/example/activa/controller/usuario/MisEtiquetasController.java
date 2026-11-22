package org.example.activa.controller.usuario;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import org.example.activa.dao.EtiquetaDAO;
import org.example.activa.util.Navegador;
import org.example.activa.util.Sesion;
import org.example.activa.util.Vista;
import org.example.activa.util.AlertUtil;

import java.util.ArrayList;
import java.util.List;

public class MisEtiquetasController {

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

    // Inicializa el controlador cargando las preferencias de etiquetas guardadas en la base de datos para el usuario activo.
    @FXML
    public void initialize() {
        cargarEtiquetas();
    }

    // Consulta la base de datos y genera visualmente las etiquetas, dejando marcadas las que el usuario ya tenía seleccionadas.
    private void cargarEtiquetas() {
        List<String> guardadas = Sesion.haySesion()
                ? etiquetaDAO.obtenerEtiquetasPorUsuario(Sesion.getIdUsuario())
                : List.of();

        poblarCategoria(flowDeporte, ETIQUETAS_DEPORTE, COLOR_DEPORTE, guardadas);
        poblarCategoria(flowArte, ETIQUETAS_ARTE, COLOR_ARTE, guardadas);
        poblarCategoria(flowCultura, ETIQUETAS_CULTURA, COLOR_CULTURA, guardadas);
        poblarCategoria(flowComunidad, ETIQUETAS_COMUNIDAD, COLOR_COMUNIDAD, guardadas);
    }

    // Recorre la lista de nombres proporcionada creando un botón por etiqueta y lo añade al contenedor visual correspondiente.
    private void poblarCategoria(FlowPane contenedor, List<String> etiquetas, String colorActivo, List<String> guardadas) {
        contenedor.getChildren().clear();

        for (String etiqueta : etiquetas) {
            boolean seleccionada = contieneIgnorandoMayusculas(guardadas, etiqueta);
            ToggleButton btn = crearBotonEtiqueta(etiqueta, colorActivo, seleccionada);
            contenedor.getChildren().add(btn);
        }
    }

    // Genera un componente interactivo que alterna su estado, prefijo textual y color cuando el usuario hace clic sobre él.
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

    // Modifica los estilos CSS del botón para mostrar un fondo sólido con un check si está activo, o un fondo claro con un '+' si no lo está.
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

    // Recopila los botones actualmente seleccionados y sobreescribe las preferencias del usuario en la base de datos.
    @FXML
    void onGuardarCambios() {
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

    // Extrae el valor textual interno de todos los botones marcados dentro de un contenedor y los añade a una lista.
    private void recogerSeleccionadas(FlowPane contenedor, List<String> destino) {
        for (Node nodo : contenedor.getChildren()) {
            if (nodo instanceof ToggleButton btn && btn.isSelected()) {
                destino.add((String) btn.getUserData());
            }
        }
    }

    // Revierte cualquier cambio no guardado en pantalla restaurando el estado visual a los datos almacenados en la base de datos.
    @FXML
    void onDescartar() {
        List<String> guardadas = Sesion.haySesion()
                ? etiquetaDAO.obtenerEtiquetasPorUsuario(Sesion.getIdUsuario())
                : List.of();

        restaurarCategoria(flowDeporte, COLOR_DEPORTE, guardadas);
        restaurarCategoria(flowArte, COLOR_ARTE, guardadas);
        restaurarCategoria(flowCultura, COLOR_CULTURA, guardadas);
        restaurarCategoria(flowComunidad, COLOR_COMUNIDAD, guardadas);
    }

    // Recorre un contenedor de botones forzando el estado activo o inactivo de cada uno basándose en una lista de valores guardados.
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

    // Cambia la vista activa de la zona central a la sección de edición del perfil personal.
    @FXML
    void onIrADatos() {
        Navegador.irA(Vista.PERFIL);
    }

    // Cambia la vista activa de la zona central a la sección de gestión de tarjetas bancarias.
    @FXML
    void onIrAMetodosPago() {
        Navegador.irA(Vista.METODOS_PAGO);
    }

    // Verifica si un término exacto se encuentra dentro de una lista de cadenas sin tener en cuenta variaciones de capitalización.
    private boolean contieneIgnorandoMayusculas(List<String> lista, String objetivo) {
        for (String item : lista) {
            if (item.trim().equalsIgnoreCase(objetivo.trim())) {
                return true;
            }
        }
        return false;
    }

    // Muestra una alerta con el estilo de la app
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        AlertUtil.crear(tipo, titulo, mensaje).showAndWait();
    }
}