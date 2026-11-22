package org.example.activa.service;

import org.example.activa.dao.UsuarioDAO;
import org.example.activa.exception.AuthException;
import org.example.activa.model.Usuario;

public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    // Verifica las credenciales introducidas y devuelve la información del usuario si son correctas.
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

    // Valida el formato de los datos y registra un nuevo usuario en la base de datos si no existe.
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
}