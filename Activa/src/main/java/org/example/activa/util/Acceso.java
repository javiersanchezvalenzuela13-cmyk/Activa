package org.example.activa.util;

public final class Acceso {

    private Acceso() {
    }

    // Avisa al invitado y le ofrece ir al login
    public static void pedirLogin(String accion) {
        boolean irAlLogin = AlertUtil.confirmar(
                "Inicia sesión para " + accion,
                "Necesitas una cuenta para " + accion + ". Es gratis y solo tardas un minuto.",
                "Iniciar sesión", "Seguir mirando");
        if (irAlLogin) {
            Navegador.mostrarPantallaCompleta(Vista.LOGIN);
        }
    }
}