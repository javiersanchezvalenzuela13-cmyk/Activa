package org.example.activa.util;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

//Clase que agrupa los métodos con fechas
public class FechaUtil {

    //Estructura que usarán los métodos de abajo
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private FechaUtil() {
    }

    //Convierte los LOCAL_DATETIME en texto
    public static String formatearFechaHora(LocalDateTime fechaHora) {
        return fechaHora != null ? fechaHora.format(FORMATO_FECHA_HORA) : "";
    }
    public static String formatearFecha(LocalDate fecha) {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "";
    }
    public static String formatearHora(LocalDateTime fechaHora) {
        return fechaHora != null ? fechaHora.format(FORMATO_HORA) : "";
    }

    //Comprueba si una actividad ya ha pasado
    public static boolean esFechaPasada(LocalDateTime fechaHora) {
        return fechaHora != null && fechaHora.isBefore(LocalDateTime.now());
    }

    //Calcula si es primero de mes para la cuadrícula del calendario
    public static LocalDate inicioDeMes(int anio, int mes) {
        return YearMonth.of(anio, mes).atDay(1);
    }

    //Calcula si es fin de mes
    public static LocalDate finDeMes(int anio, int mes) {
        return YearMonth.of(anio, mes).atEndOfMonth();
    }
}