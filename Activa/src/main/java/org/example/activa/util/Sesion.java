package org.example.activa.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class Sesion {

    // Datos del usuario conectado (null si no hay nadie)
    private static Integer idUsuario;
    private static String nombre;
    private static String email;
    private static boolean admin;

    // Foto de perfil del usuario conectado (avisa a la interfaz cuando cambia)
    private static final ObjectProperty<byte[]> foto = new SimpleObjectProperty<>();

    private Sesion() {
    }

    // Guarda los datos del usuario en la memoria del programa al iniciar
    public static void iniciar(int id, String nom, String correo, boolean esAdmin) {
        idUsuario = id;
        nombre = nom;
        email = correo;
        admin = esAdmin;
    }

    // Limpia la memoria al cerrar sesión
    public static void cerrar() {
        idUsuario = null;
        nombre = null;
        email = null;
        admin = false;
        foto.set(null);
    }

    // Comprueba si hay alguien logueado revisando si el ID no es nulo
    public static boolean haySesion() {
        return idUsuario != null;
    }

    // Devuelve el ID del usuario actual o lanza error si no hay sesión
    public static int getIdUsuario() {
        if (idUsuario == null) {
            throw new IllegalStateException("No hay ninguna sesión iniciada.");
        }
        return idUsuario;
    }

    // Devuelve el nombre del usuario
    public static String getNombre() {
        return nombre;
    }

    // Devuelve el email del usuario
    public static String getEmail() {
        return email;
    }

    // Indica si el usuario actual es administrador
    public static boolean isAdmin() {
        return admin;
    }

    // Genera iniciales para el avatar (de "Laura Méndez" sale "LM")
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

    // Devuelve la foto en bytes
    public static byte[] getFoto() {
        return foto.get();
    }

    // Actualiza la foto en memoria
    public static void setFoto(byte[] nuevaFoto) {
        foto.set(nuevaFoto);
    }

    // Carga la foto desde la ruta guardada en la BD
    public static void cargarFoto(String rutaUri) {
        foto.set(Imagenes.leerArchivo(rutaUri));
    }

    // Propiedad observable para enlazar la foto en JavaFX
    public static ObjectProperty<byte[]> fotoProperty() {
        return foto;
    }
}