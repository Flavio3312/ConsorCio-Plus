package com.consorcioplus.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton que gestiona la única conexión JDBC al servidor MySQL.
 *
 * <p>Uso:
 * <pre>
 *   Connection conn = DatabaseConnection.getInstance().getConnection();
 * </pre>
 *
 * <p>Principio aplicado: Single Responsibility — esta clase solo
 * gestiona el ciclo de vida de la conexión.
 */
public final class DatabaseConnection {

    // ── Configuración de conexión (ajustar según entorno local)
    private static final String URL      = "jdbc:mysql://localhost:3307/consorcioplus?useSSL=false&serverTimezone=America/Argentina/Buenos_Aires&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static DatabaseConnection instance;
    private Connection connection;

    /** Constructor privado: impide instanciación externa (patrón Singleton). */
    private DatabaseConnection() {}

    /**
     * Devuelve la instancia única de DatabaseConnection.
     * Thread-safe con sincronización lazy.
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Devuelve la conexión JDBC activa, creándola si no existe o si fue cerrada.
     *
     * @return Conexión JDBC válida
     * @throws SQLException si no puede establecer la conexión
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    /**
     * Cierra la conexión si está abierta.
     * Llamar al cerrar la aplicación.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    /**
     * Verifica si la conexión está activa.
     *
     * @return true si la conexión está establecida y no está cerrada
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
