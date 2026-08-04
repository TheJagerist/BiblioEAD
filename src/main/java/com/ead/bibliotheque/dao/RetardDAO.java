package com.ead.bibliotheque.dao;

import com.ead.bibliotheque.models.Retard;
import com.ead.bibliotheque.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la vue Retards.
 * Les retards ne sont pas une table séparée : ce sont des emprunts (statut = 'RETARD')
 * enrichis par jointure avec adhérents et livres.
 */
public class RetardDAO {

    private static final String BASE_SQL = """
            SELECT e.id_emprunt, e.id_adherent, e.id_livre,
                   a.nom, a.prenom, a.num_carte, a.classe,
                   l.titre, l.auteur,
                   e.date_emprunt, e.date_retour_prevue
            FROM emprunts e
            JOIN adherents a ON a.id_adherent = e.id_adherent
            JOIN livres    l ON l.id_livre    = e.id_livre
            WHERE e.statut = 'RETARD'
            """;

    /** Tous les retards, triés par ancienneté (le plus ancien en premier). */
    public List<Retard> listerTous() {
        String sql = BASE_SQL + "ORDER BY e.date_retour_prevue ASC";
        return executer(sql);
    }

    /** Retards d'un adhérent précis (pour le panneau latéral "Fiche retard"). */
    public List<Retard> listerParAdherent(int idAdherent) {
        String sql = BASE_SQL + "AND e.id_adherent = ? ORDER BY e.date_retour_prevue ASC";
        List<Retard> resultats = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAdherent);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) resultats.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur RetardDAO.listerParAdherent : " + e.getMessage());
        }
        return resultats;
    }

    /** Nombre total de retards actifs (pour les KPI). */
    public int compterRetardsActifs() {
        String sql = "SELECT COUNT(*) FROM emprunts WHERE statut = 'RETARD'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur compterRetardsActifs : " + e.getMessage());
        }
        return 0;
    }

    /** Somme des jours de retard cumulés (KPI "Jours cumulés"). */
    public int joursRetardCumules() {
        String sql = "SELECT COALESCE(SUM(CURRENT_DATE - date_retour_prevue), 0) FROM emprunts WHERE statut = 'RETARD'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur joursRetardCumules : " + e.getMessage());
        }
        return 0;
    }

    /** Plus ancien retard en jours (KPI "Plus ancien retard"). */
    public int joursRetardMax() {
        String sql = "SELECT COALESCE(MAX(CURRENT_DATE - date_retour_prevue), 0) FROM emprunts WHERE statut = 'RETARD'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur joursRetardMax : " + e.getMessage());
        }
        return 0;
    }

    // ── Helpers ───────────────────────────────────────────────

    private List<Retard> executer(String sql) {
        List<Retard> resultats = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) resultats.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Erreur RetardDAO.executer : " + e.getMessage());
        }
        return resultats;
    }

    private Retard map(ResultSet rs) throws SQLException {
        return new Retard(
                rs.getInt("id_emprunt"),
                rs.getInt("id_adherent"),
                rs.getInt("id_livre"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("num_carte"),
                rs.getString("classe"),
                rs.getString("titre"),
                rs.getString("auteur"),
                rs.getDate("date_emprunt").toLocalDate(),
                rs.getDate("date_retour_prevue").toLocalDate()
        );
    }
}
