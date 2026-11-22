package org.example.activa.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.example.activa.dao.UsuarioDAO;
import org.example.activa.model.Usuario;
import java.util.prefs.Preferences;

public final class Sesion {

    // Datos del usuario conectado (null si no hay nadie)
    private static Integer idUsuario;
    private static String nombre;
    private static String email;
    private static boolean admin;

    // Foto de perfil del usuario conectado (avisa a la interfaz cuando cambia)
    private static final ObjectProperty<byte[]> foto = new SimpleObjectProperty<>();

    // Nodo de preferencias del sistema para guardar el ID persistentemente
    private static final Preferences prefs = Preferences.userNodeForPackage(Sesion.class);
    private static final String PREF_ID_USUARIO = "activa_usuario_id";

    private Sesion() {
    }

    // Guarda los datos del usuario en la memoria del programa al iniciar
    public static void iniciar(int id, String nom, String correo, boolean esAdmin) {
        idUsuario = id;
        nombre = nom;
        email = correo;
        admin = esAdmin;
    }

    // Busca un ID guardado en Windows/Mac, consulta la BD y autocompleta la sesión
    public static boolean comprobarSesionGuardada() {
        int idGuardado = prefs.getInt(PREF_ID_USUARIO, -1);
        if (idGuardado != -1) {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario usuario = usuarioDAO.obtenerUsuarioPorId(idGuardado);

            if (usuario != null) {
                // Nota: Asegúrate de que los getters coinciden con tu modelo Usuario (ej. esAdmin() o isAdmin())
                iniciar(usuario.getIdUsuario(), usuario.getNombreUsuario(), usuario.getEmail(), usuario.esAdmin());
                return true;
            }
        }
        return false;
    }

    // Guarda el ID del usuario en el sistema operativo para recordarlo al reabrir
    public static void guardarSesionRecordada(int id) {
        prefs.putInt(PREF_ID_USUARIO, id);
    }

    // Borra el rastro guardado en el sistema operativo para que no entre solo
    public static void limpiarSesionRecordada() {
        prefs.remove(PREF_ID_USUARIO);
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

    // Genera iniciales para el avatar ("Laura Méndez" → "LM")
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

    // Propiedad observable para enlazar la foto en JavaFX
    public static ObjectProperty<byte[]> fotoProperty() {
        return foto;
    }
}