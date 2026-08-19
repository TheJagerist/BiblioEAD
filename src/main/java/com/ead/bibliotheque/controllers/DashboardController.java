package com.ead.bibliotheque.controllers;

import com.ead.bibliotheque.dao.AdherentDAO;
import com.ead.bibliotheque.dao.EmpruntDAO;
import com.ead.bibliotheque.dao.LivreDAO;
import com.ead.bibliotheque.dao.RetardDAO;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import com.ead.bibliotheque.models.Emprunt;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import java.util.List;


public class DashboardController {

    // ── Ligne hero ────────────────────────────────────────────
    @FXML private Label kpiEmprunts;
    @FXML private Label kpiRetards;

    // ── Ligne secondaire ──────────────────────────────────────
    @FXML private Label  kpiAdherents;
    @FXML private Label  kpiLivres;
    @FXML private Label  labelLivresTotal;
    @FXML private Region barreProgressionLivres;   // largeur calculée dynamiquement
    @FXML private Label genreNom1, genreNom2, genreNom3;
    @FXML private Label genrePct1, genrePct2, genrePct3;
    @FXML private Region barreGenre1, barreGenre2, barreGenre3;

    // ── Carte "Ce mois" ───────────────────────────────────────
    @FXML private Label statNouveauxEmprunts;
    @FXML private Label statRetoursEffectues;
    @FXML private Label statNouveauxAdherents;
    @FXML private VBox conteneurTimeline;

    // ── DAO ───────────────────────────────────────────────────
    private final AdherentDAO adherentDAO = new AdherentDAO();
    private final EmpruntDAO  empruntDAO  = new EmpruntDAO();
    private final LivreDAO    livreDAO    = new LivreDAO();
    private final RetardDAO   retardDAO   = new RetardDAO();

    @FXML
private void initialize() {
    Task<Void> chargement = new Task<>() {
        int empruntsEnCours, retardsActifs, nbAdherents, dispo, total;
        int nouveauxEmprunts, retoursEffectues, nouveauxAdherents;
        List<Emprunt> activite;
        java.util.Map<String, Integer> genres;

        @Override
        protected Void call() {
            empruntsEnCours = empruntDAO.listerEnCours().size();
            retardsActifs = retardDAO.compterRetardsActifs();
            nbAdherents = adherentDAO.listerTous().size();
            dispo = livreDAO.getTotalExemplairesDisponibles();
            total = livreDAO.getTotalExemplaires();
            nouveauxEmprunts = empruntDAO.compterNouveauxEmpruntsMois();
            retoursEffectues = empruntDAO.compterRetoursMois();
            nouveauxAdherents = adherentDAO.compterNouveauxMois();
            activite = empruntDAO.listerActiviteRecente(4);
            genres = empruntDAO.compterEmpruntsParGenre();
            return null;
        }

        @Override
        protected void succeeded() {
            kpiEmprunts.setText(String.valueOf(empruntsEnCours));
            kpiRetards.setText(String.valueOf(retardsActifs));
            kpiAdherents.setText(String.valueOf(nbAdherents));
            kpiLivres.setText(String.valueOf(dispo));
            labelLivresTotal.setText("/ " + total);

            if (total > 0) {
                double largeur = Math.min(1.0, (double) dispo / total) * 160;
                barreProgressionLivres.setPrefWidth(largeur);
                barreProgressionLivres.setMaxWidth(largeur);
            }

            statNouveauxEmprunts.setText(String.valueOf(nouveauxEmprunts));
            statRetoursEffectues.setText(String.valueOf(retoursEffectues));
            statNouveauxAdherents.setText(String.valueOf(nouveauxAdherents));

            construireTimeline(activite);
            construireGenres(genres);
        }
    };
    new Thread(chargement).start();
}

private void construireTimeline(List<Emprunt> activite) {
    conteneurTimeline.getChildren().clear();
    for (int i = 0; i < activite.size(); i++) {
        Emprunt e = activite.get(i);
        boolean derniere = (i == activite.size() - 1);

        String couleurPuce = switch (e.getStatut()) {
            case RETARD -> "#D0021B";
            case RENDU -> "#97CADB";
            default -> "#018ABE";
        };
        String styleMeta = switch (e.getStatut()) {
            case RETARD -> "-fx-text-fill: #D0021B;";
            case RENDU -> "-fx-text-fill: #6B7A8C;";
            default -> "-fx-text-fill: #018ABE;";
        };
        String labelStatut = switch (e.getStatut()) {
            case RETARD -> "Retard";
            case RENDU -> "Rendu";
            default -> "En cours";
        };

        Circle puce = new Circle(3.5);
        puce.setStyle("-fx-fill: " + couleurPuce + "; -fx-stroke: transparent;");

        Region ligne = new Region();
        ligne.setStyle("-fx-background-color: rgba(0,27,72,0.15); -fx-min-width: 1; -fx-max-width: 1;");
        VBox.setVgrow(ligne, javafx.scene.layout.Priority.ALWAYS);

        VBox colPuce = new VBox(4, puce);
        colPuce.setPrefWidth(14);
        colPuce.setAlignment(javafx.geometry.Pos.TOP_CENTER);
        if (!derniere) colPuce.getChildren().add(ligne);

        Label nom = new Label(e.getNomAdherent());
        nom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #001B48;");

        Label titre = new Label(e.getTitreLivre() + " · ");
        titre.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7A8C;");

        Label statut = new Label(labelStatut);
        statut.setStyle("-fx-font-size: 11px; " + styleMeta);

        javafx.scene.layout.HBox ligneMeta = new javafx.scene.layout.HBox(4, titre, statut);

        VBox texte = new VBox(1, nom, ligneMeta);
        javafx.scene.layout.HBox.setHgrow(texte, javafx.scene.layout.Priority.ALWAYS);
        if (!derniere) texte.setPadding(new javafx.geometry.Insets(0, 0, 15, 0));

        javafx.scene.layout.HBox entree = new javafx.scene.layout.HBox(10, colPuce, texte);
        conteneurTimeline.getChildren().add(entree);
    }
}

private void construireGenres(java.util.Map<String, Integer> genres) {
    var entrees = new java.util.ArrayList<>(genres.entrySet());
    int totalGenres = genres.values().stream().mapToInt(Integer::intValue).sum();
    Label[] noms = {genreNom1, genreNom2, genreNom3};
    Label[] pcts = {genrePct1, genrePct2, genrePct3};
    Region[] barres = {barreGenre1, barreGenre2, barreGenre3};
    for (int i = 0; i < Math.min(3, entrees.size()); i++) {
        int nb = entrees.get(i).getValue();
        int pct = totalGenres > 0 ? nb * 100 / totalGenres : 0;
        noms[i].setText(entrees.get(i).getKey());
        pcts[i].setText(pct + "%");
        barres[i].setPrefWidth(pct * 1.6);
        barres[i].setMaxWidth(pct * 1.6);
    }
}
}

