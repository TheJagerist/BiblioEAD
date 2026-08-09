package com.ead.bibliotheque.dao;

import com.ead.bibliotheque.models.Livre;
import com.ead.bibliotheque.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LivreDAO {

    public List<Livre> listerTous() {
        String sql = "SELECT id_livre, titre, auteur, genre, annee, isbn, " +
                "nombre_exemplaires, exemplaires_disponibles, date_ajout, image_couverture " +
                "FROM livres ORDER BY titre ASC";
        List<Livre> res = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            while (rs.next()) res.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Erreur liste livres : " + e.getMessage());
        }
        return res;
    }

    public boolean ajouter(Livre l) {
        // Vérifier si un livre avec le même ISBN existe déjà
        String isbn = l.getIsbn();
        if (isbn != null && !isbn.trim().isEmpty()) {
            String sqlCheck = "SELECT id_livre, nombre_exemplaires, exemplaires_disponibles " +
                    "FROM livres WHERE isbn = ?";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement s = c.prepareStatement(sqlCheck)) {
                s.setString(1, isbn);
                try (ResultSet rs = s.executeQuery()) {
                    if (rs.next()) {
                        // ISBN déjà existant -> on incrémente au lieu de dupliquer
                        int idExistant = rs.getInt("id_livre");
                        int nbTotal = rs.getInt("nombre_exemplaires") + l.getNombreExemplaires();
                        int nbDispo = rs.getInt("exemplaires_disponibles") + l.getExemplairesDisponibles();
                        return incrementerExemplaires(idExistant, nbTotal, nbDispo, l);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erreur vérification ISBN : " + e.getMessage());
                return false;
            }
        }

        // Aucun doublon trouvé -> insertion normale
        String sql = "INSERT INTO livres (titre, auteur, genre, annee, isbn, " +
                "nombre_exemplaires, exemplaires_disponibles, image_couverture) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, l.getTitre());
            s.setString(2, l.getAuteur());
            s.setString(3, l.getGenre());
            if (l.getAnnee() != null) s.setInt(4, l.getAnnee()); else s.setNull(4, Types.INTEGER);
            s.setString(5, l.getIsbn());
            s.setInt(6, l.getNombreExemplaires());
            s.setInt(7, l.getExemplairesDisponibles());
            s.setString(8, l.getImageCouverture());
            int rows = s.executeUpdate();
            if (rows > 0) {
                try (ResultSet k = s.getGeneratedKeys()) {
                    if (k.next()) l.setIdLivre(k.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur ajout livre : " + e.getMessage());
        }
        return false;
    }

    private boolean incrementerExemplaires(int idLivre, int nbTotal, int nbDispo, Livre l) {
        String sql = "UPDATE livres SET nombre_exemplaires = ?, exemplaires_disponibles = ? WHERE id_livre = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, nbTotal);
            s.setInt(2, nbDispo);
            s.setInt(3, idLivre);
            boolean ok = s.executeUpdate() > 0;
            if (ok) l.setIdLivre(idLivre);
            return ok;
        } catch (SQLException e) {
            System.err.println("Erreur incrémentation exemplaires : " + e.getMessage());
            return false;
        }
    }

    public boolean modifier(Livre l) {
        String sql = "UPDATE livres SET titre=?, auteur=?, genre=?, annee=?, isbn=?, image_couverture=?, " +
             "exemplaires_disponibles = exemplaires_disponibles + (? - nombre_exemplaires), " +
             "nombre_exemplaires=? WHERE id_livre=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, l.getTitre());
            s.setString(2, l.getAuteur());
            s.setString(3, l.getGenre());
            if (l.getAnnee() != null) s.setInt(4, l.getAnnee()); else s.setNull(4, Types.INTEGER);
            s.setString(5, l.getIsbn());
            s.setString(6, l.getImageCouverture());
            s.setInt(7, l.getNombreExemplaires());  // pour le delta (nouveau - ancien)
            s.setInt(8, l.getNombreExemplaires());  // pour la valeur finale
            s.setInt(9, l.getIdLivre());
            return s.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur modif livre : " + e.getMessage());
            return false;
        }
    }

    public boolean supprimer(int idLivre) {
        String sqlEmprunts = "DELETE FROM emprunts WHERE id_livre = ? AND statut = 'RENDU'";
        String sql = "DELETE FROM livres WHERE id_livre=?";
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement s1 = c.prepareStatement(sqlEmprunts);
                 PreparedStatement s2 = c.prepareStatement(sql)) {
                s1.setInt(1, idLivre);
                s1.executeUpdate();
                s2.setInt(1, idLivre);
                int rows = s2.executeUpdate();
                c.commit();
                return rows > 0;
            } catch (SQLException e) {
                c.rollback();
                System.err.println("Erreur suppression livre : " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Erreur suppression livre : " + e.getMessage());
        }
        return false;
    }

    private Livre map(ResultSet rs) throws SQLException {
        Date d = rs.getDate("date_ajout");
        return new Livre(
                rs.getInt("id_livre"),
                rs.getString("titre"),
                rs.getString("auteur"),
                rs.getString("genre"),
                rs.getObject("annee", Integer.class),
                rs.getString("isbn"),
                rs.getInt("nombre_exemplaires"),
                rs.getInt("exemplaires_disponibles"),
                d != null ? d.toLocalDate() : null,
                rs.getString("image_couverture")
        );
    }
    public int getTotalExemplaires() {
        String sql = "SELECT COALESCE(SUM(nombre_exemplaires), 0) FROM livres";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 0;
    }

    public int getTotalExemplairesDisponibles() {
        String sql = "SELECT COALESCE(SUM(exemplaires_disponibles), 0) FROM livres";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet rs = s.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 0;
    }
}