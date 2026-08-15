package com.ead.bibliotheque.dao;

import com.ead.bibliotheque.models.Administrateur;
import com.ead.bibliotheque.util.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AdministrateurDAO {

    static {
        String sql = "ALTER TABLE administrateurs ADD COLUMN IF NOT EXISTS cree_le TIMESTAMP DEFAULT NOW()";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.execute();
        } catch (SQLException e) {
            System.err.println("Migration cree_le: " + e.getMessage());
        }
    }

    public Optional<Administrateur> authentifier(String login, String motDePasseSaisi) {
        String sql = "SELECT id_admin, login, mot_de_passe, nom, prenom, cree_le FROM administrateurs WHERE login = ?";
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
                        java.sql.Timestamp ts = rs.getTimestamp("cree_le");
                        if (ts != null) admin.setCreeLe(ts.toLocalDateTime());
                        return Optional.of(admin);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'authentification : " + e.getMessage());
        }
        return Optional.empty();
    }

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

    public boolean reinitialiserMotDePasse(String login, String nouveauMotDePasse) {
        String sql = "UPDATE administrateurs SET mot_de_passe = ? WHERE login = ?";
        String hash = BCrypt.hashpw(nouveauMotDePasse, BCrypt.gensalt());
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hash);
            stmt.setString(2, login);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur reinitialiserMotDePasse : " + e.getMessage());
            return false;
        }
    }

    public boolean changerMotDePasse(int idAdmin, String ancienMdpClair, String nouveauMdpClair) {
        String sqlSelect = "SELECT mot_de_passe FROM administrateurs WHERE id_admin = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sqlSelect)) {
            s.setInt(1, idAdmin);
            try (ResultSet rs = s.executeQuery()) {
                if (!rs.next()) return false;
                if (!BCrypt.checkpw(ancienMdpClair, rs.getString("mot_de_passe"))) return false;
            }
        } catch (SQLException e) {
            System.err.println("Erreur changerMotDePasse (vérification) : " + e.getMessage());
            return false;
        }
        String sqlUpdate = "UPDATE administrateurs SET mot_de_passe = ? WHERE id_admin = ?";
        String nouveauHash = BCrypt.hashpw(nouveauMdpClair, BCrypt.gensalt());
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sqlUpdate)) {
            s.setString(1, nouveauHash);
            s.setInt(2, idAdmin);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur changerMotDePasse (mise à jour) : " + e.getMessage());
            return false;
        }
    }

    public boolean mettreAJourLogin(int idAdmin, String nouveauLogin) {
        String sql = "UPDATE administrateurs SET login = ? WHERE id_admin = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, nouveauLogin);
            s.setInt(2, idAdmin);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur mettreAJourLogin : " + e.getMessage());
            return false;
        }
    }

    public List<Administrateur> listerTous() {
        String sql = "SELECT id_admin, login, mot_de_passe, nom, prenom, cree_le FROM administrateurs ORDER BY login";
        List<Administrateur> res = new java.util.ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            while (rs.next()) {
                Administrateur a = new Administrateur(
                        rs.getInt("id_admin"), rs.getString("login"),
                        rs.getString("mot_de_passe"), rs.getString("nom"), rs.getString("prenom"));
                java.sql.Timestamp ts = rs.getTimestamp("cree_le");
                if (ts != null) a.setCreeLe(ts.toLocalDateTime());
                res.add(a);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return res;
    }

    public boolean supprimerAdministrateur(int idAdmin) {
        String sql = "DELETE FROM administrateurs WHERE id_admin = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, idAdmin);
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }
}
