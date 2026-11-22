package org.example.activa.controller.usuario;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.activa.dao.MetodoPagoDAO;
import org.example.activa.model.Pago;
import org.example.activa.model.enums.EstadoPago;
import org.example.activa.model.enums.MetodoPago;
import org.example.activa.service.PagoService;
import org.example.activa.util.Iconos;
import org.example.activa.util.Navegador;
import org.example.activa.util.Sesion;
import org.example.activa.util.Vista;
import org.example.activa.util.AlertUtil;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

// Clase controladora que gestiona la vista de pagos, actualiza los resúmenes y procesa los cobros
public class PagoController {

    // Vista cuando no hay sesión iniciada
    @FXML private VBox panelInvitado;
    @FXML private StackPane iconoInvitado;

    // Botón superior y banner de aviso
    @FXML private Button btnMetodosPago;
    @FXML private HBox bannerPendiente;
    @FXML private Label lblBannerTitulo;
    @FXML private Label lblBannerDetalle;

    // Tarjetas KPI de resumen
    @FXML private HBox cajaKpis;
    @FXML private Label lblTotalPagado;
    @FXML private Label lblCountPagado;
    @FXML private Label lblTotalPendiente;
    @FXML private Label lblCountPendiente;
    @FXML private Label lblTotalReembolsado;
    @FXML private Label lblCountReembolsado;

    // Contenedor de filas de la tabla
    @FXML private VBox cajaTabla;
    @FXML private VBox contenedorFilasPagos;

    private final PagoService pagoService = new PagoService();
    private List<Pago> listaPagosActual;

    private final Locale localeEs = Locale.forLanguageTag("es-ES");
    private final DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd MMM", localeEs);

    // Carga los datos de pagos o muestra el estado de invitado si no hay sesión iniciada
    @FXML
    public void initialize() {
        if (iconoInvitado != null) {
            iconoInvitado.getChildren().setAll(Iconos.crear("card", 24));
        }

        if (!Sesion.haySesion()) {
            aplicarEstadoSesion(false);
            return;
        }

        aplicarEstadoSesion(true);
        cargarDatosPagos();
    }

    // Alterna entre el contenido de pagos del usuario y el aviso de inicio de sesión
    private void aplicarEstadoSesion(boolean haySesion) {
        mostrar(btnMetodosPago, haySesion);
        mostrar(cajaKpis, haySesion);
        mostrar(cajaTabla, haySesion);
        mostrar(panelInvitado, !haySesion);

        if (!haySesion) {
            mostrar(bannerPendiente, false);
        }
    }

    private void mostrar(Node nodo, boolean visible) {
        if (nodo != null) {
            nodo.setVisible(visible);
            nodo.setManaged(visible);
        }
    }

    @FXML
    private void onIniciarSesion() {
        Navegador.mostrarPantallaCompleta(Vista.LOGIN);
    }

    // Consulta los pagos del usuario conectado en MySQL y actualiza toda la vista
    private void cargarDatosPagos() {
        listaPagosActual = pagoService.obtenerPagosDeUsuario(Sesion.getIdUsuario());

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
            mostrar(bannerPendiente, false);
        } else {
            mostrar(bannerPendiente, true);

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

        // Columna Actividad (Icono + Título)
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

        // Columna Fecha
        String textoFecha = pago.getFechaPago() != null ? pago.getFechaPago().format(formatoFecha) : "—";
        Label lblFecha = new Label(textoFecha);
        lblFecha.setPrefWidth(110.0);
        lblFecha.getStyleClass().add(pago.getFechaPago() != null ? "pag-cell-text" : "pag-cell-muted");

        // Columna Método
        String textoMetodo = pago.getMetodoPago() != null ? pago.getMetodoPago().toString() : "—";
        Label lblMetodo = new Label(textoMetodo);
        lblMetodo.setPrefWidth(160.0);
        lblMetodo.getStyleClass().add(pago.getMetodoPago() != null ? "pag-cell-text" : "pag-cell-muted");

        // Columna Importe
        Label lblImporte = new Label(formatearEuros(pago.getImporte()));
        lblImporte.setPrefWidth(110.0);
        lblImporte.setAlignment(Pos.CENTER_RIGHT);
        lblImporte.getStyleClass().add("pag-cell-amount");

        // Columna Estado
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

        // Columna Acción con el botón para pagar o descargar recibo
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

    // Comprueba métodos guardados, redirige si no hay, o permite elegir uno y actualiza el estado en MySQL
    private void procesarPago(Pago pago) {
        MetodoPagoDAO metodoPagoDAO = new MetodoPagoDAO();
        List<org.example.activa.model.MetodoPago> metodosGuardados = metodoPagoDAO.obtenerMetodosPorUsuario(Sesion.getIdUsuario());

        if (metodosGuardados.isEmpty()) {
            boolean irACrear = AlertUtil.confirmar(
                    "No tienes ningún método de pago",
                    "Para abonar la inscripción necesitas añadir una tarjeta o Bizum a tu perfil.",
                    "Añadir método", "Cancelar");
            if (irACrear) {
                Navegador.irA(Vista.METODOS_PAGO);
            }
            return;
        }

        List<String> opciones = metodosGuardados.stream()
                .map(m -> m.getTipo() + " - " + m.getIdentificador() + (m.isEsPredeterminado() ? " (Predeterminado)" : ""))
                .toList();

        String porDefecto = metodosGuardados.stream()
                .filter(org.example.activa.model.MetodoPago::isEsPredeterminado)
                .map(m -> m.getTipo() + " - " + m.getIdentificador() + " (Predeterminado)")
                .findFirst()
                .orElse(opciones.get(0));

        Optional<String> seleccion = AlertUtil.elegir(
                "Completar pago",
                "Abonar " + formatearEuros(pago.getImporte()) + " — " + pago.getTituloActividad()
                        + ".\nElige con qué método pagar:",
                opciones, porDefecto);

        seleccion.ifPresent(seleccionStr -> {
            org.example.activa.model.MetodoPago metodoElegido = metodosGuardados.stream()
                    .filter(m -> seleccionStr.startsWith(m.getTipo() + " - " + m.getIdentificador()))
                    .findFirst()
                    .orElse(metodosGuardados.get(0));

            MetodoPago enumMetodoPago = MetodoPago.valueOf(metodoElegido.getTipo().toUpperCase());

            boolean ok = pagoService.abonarPagoPendiente(pago.getIdPago(), enumMetodoPago);
            if (ok) {
                cargarDatosPagos();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Pago completado", "El pago se ha registrado correctamente.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo completar el pago en la base de datos.");
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
        Navegador.irA(Vista.METODOS_PAGO);
    }

    // Formatea un importe numérico a euros con dos decimales
    private String formatearEuros(double cantidad) {
        return String.format(localeEs, "%.2f €", cantidad);
    }

    // Muestra una alerta con el estilo de la app
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        AlertUtil.crear(tipo, titulo, mensaje).showAndWait();
    }
}