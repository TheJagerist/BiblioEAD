package com.ead.bibliotheque.dao;

import com.ead.bibliotheque.models.Adherent;
import com.ead.bibliotheque.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des adhérents (section 8.2 du CDC).
 * Toutes les requêtes utilisent PreparedStatement (section 9.3 - sécurité).
 */
public class AdherentDAO {

    /** Liste complète des adhérents, triée par nom par défaut. */
    public List<Adherent> listerTous() {
        return listerTous("nom", true);
    }

    /**
     * Liste les adhérents avec un tri paramétrable (bouton "Trier par" de la maquette).
     * @param colonne colonne de tri : nom, prenom, classe, num_carte, date_inscription
     * @param ascendant sens du tri
     */
    public List<Adherent> listerTous(String colonne, boolean ascendant) {
        List<String> colonnesAutorisees = List.of("nom", "prenom", "classe", "num_carte", "date_inscription");
        String colonneSure = colonnesAutorisees.contains(colonne) ? colonne : "nom";
        String sens = ascendant ? "ASC" : "DESC";

        // colonneSure vient d'une liste blanche fixe, donc pas d'injection possible ici
        String sql = "SELECT id_adherent, nom, prenom, classe, filiere, num_carte, date_inscription " +
                "FROM adherents ORDER BY " + colonneSure + " " + sens;

        List<Adherent> resultats = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                resultats.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la liste des adhérents : " + e.getMessage());
        }
        return resultats;
    }

    /** Recherche par nom, prénom ou numéro de carte (champ "Rechercher" de la maquette). */
    public List<Adherent> rechercher(String motCle) {
        String sql = "SELECT id_adherent, nom, prenom, classe, filiere, num_carte, date_inscription " +
                     "FROM adherents " +
                     "WHERE nom ILIKE ? OR prenom ILIKE ? OR num_carte ILIKE ? " +
                     "ORDER BY nom ASC";

        List<Adherent> resultats = new ArrayList<>();
        String motif = "%" + motCle + "%";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, motif);
            stmt.setString(2, motif);
            stmt.setString(3, motif);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche : " + e.getMessage());
        }
        return resultats;
    }

    /** Filtre par classe (menu "Filtrer par" de la maquette). */
    public List<Adherent> filtrerParClasse(String classe) {
        String sql = "SELECT id_adherent, nom, prenom, classe, filiere, num_carte, date_inscription " +
                "FROM adherents WHERE classe = ? ORDER BY nom ASC";

        List<Adherent> resultats = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, classe);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du filtrage : " + e.getMessage());
        }
        return resultats;
    }

    /** Ajoute un adhérent ; le numéro de carte est généré automatiquement (section 8.2 du CDC). */
    public boolean ajouter(Adherent adherent) {
        String numCarte = genererProchainNumCarte();
        String sql = "INSERT INTO adherents (nom, prenom, classe, filiere, num_carte, date_inscription) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, adherent.getNom());
            stmt.setString(2, adherent.getPrenom());
            stmt.setString(3, adherent.getClasse());
            stmt.setString(4, adherent.getFiliere());
            stmt.setString(5, numCarte);
            stmt.setDate(6, Date.valueOf(
                    adherent.getDateInscription() != null ? adherent.getDateInscription() : LocalDate.now()));

            int lignes = stmt.executeUpdate();
            if (lignes > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        adherent.setIdAdherent(keys.getInt(1));
                    }
                }
                adherent.setNumCarte(numCarte);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout de l'adhérent : " + e.getMessage());
        }
        return false;
    }

    /** Modifie un adhérent existant. */
    public boolean modifier(Adherent adherent) {
        String sql = "UPDATE adherents SET nom = ?, prenom = ?, classe = ? WHERE id_adherent = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, adherent.getNom());
            stmt.setString(2, adherent.getPrenom());
            stmt.setString(3, adherent.getClasse());
            stmt.setInt(4, adherent.getIdAdherent());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification de l'adhérent : " + e.getMessage());
            return false;
        }
    }

    /** Supprime un adhérent par son identifiant. */
    public boolean supprimer(int idAdherent) {
        String sql = "DELETE FROM adherents WHERE id_adherent = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idAdherent);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de l'adhérent : " + e.getMessage());
            return false;
        }
    }

    /**
     * Génère un numéro de carte unique au format ADH-AAAA-XXXX
     * (ex. ADH-2026-0007), basé sur le compteur d'adhérents de l'année en cours.
     */
    public String genererProchainNumCarte() {
        int annee = Year.now().getValue();
        String sql = "SELECT COUNT(*) AS total FROM adherents WHERE num_carte LIKE ?";

        int compteur = 1;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "ADH-" + annee + "-%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    compteur = rs.getInt("total") + 1;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la génération du numéro de carte : " + e.getMessage());
        }
        return String.format("ADH-%d-%04d", annee, compteur);
    }
    public int compterNouveauxMois() {
        String sql = "SELECT COUNT(*) FROM adherents WHERE date_inscription >= date_trunc('month', CURRENT_DATE)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 0;
    }

    private Adherent mapResultSet(ResultSet rs) throws SQLException {
        return new Adherent(
                rs.getInt("id_adherent"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("classe"),
                rs.getString("filiere"),
                rs.getString("num_carte"),
                rs.getDate("date_inscription").toLocalDate()
        );
    }
}
