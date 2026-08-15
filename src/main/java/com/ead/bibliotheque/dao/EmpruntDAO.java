package com.ead.bibliotheque.dao;

import com.ead.bibliotheque.models.Emprunt;
import com.ead.bibliotheque.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO Emprunt — RG-01, RG-02, RG-03 (CDC sections 8.3 et 9.2).
 * Requêtes exclusivement en PreparedStatement.
 *
 * Schéma attendu :
 *   emprunts(id_emprunt SERIAL PK,
 *            id_adherent INT FK,
 *            id_livre    INT FK,
 *            date_emprunt        DATE NOT NULL,
 *            date_retour_prevue  DATE NOT NULL,
 *            date_retour_effectif DATE,         -- NULL = non rendu
 *            statut VARCHAR(10) DEFAULT 'EN_COURS')
 */
public class EmpruntDAO {

    // ── Lecture ──────────────────────────────────────────────

    /** Tous les emprunts EN_COURS (onglet "En cours"). */
    public List<Emprunt> listerEnCours() {
        return listerParStatut("EN_COURS");
    }

    /** Tous les emprunts RETARD (onglet "Retards"). */
    public List<Emprunt> listerRetards() {
        return listerParStatut("RETARD");
    }

    /** Historique : emprunts RENDUS. */
    public List<Emprunt> listerHistorique() {
        return listerParStatut("RENDU");
    }

    private List<Emprunt> listerParStatut(String statut) {
        String sql = """
            SELECT e.id_emprunt, e.id_adherent, e.id_livre,
                   a.nom || ' ' || a.prenom AS nom_adherent,
                   a.num_carte,
                   l.titre,
                   e.date_emprunt, e.date_retour_prevue, e.date_retour_effectif, e.statut,
                   e.cree_par
            FROM emprunts e
            JOIN adherents a ON a.id_adherent = e.id_adherent
            JOIN livres    l ON l.id_livre    = e.id_livre
            WHERE e.statut = ?
            ORDER BY e.date_retour_prevue ASC
            """;
        return executer(sql, statut);
    }

    /** Tous les emprunts actifs d'un adhérent (pour le panneau latéral). */
    public List<Emprunt> listerParAdherent(int idAdherent) {
        String sql = """
            SELECT e.id_emprunt, e.id_adherent, e.id_livre,
                   a.nom || ' ' || a.prenom AS nom_adherent,
                   a.num_carte,
                   l.titre,
                   e.date_emprunt, e.date_retour_prevue, e.date_retour_effectif, e.statut,
                   e.cree_par
            FROM emprunts e
            JOIN adherents a ON a.id_adherent = e.id_adherent
            JOIN livres    l ON l.id_livre    = e.id_livre
            WHERE e.id_adherent = ?
            ORDER BY e.date_emprunt DESC
            """;
        return executer(sql, idAdherent);
    }

    private List<Emprunt> executer(String sql, Object param) {
        List<Emprunt> liste = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (param instanceof String s) stmt.setString(1, s);
            else if (param instanceof Integer i) stmt.setInt(1, i);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) liste.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur EmpruntDAO.executer : " + e.getMessage());
        }
        return liste;
    }

    // ── Vérifications RG ─────────────────────────────────────

    /**
     * RG-03 : nombre d'emprunts EN_COURS ou RETARD pour cet adhérent.
     * Le contrôleur vérifie que ce nombre < 3 avant d'autoriser le nouvel emprunt.
     */
    public int compterEmpruntsActifs(int idAdherent) {
        String sql = "SELECT COUNT(*) FROM emprunts WHERE id_adherent = ? AND statut IN ('EN_COURS','RETARD')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAdherent);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur compterEmpruntsActifs : " + e.getMessage());
        }
        return 0;
    }

    /**
     * RG-01 : retourne le nombre d'exemplaires disponibles pour un livre.
     * Le contrôleur vérifie que ce nombre > 0.
     */
    public int getExemplairesDisponibles(int idLivre) {
        String sql = "SELECT exemplaires_disponibles FROM livres WHERE id_livre = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idLivre);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur getExemplairesDisponibles : " + e.getMessage());
        }
        return 0;
    }

    /** Vérifie si l'adhérent a un retard en cours (bloque l'emprunt selon la politique choisie). */
    public boolean aUnRetard(int idAdherent) {
        String sql = "SELECT COUNT(*) FROM emprunts WHERE id_adherent = ? AND statut = 'RETARD'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAdherent);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erreur aUnRetard : " + e.getMessage());
        }
        return false;
    }

    // ── Écriture ─────────────────────────────────────────────

    /**
     * Crée un emprunt (RG-02 : date_retour_prevue = date_emprunt + 14 jours, calculée ici).
     * Décrémente aussi exemplaires_disponibles dans la table livres (RG-01).
     */
    public boolean enregistrerEmprunt(int idAdherent, int idLivre) {
        LocalDate dateEmprunt      = LocalDate.now();
        LocalDate dateRetourPrevue = dateEmprunt.plusDays(14); // RG-02

        String sqlEmprunt = """
    INSERT INTO emprunts (id_adherent, id_livre, date_emprunt, date_retour_prevue, statut, cree_par)
    VALUES (?, ?, ?, ?, 'EN_COURS', ?)
    """;
        String sqlDispo = "UPDATE livres SET exemplaires_disponibles = exemplaires_disponibles - 1 WHERE id_livre = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement s1 = conn.prepareStatement(sqlEmprunt);
                 PreparedStatement s2 = conn.prepareStatement(sqlDispo)) {

                s1.setInt(1, idAdherent);
                s1.setInt(2, idLivre);
                s1.setDate(3, Date.valueOf(dateEmprunt));
                s1.setDate(4, Date.valueOf(dateRetourPrevue));
                String login = com.ead.bibliotheque.util.SessionManager.getAdministrateurConnecte() != null
                ? com.ead.bibliotheque.util.SessionManager.getAdministrateurConnecte().getLogin() : "système";
                s1.setString(5, login);
                s1.executeUpdate();

                s2.setInt(1, idLivre);
                s2.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Erreur enregistrerEmprunt (rollback) : " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Erreur connexion EmpruntDAO : " + e.getMessage());
        }
        return false;
    }

    /**
     * Enregistre le retour d'un livre :
     * - date_retour_effectif = aujourd'hui
     * - statut → RENDU
     * - incrémente exemplaires_disponibles
     */
    public boolean enregistrerRetour(int idEmprunt, int idLivre) {
        String sqlRetour = """
            UPDATE emprunts SET date_retour_effectif = ?, statut = 'RENDU'
            WHERE id_emprunt = ?
            """;
        String sqlDispo = "UPDATE livres SET exemplaires_disponibles = exemplaires_disponibles + 1 WHERE id_livre = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement s1 = conn.prepareStatement(sqlRetour);
                 PreparedStatement s2 = conn.prepareStatement(sqlDispo)) {

                s1.setDate(1, Date.valueOf(LocalDate.now()));
                s1.setInt(2, idEmprunt);
                s1.executeUpdate();

                s2.setInt(1, idLivre);
                s2.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Erreur enregistrerRetour (rollback) : " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Erreur connexion EmpruntDAO : " + e.getMessage());
        }
        return false;
    }

    /**
     * Met à jour le statut des emprunts dont la date_retour_prevue est dépassée
     * (à appeler au démarrage ou périodiquement).
     */
    public void synchroniserStatutsRetard() {
        String sql = """
            UPDATE emprunts SET statut = 'RETARD'
            WHERE statut = 'EN_COURS' AND date_retour_prevue < CURRENT_DATE
            """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur synchroniserStatutsRetard : " + e.getMessage());
        }
    }
    public List<Emprunt> listerActiviteRecente(int limite) {
        String sql = """
        SELECT e.id_emprunt, e.id_adherent, e.id_livre,
               a.nom || ' ' || a.prenom AS nom_adherent,
               a.num_carte,
               l.titre,
               e.date_emprunt, e.date_retour_prevue, e.date_retour_effectif, e.statut,
               e.cree_par
        FROM emprunts e
        JOIN adherents a ON a.id_adherent = e.id_adherent
        JOIN livres    l ON l.id_livre    = e.id_livre
        ORDER BY e.date_emprunt DESC, e.id_emprunt DESC
        LIMIT ?
        """;
        List<Emprunt> liste = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, limite);
            try (ResultSet rs = s.executeQuery()) {
                while (rs.next()) liste.add(map(rs));
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return liste;
    }

    public int compterNouveauxEmpruntsMois() {
        String sql = "SELECT COUNT(*) FROM emprunts WHERE date_emprunt >= date_trunc('month', CURRENT_DATE)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 0;
    }

    public int compterRetoursMois() {
        String sql = "SELECT COUNT(*) FROM emprunts WHERE statut = 'RENDU' AND date_retour_effectif >= date_trunc('month', CURRENT_DATE)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 0;
    }

    public boolean livreADesEmprunts(int idLivre) {
        String sql = "SELECT COUNT(*) FROM emprunts WHERE id_livre = ? AND statut IN ('EN_COURS','RETARD')";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, idLivre);
            try (ResultSet rs = s.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return false;
    }

    // ── Mapper ───────────────────────────────────────────────

    private Emprunt map(ResultSet rs) throws SQLException {
        Date retourEffectifSql = rs.getDate("date_retour_effectif");
        Emprunt e = new Emprunt(
                rs.getInt("id_emprunt"),
                rs.getInt("id_adherent"),
                rs.getInt("id_livre"),
                rs.getString("nom_adherent"),
                rs.getString("num_carte"),
                rs.getString("titre"),
                rs.getDate("date_emprunt").toLocalDate(),
                rs.getDate("date_retour_prevue").toLocalDate(),
                retourEffectifSql != null ? retourEffectifSql.toLocalDate() : null,
                Emprunt.Statut.valueOf(rs.getString("statut"))
        );
        e.setCreePar(rs.getString("cree_par"));
        return e;
    }

    public int compterARendreCetteSemaine() {
    String sql = """
        SELECT COUNT(*) FROM emprunts
        WHERE statut = 'EN_COURS'
        AND date_retour_prevue BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'
        """;
    try (Connection c = DatabaseConnection.getConnection();
         PreparedStatement s = c.prepareStatement(sql);
         ResultSet rs = s.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) { System.err.println(e.getMessage()); }
    return 0;
}

public int compterTotalLivresEmpruntes() {
    String sql = "SELECT COUNT(*) FROM emprunts WHERE statut IN ('EN_COURS', 'RETARD')";
    try (Connection c = DatabaseConnection.getConnection();
         PreparedStatement s = c.prepareStatement(sql);
         ResultSet rs = s.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) { System.err.println(e.getMessage()); }
    return 0;
}

public int compterTotalEmprunts() {
    String sql = "SELECT COUNT(*) FROM emprunts";
    try (Connection c = DatabaseConnection.getConnection();
         PreparedStatement s = c.prepareStatement(sql);
         ResultSet rs = s.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) { System.err.println(e.getMessage()); }
    return 0;
}

public int compterTotalRetours() {
    String sql = "SELECT COUNT(*) FROM emprunts WHERE statut = 'RENDU'";
    try (Connection c = DatabaseConnection.getConnection();
         PreparedStatement s = c.prepareStatement(sql);
         ResultSet rs = s.executeQuery()) {
        if (rs.next()) return rs.getInt(1);
    } catch (SQLException e) { System.err.println(e.getMessage()); }
    return 0;
}

public java.util.Map<String, Integer> compterEmpruntsParGenre() {
    String sql = """
        SELECT l.genre, COUNT(*) AS nb
        FROM emprunts e
        JOIN livres l ON l.id_livre = e.id_livre
        GROUP BY l.genre
        ORDER BY nb DESC
        LIMIT 3
        """;
    java.util.Map<String, Integer> res = new java.util.LinkedHashMap<>();
    try (Connection c = DatabaseConnection.getConnection();
         PreparedStatement s = c.prepareStatement(sql);
         ResultSet rs = s.executeQuery()) {
        while (rs.next()) res.put(rs.getString("genre"), rs.getInt("nb"));
    } catch (SQLException e) { System.err.println(e.getMessage()); }
    return res;
}

}
