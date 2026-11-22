package org.example.activa.model;

import org.example.activa.model.enums.Rol;

import java.time.LocalDateTime;

public class Usuario {

    private int idUsuario;
    private String nombreUsuario;
    private String email;
    private String contrasena;
    private Rol rol;
    private LocalDateTime fechaRegistro;
    private String telefono;
    private String sobreMi;
    private String fotoPerfil;

    // Constructor vacío
    public Usuario() {
    }

    // Constructor para registro de nuevos usuarios
    public Usuario(String nombreUsuario, String email, String contrasena) {
        this.nombreUsuario = nombreUsuario;
        this.email = email;
        this.contrasena = contrasena;
        this.rol = Rol.USUARIO;
    }

    // Comprueba si el usuario tiene rol de administrador
    public boolean esAdmin() {
        return rol == Rol.ADMINISTRADOR;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getSobreMi() { return sobreMi; }
    public void setSobreMi(String sobreMi) { this.sobreMi = sobreMi; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    // Devuelve una representación en texto del usuario
    @Override
    public String toString() {
        return nombreUsuario + " (" + email + ")";
    }
}