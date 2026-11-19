package org.example.activa.controller.usuario;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.example.activa.exception.ActividadException;
import org.example.activa.exception.InscripcionException;
import org.example.activa.model.Actividad;
import org.example.activa.model.Inscripcion;
import org.example.activa.service.ActividadService;
import org.example.activa.service.InscripcionService;
import org.example.activa.util.Acceso;
import org.example.activa.util.Sesion;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class DetalleActividadController {

    // Usuario de prueba hasta que exista Sesion (Persona 1)
    private static final int ID_USUARIO_PRUEBA = 1;

    // Formatos en español
    private static final Locale ES = Locale.forLanguageTag("es-ES");
    private static final DateTimeFormatter FORMATO_FECHA_LARGA =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", ES);
    private static final DateTimeFormatter FORMATO_FECHA_CORTA =
            DateTimeFormatter.ofPattern("EEE d MMM", ES);
    private static final NumberFormat FORMATO_PRECIO = NumberFormat.getCurrencyInstance(ES);

    // Elementos del FXML
    @FXML private Label lblPrecio;
    @FXML private Label lblNombre;
    @FXML private Label lblResumen;
    @FXML private Label lblDescripcion;
    @FXML private Label lblFecha;
    @FXML private Label lblHorario;
    @FXML private Label lblLugar;
    @FXML private Label lblPlazasNumero;
    @FXML private ProgressBar barraPlazas;
    @FXML private Label lblPlazasTexto;
    @FXML private Label lblEstado;
    @FXML private Button btnAccion;

    // Services
    private final ActividadService actividadService = new ActividadService();
    private final InscripcionService inscripcionService = new InscripcionService();

    // Datos de la pantalla
    private Actividad actividad;
    private Inscripcion miInscripcion;
    private Runnable alVolver;

    // Lo llama InicioController al abrir esta pantalla
    public void setActividad(Actividad actividad, Runnable alVolver) {
        this.actividad = actividad;
        this.alVolver = alVolver;
        refrescar();
    }

    // Vuelve a leer la actividad y mi inscripción de la BD
    private void refrescar() {
        try {
            actividad = actividadService.buscarPorId(actividad.getIdActividad());

            miInscripcion = !Sesion.haySesion() ? null
                    : inscripcionService.listarMisInscripciones(Sesion.getIdUsuario()).stream()
                    .filter(i -> i.getIdActividad() == actividad.getIdActividad() && i.estaActiva())
                    .findFirst()
                    .orElse(null);
            pintar();
        } catch (ActividadException | InscripcionException e) {
            mostrarError(e.getMessage());
        }
    }

    // Rellena todos los elementos con los datos
    private void pintar() {
        // Cabecera
        lblPrecio.setText(actividad.esGratis() ? "Gratis" : FORMATO_PRECIO.format(actividad.getPrecio()));
        lblPrecio.getStyleClass().remove("gratis");
        if (actividad.esGratis()) {
            lblPrecio.getStyleClass().add("gratis");
        }
        lblNombre.setText(actividad.getNombreActividad());
        lblResumen.setText(FORMATO_FECHA_CORTA.format(actividad.getFechaActividad())
                + " · " + actividad.getLugar());

        // Información
        String descripcion = actividad.getDescripcion();
        lblDescripcion.setText(descripcion == null || descripcion.isBlank() ? "Sin descripción." : descripcion);
        lblFecha.setText(capitalizar(FORMATO_FECHA_LARGA.format(actividad.getFechaActividad())));
        lblHorario.setText(actividad.getHoraInicio() + " – " + actividad.getHoraFin());
        lblLugar.setText(actividad.getLugar());

        // Plazas
        lblPlazasNumero.setText(actividad.getCupoActual() + " / " + actividad.getCupoMax());
        barraPlazas.setProgress((double) actividad.getCupoActual() / actividad.getCupoMax());
        barraPlazas.getStyleClass().removeAll("pocas", "llena");
        if (actividad.estaCompleta()) {
            barraPlazas.getStyleClass().add("llena");
        } else if (actividad.getPlazasLibres() <= 3) {
            barraPlazas.getStyleClass().add("pocas");
        }
        lblPlazasTexto.setText(textoPlazas());

        // Estado de mi inscripción
        boolean inscrito = miInscripcion != null;
        lblEstado.setVisible(inscrito);
        lblEstado.setManaged(inscrito);
        lblEstado.getStyleClass().remove("pendiente");
        if (inscrito && miInscripcion.estaPendienteDePago()) {
            lblEstado.setText("Plaza reservada. Pendiente de pago.");
            lblEstado.getStyleClass().add("pendiente");
        } else if (inscrito) {
            lblEstado.setText("Estás inscrito en esta actividad.");
        }

        // Botón
        btnAccion.getStyleClass().removeAll("boton-apuntarme", "boton-cancelar");
        btnAccion.setDisable(false);
        if (inscrito) {
            btnAccion.setText("Cancelar inscripción");
            btnAccion.getStyleClass().add("boton-cancelar");
        } else if (actividad.estaCompleta()) {
            btnAccion.setText("Completa");
            btnAccion.getStyleClass().add("boton-apuntarme");
            btnAccion.setDisable(true);
        } else {
            btnAccion.setText("Apuntarme");
            btnAccion.getStyleClass().add("boton-apuntarme");
        }
    }

    // Botón "Volver"
    @FXML
    private void onVolver() {
        if (alVolver != null) {
            alVolver.run();
        }
    }

    // Botón principal: apuntarme o cancelar
    @FXML
    private void onAccion() {
        if (!Sesion.haySesion()) {
            Acceso.pedirLogin("apuntarte");
            return;
        }
        if (miInscripcion != null) {
            cancelar();
        } else {
            inscribir();
        }
    }

    private void inscribir() {
        try {
            Inscripcion i = inscripcionService.inscribir(ID_USUARIO_PRUEBA, actividad.getIdActividad());
            mostrarInfo(i.estaPendienteDePago()
                    ? "Plaza reservada. Completa el pago para confirmarla."
                    : "Te has apuntado a \"" + actividad.getNombreActividad() + "\".");
        } catch (InscripcionException e) {
            mostrarError(e.getMessage());
        }
        refrescar();
    }

    private void cancelar() {
        if (!confirmar("¿Seguro que quieres cancelar tu inscripción?")) {
            return;
        }
        try {
            inscripcionService.cancelar(ID_USUARIO_PRUEBA, actividad.getIdActividad());
            mostrarInfo("Has cancelado tu inscripción.");
        } catch (InscripcionException e) {
            mostrarError(e.getMessage());
        }
        refrescar();
    }

    // Texto de plazas libres
    private String textoPlazas() {
        int libres = actividad.getPlazasLibres();
        if (libres == 0) return "No quedan plazas";
        if (libres == 1) return "Queda 1 plaza libre";
        return "Quedan " + libres + " plazas libres";
    }

    // Pone la primera letra en mayúscula ("miércoles" → "Miércoles")
    private String capitalizar(String texto) {
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    // Alertas (se moverán a AlertUtil cuando exista)
    private void mostrarInfo(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    private boolean confirmar(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.YES, ButtonType.NO);
        alerta.setHeaderText(null);
        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == ButtonType.YES;
    }
}