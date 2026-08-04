package com.ead.bibliotheque.dao;

import com.ead.bibliotheque.models.Administrateur;
import com.ead.bibliotheque.util.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * DAO pour l'authentification de l'administrateur (RG-06, section 9.3 du CDC).
 * Utilise exclusivement des requêtes préparées (protection contre les injections SQL).
 */
public class AdministrateurDAO {

    /**
     * Vérifie les identifiants saisis sur l'écran de connexion.
     * Retourne l'administrateur si le login existe et que le mot de passe correspond au hash BCrypt stocké.
     */
    public Optional<Administrateur> authentifier(String login, String motDePasseSaisi) {
        String sql = "SELECT id_admin, login, mot_de_passe, nom, prenom FROM administrateurs WHERE login = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hashStocke = rs.getString("mot_de_passe");
                    if (BCrypt.checkpw(motDePasseSaisi, hashStocke)) {
                        Administrateur admin = new Administrateur(
                                rs.getInt("id_admin"),
                                rs.getString("login"),
                                hashStocke,
                                rs.getString("nom"),
                                rs.getString("prenom")
                        );
                        return Optional.of(admin);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'authentification : " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Crée un administrateur avec mot de passe haché (utile pour initialiser le compte,
     * par ex. via un petit script à part, pas depuis l'IHM).
     */
    public boolean creerAdministrateur(String login, String motDePasseClair, String nom, String prenom) {
        String sql = "INSERT INTO administrateurs (login, mot_de_passe, nom, prenom) VALUES (?, ?, ?, ?)";
        String hash = BCrypt.hashpw(motDePasseClair, BCrypt.gensalt());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, hash);
            stmt.setString(3, nom);
            stmt.setString(4, prenom);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de l'administrateur : " + e.getMessage());
            return false;
        }
    }
}
