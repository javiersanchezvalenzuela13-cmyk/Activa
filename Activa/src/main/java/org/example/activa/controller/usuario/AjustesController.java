package org.example.activa.controller.usuario;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.example.activa.util.GestorApariencia;

// Controlador de la pantalla de Ajustes: gestiona las preferencias visuales y actualiza la vista previa
public class AjustesController {

    // Elementos de la cabecera
    @FXML private Label lblTituloItalica;

    // Botones de selección de tema
    @FXML private ToggleButton tglClaro;
    @FXML private ToggleButton tglOscuro;
    @FXML private ToggleButton tglSistema;

    // Botones de tamaño de fuente
    @FXML private ToggleButton tglTextoPeq;
    @FXML private ToggleButton tglTextoNorm;
    @FXML private ToggleButton tglTextoGrande;
    @FXML private ToggleButton tglTextoExtra;

    // Botones de color principal
    @FXML private ToggleButton tglColorVerde;
    @FXML private ToggleButton tglColorAzul;
    @FXML private ToggleButton tglColorTerracota;
    @FXML private ToggleButton tglColorMorado;

    // Botones de densidad de interfaz
    @FXML private ToggleButton tglComoda;
    @FXML private ToggleButton tglCompacta;

    // Componentes de la tarjeta de vista previa
    @FXML private VBox boxPreviewBody;
    @FXML private ProgressBar barPreviewPlazas;

    // Grupos para que solo haya una opción marcada por cada categoría
    private final ToggleGroup grupoTema = new ToggleGroup();
    private final ToggleGroup grupoTamano = new ToggleGroup();
    private final ToggleGroup grupoColor = new ToggleGroup();
    private final ToggleGroup grupoDensidad = new ToggleGroup();

    // Inicializa los grupos, carga las preferencias guardadas y activa los eventos
    @FXML
    public void initialize() {
        configurarGrupos();
        cargarPreferenciasGuardadas();
        configurarListeners();
        actualizarVistaPrevia();
    }

    // Asigna el valor interno (userData) a cada botón y los agrupa
    private void configurarGrupos() {
        tglClaro.setUserData("claro");
        tglOscuro.setUserData("oscuro");
        tglSistema.setUserData("sistema");
        asignarGrupoObligatorio(grupoTema, tglClaro, tglOscuro, tglSistema);

        tglTextoPeq.setUserData("pequeno");
        tglTextoNorm.setUserData("normal");
        tglTextoGrande.setUserData("grande");
        tglTextoExtra.setUserData("extragrande");
        asignarGrupoObligatorio(grupoTamano, tglTextoPeq, tglTextoNorm, tglTextoGrande, tglTextoExtra);

        tglColorVerde.setUserData("#1B4632");
        tglColorAzul.setUserData("#256B9A");
        tglColorTerracota.setUserData("#C45A32");
        tglColorMorado.setUserData("#7A3E7A");
        asignarGrupoObligatorio(grupoColor, tglColorVerde, tglColorAzul, tglColorTerracota, tglColorMorado);

        tglComoda.setUserData("comoda");
        tglCompacta.setUserData("compacta");
        asignarGrupoObligatorio(grupoDensidad, tglComoda, tglCompacta);
    }

    // Evita que un grupo se quede sin ningún botón seleccionado al volver a pulsar el activo
    private void asignarGrupoObligatorio(ToggleGroup grupo, ToggleButton... botones) {
        for (ToggleButton btn : botones) {
            btn.setToggleGroup(grupo);
        }
        grupo.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            if (nuevo == null && anterior != null) {
                anterior.setSelected(true);
            }
        });
    }

    // Marca en la interfaz las preferencias guardadas anteriormente en el equipo
    private void cargarPreferenciasGuardadas() {
        seleccionarPorData(grupoTema, GestorApariencia.getTema());
        seleccionarPorData(grupoTamano, GestorApariencia.getTamano());
        seleccionarPorData(grupoColor, GestorApariencia.getColor());
        seleccionarPorData(grupoDensidad, GestorApariencia.getDensidad());
    }

    // Busca y selecciona dentro de un grupo el botón cuyo userData coincida con el valor
    private void seleccionarPorData(ToggleGroup grupo, String valor) {
        for (Toggle t : grupo.getToggles()) {
            if (valor.equals(t.getUserData())) {
                t.setSelected(true);
                break;
            }
        }
    }

    // Guarda cada cambio y lo aplica en tiempo real al pulsar una opción
    private void configurarListeners() {
        grupoTema.selectedToggleProperty().addListener((obs, ant, nuevo) -> {
            if (nuevo != null) {
                GestorApariencia.setTema(nuevo.getUserData().toString());
                aplicarGlobal();
            }
        });

        grupoTamano.selectedToggleProperty().addListener((obs, ant, nuevo) -> {
            if (nuevo != null) {
                GestorApariencia.setTamano(nuevo.getUserData().toString());
                aplicarGlobal();
            }
        });

        grupoColor.selectedToggleProperty().addListener((obs, ant, nuevo) -> {
            if (nuevo != null) {
                GestorApariencia.setColor(nuevo.getUserData().toString());
                aplicarGlobal();
            }
        });

        grupoDensidad.selectedToggleProperty().addListener((obs, ant, nuevo) -> {
            if (nuevo != null) {
                GestorApariencia.setDensidad(nuevo.getUserData().toString());
                aplicarGlobal();
            }
        });
    }

    // Aplica los estilos globales a toda la ventana y refresca la tarjeta de ejemplo
    private void aplicarGlobal() {
        GestorApariencia.aplicar(lblTituloItalica);
        actualizarVistaPrevia();
    }

    // Actualiza el check del color activo y el espaciado en la tarjeta de vista previa
    private void actualizarVistaPrevia() {
        for (Toggle t : grupoColor.getToggles()) {
            ToggleButton btn = (ToggleButton) t;
            btn.setText(btn.isSelected() ? "✓" : "");
        }

        // Usa la variable -forest para que adapte su brillo automáticamente en modo oscuro
        barPreviewPlazas.setStyle("-fx-accent: -forest;");

        if ("compacta".equals(GestorApariencia.getDensidad())) {
            boxPreviewBody.setSpacing(5.0);
            boxPreviewBody.setStyle("-fx-padding: 10;");
        } else {
            boxPreviewBody.setSpacing(10.0);
            boxPreviewBody.setStyle("-fx-padding: 16;");
        }
    }
}