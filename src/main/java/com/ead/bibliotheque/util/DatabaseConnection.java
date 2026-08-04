package com.ead.bibliotheque.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gère la connexion unique (singleton) à la base de données PostgreSQL.
 * Adapter les constantes URL / USER / PASSWORD à votre configuration locale.
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:postgresql://localhost:5432/bibliotheque_ead";
    private static final String USER = "postgres";
    private static final String PASSWORD = "0090"; // TODO: à adapter à votre installation

    private static Connection connection;

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
        }
    }
}
