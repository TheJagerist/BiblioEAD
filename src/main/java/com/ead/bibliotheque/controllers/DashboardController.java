package com.ead.bibliotheque.controllers;

import com.ead.bibliotheque.dao.AdherentDAO;
import com.ead.bibliotheque.dao.EmpruntDAO;
import com.ead.bibliotheque.dao.LivreDAO;
import com.ead.bibliotheque.dao.RetardDAO;
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
        // KPI hero
        int empruntsEnCours = empruntDAO.listerEnCours().size();
        int retardsActifs = retardDAO.compterRetardsActifs();
        kpiEmprunts.setText(String.valueOf(empruntsEnCours));
        kpiRetards.setText(String.valueOf(retardsActifs));

        // KPI adhérents
        int nbAdherents = adherentDAO.listerTous().size();
        kpiAdherents.setText(String.valueOf(nbAdherents));

        // KPI livres
        int dispo = livreDAO.getTotalExemplairesDisponibles();
        int total = livreDAO.getTotalExemplaires();
        kpiLivres.setText(String.valueOf(dispo));
        labelLivresTotal.setText("/ " + total);

        // Barre de progression livres (max 160px pour 100%)
        if (total > 0) {
            double ratio = Math.min(1.0, (double) dispo / total);
            double largeur = ratio * 160;
            barreProgressionLivres.setPrefWidth(largeur);
            barreProgressionLivres.setMaxWidth(largeur);
        }

        // Stats "Ce mois"
        statNouveauxEmprunts.setText(String.valueOf(empruntDAO.compterNouveauxEmpruntsMois()));
        statRetoursEffectues.setText(String.valueOf(empruntDAO.compterRetoursMois()));
        statNouveauxAdherents.setText(String.valueOf(adherentDAO.compterNouveauxMois()));


        // Timeline activité récente
        List<Emprunt> activite = empruntDAO.listerActiviteRecente(4);
        conteneurTimeline.getChildren().clear();
        for (int i = 0; i < activite.size(); i++) {
            Emprunt e = activite.get(i);
            boolean derniere = (i == activite.size() - 1);

            String couleurPuce = switch (e.getStatut()) {
                case RETARD -> "#CC0000";
                case RENDU -> "#cccccc";
                default -> "#5BB8D4";
            };
            String styleMeta = switch (e.getStatut()) {
                case RETARD -> "-fx-text-fill: #CC0000;";
                case RENDU -> "-fx-text-fill: #9a9a9a;";
                default -> "-fx-text-fill: #2E7A98;";
            };
            String labelStatut = switch (e.getStatut()) {
                case RETARD -> "Retard";
                case RENDU -> "Rendu";
                default -> "En cours";
            };

            Circle puce = new Circle(3.5);
            puce.setStyle("-fx-fill: " + couleurPuce + "; -fx-stroke: transparent;");

            Region ligne = new Region();
            ligne.setStyle("-fx-background-color: #e5e5e5; -fx-min-width: 1; -fx-max-width: 1;");
            javafx.scene.layout.VBox.setVgrow(ligne, javafx.scene.layout.Priority.ALWAYS);

            VBox colPuce = new VBox(4, puce);
            colPuce.setPrefWidth(14);
            colPuce.setAlignment(javafx.geometry.Pos.TOP_CENTER);
            if (!derniere) colPuce.getChildren().add(ligne);

            javafx.scene.control.Label nom = new javafx.scene.control.Label(e.getNomAdherent());
            nom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");

            javafx.scene.control.Label titre = new javafx.scene.control.Label(e.getTitreLivre() + " · ");
            titre.setStyle("-fx-font-size: 11px; -fx-text-fill: #9a9a9a;");

            javafx.scene.control.Label statut = new javafx.scene.control.Label(labelStatut);
            statut.setStyle("-fx-font-size: 11px; " + styleMeta);

            javafx.scene.layout.HBox ligneMeta = new javafx.scene.layout.HBox(4, titre, statut);

            VBox texte = new VBox(1, nom, ligneMeta);
            javafx.scene.layout.HBox.setHgrow(texte, javafx.scene.layout.Priority.ALWAYS);
            if (!derniere) texte.setPadding(new javafx.geometry.Insets(0, 0, 15, 0));

            javafx.scene.layout.HBox entree = new javafx.scene.layout.HBox(10, colPuce, texte);
            conteneurTimeline.getChildren().add(entree);
        }
    }
}
