package org.example.activa.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class ConexionBD {

    private static final Path ARCHIVO_CONFIG = Path.of("db.properties");

    private ConexionBD() {
    }

    public static Connection getConexion() throws SQLException {
        Properties config = cargarConfiguracion();
        String url = config.getProperty("db.url");
        String usuario = config.getProperty("db.user");
        String password = config.getProperty("db.password", "");

        if (url == null || usuario == null) {
            throw new SQLException("Faltan db.url o db.user en " + ARCHIVO_CONFIG.toAbsolutePath());
        }
        DriverManager.setLoginTimeout(5);
        return DriverManager.getConnection(url, usuario, password);
    }

    private static Properties cargarConfiguracion() throws SQLException {
        if (!Files.exists(ARCHIVO_CONFIG)) {
            throw new SQLException("No se encuentra " + ARCHIVO_CONFIG.toAbsolutePath() + ". Copia db.properties.example como db.properties y rellena tus datos.");
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(ARCHIVO_CONFIG)) {
            props.load(in);
        } catch (IOException e) {
            throw new SQLException("No se pudo leer " + ARCHIVO_CONFIG.toAbsolutePath(), e);
        }
        return props;
    }
}
