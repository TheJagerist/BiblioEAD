package com.ead.bibliotheque.controllers;

import com.ead.bibliotheque.dao.EmpruntDAO;
import com.ead.bibliotheque.dao.RetardDAO;
import com.ead.bibliotheque.models.Retard;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class RetardsController implements Initializable {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── KPI ──────────────────────────────────────────────────
    @FXML private Label kpiRetardsActifs;
    @FXML private Label kpiJoursCumules;
    @FXML private Label kpiRetardMax;

    // ── Tableau ──────────────────────────────────────────────
    @FXML private TableView<Retard>             tableRetards;
    @FXML private TableColumn<Retard, String>   colAdherent;
    @FXML private TableColumn<Retard, String>   colLivre;
    @FXML private TableColumn<Retard, LocalDate> colRetourPrevu;
    @FXML private TableColumn<Retard, LocalDate> colDateEmprunt;
    @FXML private TableColumn<Retard, String>   colRetard;
    @FXML private TableColumn<Retard, String>   colSeverite;

    // ── Toolbar ──────────────────────────────────────────────
    @FXML private TextField             fieldRecherche;
    @FXML private ComboBox<String>      comboSeverite;

    // ── Boutons action ────────────────────────────────────────
    @FXML private Button btnRetour;
    @FXML private Button btnRelancer;

    // ── Panneau fiche ────────────────────────────────────────
    @FXML private VBox  panneauFiche;
    @FXML private Label labelNomAdherent;
    @FXML private Label labelInfoAdherent;
    @FXML private Label labelFicheTitre;
    @FXML private Label labelFicheAuteur;
    @FXML private Label labelFicheEmprunt;
    @FXML private Label labelFicheRetourPrevu;
    @FXML private Label labelFicheRetardJours;
    @FXML private Label labelFicheSeverite;

    // ── DAO & données ─────────────────────────────────────────
    private final RetardDAO  retardDAO  = new RetardDAO();
    private final EmpruntDAO empruntDAO = new EmpruntDAO();

    private ObservableList<Retard> tousLesRetards = FXCollections.observableArrayList();

    // ─────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Synchronise les retards au chargement
        empruntDAO.synchroniserStatutsRetard();

        // Colonnes
        colAdherent.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().getNomComplet()));
        colLivre.setCellValueFactory(new PropertyValueFactory<>("titreLivre"));
        colRetourPrevu.setCellValueFactory(new PropertyValueFactory<>("dateRetourPrevue"));
        colDateEmprunt.setCellValueFactory(new PropertyValueFactory<>("dateEmprunt"));
        colRetard.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().getRetardLabel()));
        colSeverite.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                        severiteLabel(cd.getValue().getSeverite())));

        // Couleur ligne selon sévérité
        tableRetards.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Retard r, boolean empty) {
                super.updateItem(r, empty);
                if (r == null || empty) { setStyle(""); return; }
                switch (r.getSeverite()) {
                    case CRITIQUE -> setStyle("-fx-border-color:#c62828;-fx-border-width:0 0 0 3;");
                    case ELEVE    -> setStyle("-fx-border-color:#e65100;-fx-border-width:0 0 0 3;");
                    default       -> setStyle("-fx-border-color:#f9a825;-fx-border-width:0 0 0 3;");
                }
            }
        });

        // Filtre sévérité
        comboSeverite.setItems(FXCollections.observableArrayList(
                "Tous", "Critique", "Élevé", "Modéré"));
        comboSeverite.setValue("Tous");

        chargerDonnees();
    }

    // ── Chargement ────────────────────────────────────────────
    private void chargerDonnees() {
        // KPI
        kpiRetardsActifs.setText(String.valueOf(retardDAO.compterRetardsActifs()));
        kpiJoursCumules.setText(String.valueOf(retardDAO.joursRetardCumules()));
        kpiRetardMax.setText(retardDAO.joursRetardMax() + " j");

        // Tableau
        tousLesRetards = FXCollections.observableArrayList(retardDAO.listerTous());
        tableRetards.setItems(tousLesRetards);

        // Reset UI
        btnRetour.setDisable(true);
        btnRelancer.setDisable(true);
        fermerPanneau();
    }

    // ── Recherche & filtre ────────────────────────────────────
    @FXML
    private void onRecherche() { filtrer(); }

    @FXML
    private void onFiltreSeverite() { filtrer(); }

    private void filtrer() {
        String terme    = fieldRecherche.getText().toLowerCase().trim();
        String severite = comboSeverite.getValue();

        List<Retard> filtre = tousLesRetards.stream()
                .filter(r -> terme.isEmpty()
                        || r.getNomComplet().toLowerCase().contains(terme)
                        || r.getTitreLivre().toLowerCase().contains(terme)
                        || r.getNumCarte().toLowerCase().contains(terme))
                .filter(r -> severite == null || severite.equals("Tous")
                        || severiteLabel(r.getSeverite()).equals(severite))
                .toList();

        tableRetards.setItems(FXCollections.observableArrayList(filtre));
    }

    // ── Sélection ligne → panneau fiche ───────────────────────
    @FXML
    private void onSelectionLigne(MouseEvent event) {
        Retard sel = tableRetards.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        btnRetour.setDisable(false);
        btnRelancer.setDisable(false);

        // Remplir panneau fiche
        labelNomAdherent.setText(sel.getNomComplet());
        labelInfoAdherent.setText(sel.getClasse() + " · " + sel.getNumCarte());
        labelFicheTitre.setText(sel.getTitreLivre());
        labelFicheAuteur.setText(sel.getAuteurLivre());
        labelFicheEmprunt.setText(fmt(sel.getDateEmprunt()));
        labelFicheRetourPrevu.setText(fmt(sel.getDateRetourPrevue()));
        labelFicheRetardJours.setText("−" + sel.getJoursRetard() + " jours");
        labelFicheSeverite.setText(severiteLabel(sel.getSeverite()));

        panneauFiche.setVisible(true);
        panneauFiche.setManaged(true);
    }

    // ── Enregistrer retour ────────────────────────────────────
    @FXML
    private void onEnregistrerRetour() {
        Retard sel = tableRetards.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmer le retour de « " + sel.getTitreLivre() + " » ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                empruntDAO.enregistrerRetour(sel.getIdEmprunt(), sel.getIdLivre());
                chargerDonnees();
            }
        });
    }

    // ── Relancer l'adhérent (placeholder) ────────────────────
    @FXML
    private void onRelancer() {
        Retard sel = tableRetards.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        new Alert(Alert.AlertType.INFORMATION,
                "Relance envoyée à " + sel.getNomComplet() + " (" + sel.getNumCarte() + ").\n" +
                "(Fonctionnalité à connecter à un système de notification.)")
                .showAndWait();
    }

    // ── Fermer panneau ────────────────────────────────────────
    @FXML
    private void onFermerPanneau() { fermerPanneau(); }

    private void fermerPanneau() {
        panneauFiche.setVisible(false);
        panneauFiche.setManaged(false);
    }

    // ── Helpers ───────────────────────────────────────────────
    private String fmt(LocalDate d) {
        return d == null ? "—" : d.format(FMT);
    }

    private String severiteLabel(Retard.Severite s) {
        return switch (s) {
            case CRITIQUE -> "Critique";
            case ELEVE    -> "Élevé";
            default       -> "Modéré";
        };
    }
}
