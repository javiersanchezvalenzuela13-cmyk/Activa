package org.example.activa.service;

import org.example.activa.dao.UsuarioDAO;
import org.example.activa.exception.AuthException;
import org.example.activa.model.Usuario;

public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // Valida las credenciales y devuelve el objeto Usuario completo
    public Usuario login(String email, String password) throws AuthException {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new AuthException("Por favor, introduce tu correo y contraseña.");
        }

        Usuario usuario = usuarioDAO.obtenerUsuarioPorCredenciales(email.trim(), password);
        if (usuario == null) {
            throw new AuthException("El correo o la contraseña son incorrectos.");
        }
        return usuario;
    }

    // Valida los datos de registro, crea el usuario en la BD y lo devuelve
    public Usuario registrar(String nombre, String email, String password) throws AuthException {
        if (nombre == null || nombre.trim().isEmpty() || email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new AuthException("Rellena todos los campos para continuar.");
        }

        if (!email.contains("@") || !email.contains(".")) {
            throw new AuthException("El formato del correo electrónico no es válido.");
        }

        if (password.length() < 8 || !password.matches(".*\\d.*")) {
            throw new AuthException("La contraseña debe tener al menos 8 caracteres y un número.");
        }

        if (usuarioDAO.existeEmail(email.trim())) {
            throw new AuthException("Este correo electrónico ya está registrado.");
        }

        Usuario nuevoUsuario = new Usuario(nombre.trim(), email.trim(), password);
        Usuario usuarioCreado = usuarioDAO.registrarUsuario(nuevoUsuario);

        if (usuarioCreado == null) {
            throw new AuthException("Hubo un problema al crear la cuenta. Inténtalo más tarde.");
        }
        return usuarioCreado;
    }

    // Gestiona el acceso o creación automática de cuenta mediante Google
    public Usuario loginConGoogle(String emailGoogle) throws AuthException {
        if (emailGoogle == null || emailGoogle.trim().isEmpty()) {
            throw new AuthException("Debes introducir un correo válido.");
        }

        Usuario usuario = usuarioDAO.obtenerUsuarioPorEmail(emailGoogle.trim());
        if (usuario == null) {
            Usuario nuevo = new Usuario(emailGoogle.split("@")[0], emailGoogle.trim(), "GooglePass123!");
            usuario = usuarioDAO.registrarUsuario(nuevo);
        }

        if (usuario == null) {
            throw new AuthException("No se pudo iniciar sesión con Google.");
        }
        return usuario;
    }
}