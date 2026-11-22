package org.example.activa.util;

import org.example.activa.dao.UsuarioDAO;
import org.example.activa.model.Usuario;
import java.util.prefs.Preferences;

public class Sesion {

    private static int idUsuario = -1;
    private static String nombreUsuario;
    private static String email;
    private static boolean esAdmin;

    // Nodo de preferencias del sistema para guardar datos persistentes de esta clase.
    private static final Preferences prefs = Preferences.userNodeForPackage(Sesion.class);
    private static final String PREF_ID_USUARIO = "activa_usuario_id";

    // Guarda los datos del usuario en la memoria del programa durante la ejecución.
    public static void iniciar(int id, String nombre, String correo, boolean admin) {
        idUsuario = id;
        nombreUsuario = nombre;
        email = correo;
        esAdmin = admin;
    }

    // Busca un ID guardado en Windows/Mac, consulta la BD y autocompleta la sesión.
    public static boolean comprobarSesionGuardada() {
        int idGuardado = prefs.getInt(PREF_ID_USUARIO, -1);
        if (idGuardado != -1) {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            Usuario usuario = usuarioDAO.obtenerUsuarioPorId(idGuardado);

            if (usuario != null) {
                iniciar(usuario.getIdUsuario(), usuario.getNombreUsuario(), usuario.getEmail(), usuario.esAdmin());
                return true;
            }
        }
        return false;
    }

    // Guarda el ID del usuario en el sistema operativo para recordarlo al reabrir.
    public static void guardarSesionRecordada(int id) {
        prefs.putInt(PREF_ID_USUARIO, id);
    }

    // Borra el rastro guardado en el sistema operativo para que no entre solo.
    public static void limpiarSesionRecordada() {
        prefs.remove(PREF_ID_USUARIO);
    }

    // Devuelve el ID del usuario logueado actualmente.
    public static int getIdUsuario() {
        return idUsuario;
    }

    // Devuelve el nombre del usuario logueado actualmente.
    public static String getNombreUsuario() {
        return nombreUsuario;
    }

    // Devuelve el correo del usuario logueado actualmente.
    public static String getEmail() {
        return email;
    }

    // Indica si el usuario actual tiene rol de administrador.
    public static boolean isEsAdmin() {
        return esAdmin;
    }

    // Comprueba si hay alguien logueado revisando si el ID es válido.
    public static boolean haySesion() {
        return idUsuario != -1;
    }

    // Borra todas las variables de memoria al cerrar sesión manualmente.
    public static void cerrarSesion() {
        idUsuario = -1;
        nombreUsuario = null;
        email = null;
        esAdmin = false;
    }
}