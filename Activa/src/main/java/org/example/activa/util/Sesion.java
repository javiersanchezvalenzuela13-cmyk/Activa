package org.example.activa.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class Sesion {

    // Datos del usuario conectado (null si no hay nadie)
    private static Integer idUsuario;
    private static String nombre;
    private static String email;
    private static boolean admin;
    // Foto de perfil del usuario conectado (avisa cuando cambia)
    private static final ObjectProperty<byte[]> foto = new SimpleObjectProperty<>();

    private Sesion() {
    }

    // Lo llama el login al entrar
    public static void iniciar(int idUsuario, String nombre, String email, boolean admin) {
        Sesion.idUsuario = idUsuario;
        Sesion.nombre = nombre;
        Sesion.email = email;
        Sesion.admin = admin;
    }

    // Lo llama "Cerrar sesión"
    public static void cerrar() {
        idUsuario = null;
        nombre = null;
        email = null;
        admin = false;
        foto.set(null);
    }

    public static boolean haySesion() {
        return idUsuario != null;
    }

    public static int getIdUsuario() {
        if (idUsuario == null) {
            throw new IllegalStateException("No hay ninguna sesión iniciada.");
        }
        return idUsuario;
    }

    public static String getNombre() {
        return nombre;
    }

    public static String getEmail() {
        return email;
    }

    public static boolean isAdmin() {
        return admin;
    }

    // Iniciales para el avatar ("Laura Méndez" → "LM")
    public static String getIniciales() {
        if (nombre == null || nombre.isBlank()) {
            return "?";
        }
        String[] partes = nombre.trim().split("\\s+");
        String iniciales = partes[0].substring(0, 1);
        if (partes.length > 1) {
            iniciales += partes[1].substring(0, 1);
        }
        return iniciales.toUpperCase();
    }
    public static byte[] getFoto() {
        return foto.get();
    }

    public static void setFoto(byte[] nuevaFoto) {
        foto.set(nuevaFoto);
    }

    public static ObjectProperty<byte[]> fotoProperty() {
        return foto;
    }
}