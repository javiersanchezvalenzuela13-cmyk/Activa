package org.example.activa.controller.usuario;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.activa.model.Pago;
import org.example.activa.model.enums.EstadoPago;
import org.example.activa.model.enums.MetodoPago;
import org.example.activa.service.PagoService;
import org.example.activa.util.Sesion;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

// Clase controladora que gestiona la vista de pagos, actualiza los resúmenes y procesa los cobros
public class PagoController {

    // Botón superior y banner de aviso
    @FXML private Button btnMetodosPago;
    @FXML private HBox bannerPendiente;
    @FXML private Label lblBannerTitulo;
    @FXML private Label lblBannerDetalle;
    @FXML private Button btnPagarAhora;

    // Tarjetas KPI de resumen
    @FXML private Label lblTotalPagado;
    @FXML private Label lblCountPagado;
    @FXML private Label lblTotalPendiente;
    @FXML private Label lblCountPendiente;
    @FXML private Label lblTotalReembolsado;
    @FXML private Label lblCountReembolsado;

    // Contenedor de filas de la tabla
    @FXML private VBox contenedorFilasPagos;

    private final PagoService pagoService = new PagoService();
    private List<Pago> listaPagosActual;

    private final Locale localeEs = Locale.forLanguageTag("es-ES");
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd MMM", localeEs);

    // Carga los datos de pagos al abrir la pantalla
    @FXML
    public void initialize() {
        cargarDatosPagos();
    }

    // Consulta los pagos del usuario conectado en MySQL y actualiza toda la vista
    private void cargarDatosPagos() {
        int idUsuario = Sesion.haySesion() ? Sesion.getIdUsuario() : 1;
        listaPagosActual = pagoService.obtenerPagosDeUsuario(idUsuario);

        actualizarTarjetasResumen(listaPagosActual);
        actualizarBannerPendientes(listaPagosActual);
        pintarTablaPagos(listaPagosActual);
    }

    // Rellena las tres tarjetas superiores (completado, pendiente y reembolsado)
    private void actualizarTarjetasResumen(List<Pago> pagos) {
        double totalPagado = pagoService.calcularTotalPagado(pagos);
        double totalPendiente = pagoService.calcularTotalPendiente(pagos);
        double totalReembolsado = 0.0;

        int countPagado = 0;
        int countPendiente = 0;
        int countReembolsado = 0;

        for (Pago p : pagos) {
            if (p.getEstado() == EstadoPago.COMPLETADO) {
                countPagado++;
            } else if (p.getEstado() == EstadoPago.PENDIENTE) {
                countPendiente++;
            } else if (p.getEstado() == EstadoPago.REEMBOLSADO) {
                totalReembolsado += p.getImporte();
                countReembolsado++;
            }
        }

        lblTotalPagado.setText(formatearEuros(totalPagado));
        lblCountPagado.setText(countPagado == 1 ? "1 actividad" : countPagado + " actividades");

        lblTotalPendiente.setText(formatearEuros(totalPendiente));
        lblCountPendiente.setText(countPendiente == 1 ? "1 pago" : countPendiente + " pagos");

        lblTotalReembolsado.setText(formatearEuros(totalReembolsado));
        lblCountReembolsado.setText(countReembolsado == 1 ? "1 devolución" : countReembolsado + " devoluciones");
    }

    // Muestra u oculta el banner superior según si hay pagos pendientes
    private void actualizarBannerPendientes(List<Pago> pagos) {
        List<Pago> pendientes = pagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.PENDIENTE)
                .toList();

        if (pendientes.isEmpty()) {
            bannerPendiente.setVisible(false);
            bannerPendiente.setManaged(false);
        } else {
            bannerPendiente.setVisible(true);
            bannerPendiente.setManaged(true);

            int cantidad = pendientes.size();
            lblBannerTitulo.setText(cantidad == 1
                    ? "Tienes 1 pago pendiente"
                    : "Tienes " + cantidad + " pagos pendientes");

            Pago primero = pendientes.get(0);
            String nombreAct = primero.getTituloActividad() != null ? primero.getTituloActividad() : "Actividad";
            lblBannerDetalle.setText(nombreAct + " · " + formatearEuros(primero.getImporte())
                    + ". Págalo antes de la actividad para mantener tu plaza.");
        }
    }

    // Limpia las filas anteriores y genera una fila visual por cada pago de la lista
    private void pintarTablaPagos(List<Pago> pagos) {
        contenedorFilasPagos.getChildren().clear();

        if (pagos.isEmpty()) {
            Label lblVacio = new Label("Todavía no tienes movimientos registrados.");
            lblVacio.getStyleClass().add("pag-cell-muted");
            lblVacio.setStyle("-fx-padding: 24px;");
            contenedorFilasPagos.getChildren().add(lblVacio);
            return;
        }

        for (int i = 0; i < pagos.size(); i++) {
            Pago pago = pagos.get(i);
            boolean esUltima = (i == pagos.size() - 1);
            contenedorFilasPagos.getChildren().add(crearFilaPago(pago, esUltima));
        }
    }

    // Construye el HBox completo de una fila de pago
    private HBox crearFilaPago(Pago pago, boolean esUltima) {
        HBox fila = new HBox();
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("pag-row");
        if (esUltima) {
            fila.getStyleClass().add("pag-row-last");
        }

        // 1. Columna Actividad (Icono + Título)
        HBox colActividad = new HBox(14.0);
        colActividad.setAlignment(Pos.CENTER_LEFT);
        colActividad.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(colActividad, Priority.ALWAYS);

        StackPane iconoBox = new StackPane();
        iconoBox.getStyleClass().addAll("pag-cat-icon", pago.getIdPago() % 2 == 0 ? "pag-cat-deporte" : "pag-cat-arte");
        Label lblEmoji = new Label(pago.getIdPago() % 2 == 0 ? "⛰" : "🏺");
        lblEmoji.getStyleClass().add("pag-cat-emoji");
        iconoBox.getChildren().add(lblEmoji);

        VBox textosAct = new VBox(2.0);
        Label lblTitulo = new Label(pago.getTituloActividad() != null ? pago.getTituloActividad() : "Inscripción #" + pago.getIdInscripcion());
        lblTitulo.getStyleClass().add("pag-act-title");
        Label lblSub = new Label("Actividad");
        lblSub.getStyleClass().add("pag-act-cat");
        textosAct.getChildren().addAll(lblTitulo, lblSub);

        colActividad.getChildren().addAll(iconoBox, textosAct);

        // 2. Columna Fecha
        String textoFecha = pago.getFechaPago() != null ? pago.getFechaPago().format(formatoFecha) : "—";
        Label lblFecha = new Label(textoFecha);
        lblFecha.setPrefWidth(110.0);
        lblFecha.getStyleClass().add(pago.getFechaPago() != null ? "pag-cell-text" : "pag-cell-muted");

        // 3. Columna Método
        String textoMetodo = pago.getMetodoPago() != null ? pago.getMetodoPago().toString() : "—";
        Label lblMetodo = new Label(textoMetodo);
        lblMetodo.setPrefWidth(160.0);
        lblMetodo.getStyleClass().add(pago.getMetodoPago() != null ? "pag-cell-text" : "pag-cell-muted");

        // 4. Columna Importe
        Label lblImporte = new Label(formatearEuros(pago.getImporte()));
        lblImporte.setPrefWidth(110.0);
        lblImporte.setAlignment(Pos.CENTER_RIGHT);
        lblImporte.getStyleClass().add("pag-cell-amount");

        // 5. Columna Estado
        HBox colEstado = new HBox();
        colEstado.setAlignment(Pos.CENTER_LEFT);
        colEstado.setPrefWidth(150.0);
        colEstado.getStyleClass().add("pag-cell-estado");

        Label badgeEstado = new Label();
        badgeEstado.getStyleClass().add("pag-badge");
        if (pago.getEstado() == EstadoPago.PENDIENTE) {
            badgeEstado.setText("Pendiente");
            badgeEstado.getStyleClass().add("pag-badge-pendiente");
        } else if (pago.getEstado() == EstadoPago.COMPLETADO) {
            badgeEstado.setText("Pagado");
            badgeEstado.getStyleClass().add("pag-badge-pagado");
        } else {
            badgeEstado.setText("Reembolsado");
            badgeEstado.getStyleClass().add("pag-badge-reembolsado");
        }
        colEstado.getChildren().add(badgeEstado);

        // 6. Columna Acción con el botón para pagar o descargar recibo
        HBox colAccion = new HBox();
        colAccion.setAlignment(Pos.CENTER_RIGHT);
        colAccion.setPrefWidth(110.0);

        if (pago.getEstado() == EstadoPago.PENDIENTE) {
            Button btnPagar = new Button("Pagar");
            btnPagar.getStyleClass().add("pag-btn-primary-sm");
            btnPagar.setOnAction(e -> procesarPago(pago));
            colAccion.getChildren().add(btnPagar);
        } else {
            Button btnRecibo = new Button("⬇  Recibo");
            btnRecibo.getStyleClass().add("pag-btn-outline-sm");
            btnRecibo.setOnAction(e -> mostrarRecibo(pago));
            colAccion.getChildren().add(btnRecibo);
        }

        fila.getChildren().addAll(colActividad, lblFecha, lblMetodo, lblImporte, colEstado, colAccion);
        return fila;
    }

    // Acción del botón superior del banner para abonar el primer pago pendiente
    @FXML
    private void pagarSiguientePendiente() {
        if (listaPagosActual == null) return;

        listaPagosActual.stream()
                .filter(p -> p.getEstado() == EstadoPago.PENDIENTE)
                .findFirst()
                .ifPresent(this::procesarPago);
    }

    // Abre el diálogo de selección de método de pago y actualiza el estado en MySQL
    private void procesarPago(Pago pago) {
        ChoiceDialog<MetodoPago> dialogo = new ChoiceDialog<>(MetodoPago.TARJETA, MetodoPago.values());
        dialogo.setTitle("Completar pago");
        dialogo.setHeaderText("Abonar " + formatearEuros(pago.getImporte()) + " — " + pago.getTituloActividad());
        dialogo.setContentText("Elige el método de pago:");

        Optional<MetodoPago> seleccion = dialogo.showAndWait();
        seleccion.ifPresent(metodo -> {
            boolean ok = pagoService.abonarPagoPendiente(pago.getIdPago(), metodo);
            if (ok) {
                cargarDatosPagos();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo registrar el pago en la base de datos.");
            }
        });
    }

    // Muestra una ventana emergente con el comprobante del pago
    private void mostrarRecibo(Pago pago) {
        String fecha = pago.getFechaPago() != null ? pago.getFechaPago().format(formatoFecha) : "Sin fecha";
        String metodo = pago.getMetodoPago() != null ? pago.getMetodoPago().toString() : "No especificado";

        String detalle = "Actividad: " + pago.getTituloActividad() + "\n"
                + "Importe: " + formatearEuros(pago.getImporte()) + "\n"
                + "Método: " + metodo + "\n"
                + "Fecha: " + fecha + "\n"
                + "Estado: " + pago.getEstado().toString();

        mostrarAlerta(Alert.AlertType.INFORMATION, "Comprobante de pago #" + pago.getIdPago(), detalle);
    }

    // Acción del botón superior de métodos de pago
    @FXML
    private void abrirMetodosPago() {
        mostrarAlerta(Alert.AlertType.INFORMATION,
                "Métodos de pago disponibles",
                "Puedes abonar tus inscripciones mediante Tarjeta, Bizum o Efectivo al confirmar el pago.");
    }

    // Formatea un importe numérico a euros con dos decimales
    private String formatearEuros(double cantidad) {
        return String.format(localeEs, "%.2f €", cantidad);
    }

    // Muestra una alerta simple al usuario
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}