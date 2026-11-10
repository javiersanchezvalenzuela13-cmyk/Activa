package org.example.activa.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class PruebaConexion {

    public static void main(String[] args) {
        try (Connection con = ConexionBD.getConexion()) {
            DatabaseMetaData meta = con.getMetaData();
            System.out.println("Conexión correcta");
            System.out.println("  Servidor: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            System.out.println("  URL:      " + meta.getURL());
            System.out.println("  Usuario:  " + meta.getUserName());
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            System.exit(1);
        }
    }
}
