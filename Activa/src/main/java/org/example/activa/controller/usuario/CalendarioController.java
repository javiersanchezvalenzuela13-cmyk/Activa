package org.example.activa.controller.usuario;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import org.example.activa.dao.ActividadDAO;
import org.example.activa.db.ConexionBD;
import org.example.activa.model.Actividad;
import org.example.activa.util.AlertUtil;
import org.example.activa.util.Sesion;
import org.example.activa.util.Navegador;
import org.example.activa.util.Vista;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

// Clase controladora que gestiona la vista del calendario mensual y las actividades inscritas del usuario
public class CalendarioController {

    // Cabecera y botones de navegación
    @FXML private Label lblMesAnio;
    @FXML private Button btnMesAnterior;
    @FXML private Button btnHoy;
    @FXML private Button btnMesSiguiente;

    // Filtros superiores por categoría
    @FXML private ToggleButton tglDeporte;
    @FXML private ToggleButton tglArte;
    @FXML private ToggleButton tglCultura;
    @FXML private ToggleButton tglComunidad;

    // Rejilla principal del calendario
    @FXML private GridPane gridCalendario;

    // Panel derecho con el detalle del día seleccionado
    @FXML private Label lblDiaSeleccionado;
    @FXML private Label lblContadorActividades;
    @FXML private VBox contenedorEventosDia;

    private YearMonth mesActual = YearMonth.now();
    private LocalDate fechaSeleccionada = LocalDate.now();

    // Mapa que agrupa las actividades del usuario por fecha
    private final Map<LocalDate, List<ActividadCalendario>> actividadesPorFecha = new HashMap<>();
    private final ActividadDAO actividadDAO = new ActividadDAO();

    private final Locale localeEs = Locale.forLanguageTag("es-ES");
    private final DateTimeFormatter formatoDiaDerecha = DateTimeFormatter.ofPattern("d 'de' MMMM", localeEs);

    // Inicializa los controles, carga las actividades desde MySQL y dibuja el mes actual
    @FXML
    public void initialize() {
        configurarControles();
        cargarActividadesUsuario();
        refrescarVista();
    }

    // Enlaza los botones de navegación y los filtros de categoría
    private void configurarControles() {
        btnMesAnterior.setOnAction(e -> {
            mesActual = mesActual.minusMonths(1);
            refrescarVista();
        });

        btnMesSiguiente.setOnAction(e -> {
            mesActual = mesActual.plusMonths(1);
            refrescarVista();
        });

        btnHoy.setOnAction(e -> {
            fechaSeleccionada = LocalDate.now();
            mesActual = YearMonth.now();
            refrescarVista();
        });

        tglDeporte.setOnAction(e -> refrescarVista());
        tglArte.setOnAction(e -> refrescarVista());
        tglCultura.setOnAction(e -> refrescarVista());
        tglComunidad.setOnAction(e -> refrescarVista());
    }

    // Redibuja tanto la cuadrícula del mes como el panel lateral del día
    private void refrescarVista() {
        dibujarCalendario();
        mostrarActividadesDelDia(fechaSeleccionada);
    }

    // Consulta en MySQL las actividades en las que está inscrito el usuario conectado
    private void cargarActividadesUsuario() {
        actividadesPorFecha.clear();
        String sql = """
                SELECT a.id_actividad,
                       a.nombre_actividad,
                       a.fecha_actividad,
                       a.hora_inicio,
                       a.lugar,
                       COALESCE(GROUP_CONCAT(ae.etiqueta SEPARATOR ', '), 'Comunidad') AS categoria
                FROM inscripcion i
                INNER JOIN actividad a ON i.id_actividad = a.id_actividad
                LEFT JOIN actividad_etiqueta ae ON a.id_actividad = ae.id_actividad
                WHERE i.id_usuario = ?
                  AND i.estado_inscripcion != 'CANCELADA'
                GROUP BY a.id_actividad, a.nombre_actividad, a.fecha_actividad, a.hora_inicio, a.lugar
                ORDER BY a.fecha_actividad ASC, a.hora_inicio ASC
                """;

        try (Connection conn = ConexionBD.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idUsuario = Sesion.haySesion() ? Sesion.getIdUsuario() : 1;
            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date sqlDate = rs.getDate("fecha_actividad");
                    if (sqlDate == null) continue;

                    int idActividad = rs.getInt("id_actividad");
                    LocalDate fecha = sqlDate.toLocalDate();
                    String titulo = rs.getString("nombre_actividad");
                    java.sql.Time sqlTime = rs.getTime("hora_inicio");
                    String hora = sqlTime != null ? sqlTime.toLocalTime().toString() : "Por confirmar";
                    String ubicacion = rs.getString("lugar") != null ? rs.getString("lugar") : "Sin ubicación";
                    String categoria = rs.getString("categoria") != null ? rs.getString("categoria") : "Comunidad";

                    ActividadCalendario item = new ActividadCalendario(idActividad, titulo, fecha, hora, ubicacion, categoria);
                    actividadesPorFecha.computeIfAbsent(fecha, k -> new ArrayList<>()).add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar actividades en CalendarioController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Rellena la rejilla del mes actual ajustando dinámicamente 5 o 6 filas
    private void dibujarCalendario() {
        String nombreMes = mesActual.getMonth().getDisplayName(TextStyle.FULL, localeEs);
        nombreMes = nombreMes.substring(0, 1).toUpperCase() + nombreMes.substring(1);
        lblMesAnio.setText(nombreMes + " " + mesActual.getYear());

        gridCalendario.getChildren().clear();
        gridCalendario.getRowConstraints().clear();

        LocalDate primerDiaMes = mesActual.atDay(1);
        int desplazamiento = primerDiaMes.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
        int totalDiasVisibles = desplazamiento + mesActual.lengthOfMonth();
        int numFilas = (int) Math.ceil(totalDiasVisibles / 7.0);

        for (int i = 0; i < numFilas; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / numFilas);
            gridCalendario.getRowConstraints().add(rc);
        }

        LocalDate fechaCursor = primerDiaMes.minusDays(desplazamiento);

        for (int fila = 0; fila < numFilas; fila++) {
            for (int col = 0; col < 7; col++) {
                VBox celdaDia = crearCeldaDia(fechaCursor);
                gridCalendario.add(celdaDia, col, fila);
                fechaCursor = fechaCursor.plusDays(1);
            }
        }
    }

    // Construye cada casilla del calendario aplicando las clases de calendario.css
    private VBox crearCeldaDia(LocalDate fecha) {
        VBox celda = new VBox(4.0);
        celda.setPadding(new Insets(6, 8, 6, 8));
        celda.getStyleClass().add("cal-celda");
        celda.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        GridPane.setHgrow(celda, Priority.ALWAYS);
        GridPane.setVgrow(celda, Priority.ALWAYS);

        boolean esMesActual = YearMonth.from(fecha).equals(mesActual);
        boolean esHoy = fecha.equals(LocalDate.now());
        boolean esSeleccionado = fecha.equals(fechaSeleccionada);

        if (!esMesActual) {
            celda.getStyleClass().add("cal-celda-otro-mes");
        }
        if (esSeleccionado) {
            celda.getStyleClass().add("cal-celda-seleccionada");
        }

        Label lblNumero = new Label(String.valueOf(fecha.getDayOfMonth()));
        lblNumero.getStyleClass().add("cal-numero-dia");
        if (esHoy) {
            lblNumero.getStyleClass().add("cal-dia-hoy");
        }
        celda.getChildren().add(lblNumero);

        // Etiquetas de actividades filtradas por categoría (clicables para ir al detalle)
        List<ActividadCalendario> eventosDia = obtenerEventosFiltrados(fecha);
        int limite = Math.min(eventosDia.size(), 2);

        for (int i = 0; i < limite; i++) {
            ActividadCalendario ev = eventosDia.get(i);
            Label pill = new Label(ev.titulo());
            pill.setMaxWidth(Double.MAX_VALUE);
            pill.setCursor(Cursor.HAND);
            pill.getStyleClass().addAll("cal-evento-pill", obtenerClaseCategoria(ev.categoria()));
            pill.setOnMouseClicked(e -> {
                e.consume(); // Evita que el clic se propague a la celda y navega directo
                abrirDetalleActividad(ev.idActividad());
            });
            celda.getChildren().add(pill);
        }

        if (eventosDia.size() > 2) {
            Label lblMas = new Label("+" + (eventosDia.size() - 2) + " más");
            lblMas.getStyleClass().add("cal-texto-secundario");
            celda.getChildren().add(lblMas);
        }

        celda.setOnMouseClicked(e -> {
            fechaSeleccionada = fecha;
            if (!esMesActual) {
                mesActual = YearMonth.from(fecha);
            }
            refrescarVista();
        });

        return celda;
    }

    // Rellena el panel derecho con las actividades del día seleccionado
    private void mostrarActividadesDelDia(LocalDate fecha) {
        lblDiaSeleccionado.setText(fecha.format(formatoDiaDerecha));

        List<ActividadCalendario> eventos = obtenerEventosFiltrados(fecha);
        lblContadorActividades.setText(eventos.size() == 1 ? "1 actividad" : eventos.size() + " actividades");

        contenedorEventosDia.getChildren().clear();

        if (eventos.isEmpty()) {
            Label lblVacio = new Label("No tienes actividades programadas para este día.");
            lblVacio.setWrapText(true);
            lblVacio.getStyleClass().add("cal-texto-secundario");
            contenedorEventosDia.getChildren().add(lblVacio);
            return;
        }

        for (ActividadCalendario ev : eventos) {
            VBox tarjeta = new VBox(6.0);
            tarjeta.setCursor(Cursor.HAND);
            tarjeta.getStyleClass().add("cal-tarjeta-evento");
            tarjeta.setOnMouseClicked(e -> abrirDetalleActividad(ev.idActividad()));

            HBox filaSuperior = new HBox(8.0);
            filaSuperior.setAlignment(Pos.CENTER_LEFT);

            Label badgeCat = new Label(ev.categoria());
            badgeCat.getStyleClass().addAll("cal-evento-pill", obtenerClaseCategoria(ev.categoria()));

            Label lblHora = new Label(ev.hora());
            lblHora.getStyleClass().add("cal-texto-secundario");

            filaSuperior.getChildren().addAll(badgeCat, lblHora);

            Label lblTitulo = new Label(ev.titulo());
            lblTitulo.setWrapText(true);
            lblTitulo.getStyleClass().add("cal-titulo-evento");

            Label lblLugar = new Label("📍 " + ev.ubicacion());
            lblLugar.setWrapText(true);
            lblLugar.getStyleClass().add("cal-texto-secundario");

            tarjeta.getChildren().addAll(filaSuperior, lblTitulo, lblLugar);
            contenedorEventosDia.getChildren().add(tarjeta);
        }
    }

    // Busca la actividad completa en MySQL y abre su pantalla de detalle
    private void abrirDetalleActividad(int idActividad) {
        try {
            Actividad actividad = actividadDAO.buscarPorId(idActividad);
            if (actividad == null) {
                return;
            }

            Navegador.irA(Vista.DETALLE_ACTIVIDAD, (DetalleActividadController c) -> {
                c.setActividad(actividad, () -> Navegador.irA(Vista.CALENDARIO));
            });

        } catch (SQLException e) {
            // Antes
            Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo cargar el detalle de la actividad.");
            alert.setHeaderText(null);
            alert.showAndWait();

            // Después
            AlertUtil.error("No se pudo cargar el detalle de la actividad.");
        }
    }

    // Filtra las actividades de una fecha según los ToggleButton activos
    private List<ActividadCalendario> obtenerEventosFiltrados(LocalDate fecha) {
        List<ActividadCalendario> lista = actividadesPorFecha.getOrDefault(fecha, Collections.emptyList());
        return lista.stream().filter(ev -> {
            String cat = ev.categoria().toLowerCase(localeEs);
            if (cat.contains("deporte")) return tglDeporte.isSelected();
            if (cat.contains("arte")) return tglArte.isSelected();
            if (cat.contains("cultura")) return tglCultura.isSelected();
            if (cat.contains("comunidad")) return tglComunidad.isSelected();
            return true;
        }).toList();
    }

    // Devuelve la clase de color de calendario.css según la categoría
    private String obtenerClaseCategoria(String categoria) {
        String cat = categoria.toLowerCase(localeEs);
        if (cat.contains("deporte")) return "cal-cat-deporte";
        if (cat.contains("arte")) return "cal-cat-arte";
        if (cat.contains("cultura")) return "cal-cat-cultura";
        return "cal-cat-comunidad";
    }

    // Estructura interna para guardar los datos de cada actividad en el calendario
    private record ActividadCalendario(
            int idActividad,
            String titulo,
            LocalDate fecha,
            String hora,
            String ubicacion,
            String categoria
    ) {}
}