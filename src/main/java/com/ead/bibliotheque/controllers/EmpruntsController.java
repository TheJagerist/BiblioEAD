package com.ead.bibliotheque.controllers;

import com.ead.bibliotheque.util.ChargementUtil;
import javafx.concurrent.Task;
import com.ead.bibliotheque.dao.AdherentDAO;
import com.ead.bibliotheque.dao.EmpruntDAO;
import com.ead.bibliotheque.dao.LivreDAO;
import com.ead.bibliotheque.dao.RetardDAO;
import com.ead.bibliotheque.models.Adherent;
import com.ead.bibliotheque.models.Emprunt;
import com.ead.bibliotheque.models.Livre;
import com.ead.bibliotheque.util.RechercheUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;


public class EmpruntsController implements Initializable {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int QUOTA_MAX = 3;

    // ── Onglets ──────────────────────────────────────────────
    @FXML private TabPane tabPane;
    @FXML private Tab tabEnCours, tabRetards, tabHistorique;

    // ── KPI ──────────────────────────────────────────────────
    @FXML private HBox  kpiBar;
    @FXML private Label kpiRetardsActifs, kpiJoursCumules, kpiRetardMax;
    @FXML private Label kpiEnCoursTotal, kpiARendreSemaine, kpiTotalLivres;

    // ── Tableau En cours ─────────────────────────────────────
    @FXML private TextField fieldRecherche;
    @FXML private TableView<Emprunt>              tableEmprunts;
    @FXML private TableColumn<Emprunt, String>    colAdherent, colLivre, colJoursRestants;
    @FXML private TableColumn<Emprunt, LocalDate> colDateEmprunt, colDateRetour;
    @FXML private TableColumn<Emprunt, Emprunt.Statut> colStatut;
    @FXML private TableColumn<Emprunt, String>    colCreePar;
    @FXML private Button btnRetour, btnHistoriqueAdherent, btnNouvelEmprunt;

    // ── Tableau Retards ───────────────────────────────────────
    @FXML private TextField fieldRechercheRetards;
    @FXML private ComboBox<String> comboSeverite;
    @FXML private TableView<Emprunt>              tableRetards;
    @FXML private TableColumn<Emprunt, String>    colAdherentR, colLivreR, colJoursRestantsR, colSeverite;
    @FXML private TableColumn<Emprunt, LocalDate> colDateEmpruntR, colDateRetourR;
    @FXML private Button btnRetourRetard, btnRelancer;

    // ── Tableau Historique ────────────────────────────────────
    @FXML private TextField fieldRechercheHisto;
    @FXML private TableView<Emprunt>              tableHistorique;
    @FXML private TableColumn<Emprunt, String>    colAdherentH, colLivreH, colStatutH;
    @FXML private TableColumn<Emprunt, LocalDate> colDateEmpruntH, colDateRetourH;
    @FXML private TableColumn<Emprunt, String>    colCreeParH;

    // ── Panneau latéral ──────────────────────────────────────
    @FXML private VBox  panneauFormulaire, contenuEmprunt, contenuFicheRetard;
    @FXML private VBox  blocRG03, blocRG01;
    @FXML private HBox  footerEmprunt;
    @FXML private VBox  footerRetard;
    @FXML private Label labelTitrePanneau, labelQuota, labelRetardAdherent;
    @FXML private Label labelDispo, labelDateRetour, labelErreur;
    @FXML private Label labelNomAdherent, labelInfoAdherent;
    @FXML private Label labelFicheTitre, labelFicheAuteur, labelFicheEmprunt;
    @FXML private Label labelFicheRetourPrevu, labelFicheRetardJours, labelFicheSeverite;
    @FXML private ComboBox<Adherent> comboAdherent;
    @FXML private ComboBox<Livre>    comboLivre;
    @FXML private TextField fieldDateEmprunt;
    @FXML private Button btnConfirmer;

    // ── DAO ──────────────────────────────────────────────────
    private final EmpruntDAO  empruntDAO  = new EmpruntDAO();
    private final AdherentDAO adherentDAO = new AdherentDAO();
    private final LivreDAO    livreDAO    = new LivreDAO();
    private final RetardDAO   retardDAO   = new RetardDAO();

    private ObservableList<Emprunt> tousEmprunts   = FXCollections.observableArrayList();
    private ObservableList<Emprunt> tousRetards     = FXCollections.observableArrayList();
    private ObservableList<Emprunt> tousHistorique  = FXCollections.observableArrayList();
    private ObservableList<Adherent> listeAdherents;
    private ObservableList<Livre>    listeLivres;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        empruntDAO.synchroniserStatutsRetard();
        configurerColonnesEnCours();
        configurerColonnesRetards();
        configurerColonnesHistorique();
        configurerCombos();
        chargerEnCours();
        fieldDateEmprunt.setText(LocalDate.now().format(FMT));
    }

    // ── Configuration colonnes ────────────────────────────────
    private void configurerColonnesEnCours() {
        colAdherent.setCellValueFactory(new PropertyValueFactory<>("nomAdherent"));
        colLivre.setCellValueFactory(new PropertyValueFactory<>("titreLivre"));
        colDateEmprunt.setCellValueFactory(new PropertyValueFactory<>("dateEmprunt"));
        colDateRetour.setCellValueFactory(new PropertyValueFactory<>("dateRetourPrevue"));
        colJoursRestants.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().getJoursLabel()));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colCreePar.setCellValueFactory(new PropertyValueFactory<>("creePar"));
        tableEmprunts.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Emprunt e, boolean empty) {
                super.updateItem(e, empty);
                setStyle(e != null && !empty && e.getStatut() == Emprunt.Statut.RETARD
                        ? "-fx-border-color:#c62828;-fx-border-width:0 0 0 3;" : "");
            }
        });
    }

    private StackPane getContenuPrincipal() {
    return (StackPane) panneauFormulaire.getScene().lookup("#contenuPrincipal");
}

    private void configurerColonnesRetards() {
        colAdherentR.setCellValueFactory(new PropertyValueFactory<>("nomAdherent"));
        colLivreR.setCellValueFactory(new PropertyValueFactory<>("titreLivre"));
        colDateEmpruntR.setCellValueFactory(new PropertyValueFactory<>("dateEmprunt"));
        colDateRetourR.setCellValueFactory(new PropertyValueFactory<>("dateRetourPrevue"));
        colJoursRestantsR.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().getJoursLabel()));
        colSeverite.setCellValueFactory(cd -> {
            long j = ChronoUnit.DAYS.between(cd.getValue().getDateRetourPrevue(), LocalDate.now());
            return new javafx.beans.property.SimpleStringProperty(
                    j >= 14 ? "Critique" : j >= 7 ? "Élevé" : "Modéré");
        });
        tableRetards.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(Emprunt e, boolean empty) {
                super.updateItem(e, empty);
                if (e == null || empty) { setStyle(""); return; }
                long j = ChronoUnit.DAYS.between(e.getDateRetourPrevue(), LocalDate.now());
                setStyle(j >= 14 ? "-fx-border-color:#c62828;-fx-border-width:0 0 0 3;"
                        : j >= 7 ? "-fx-border-color:#e65100;-fx-border-width:0 0 0 3;"
                          : "-fx-border-color:#f9a825;-fx-border-width:0 0 0 3;");
            }
        });
        comboSeverite.setItems(FXCollections.observableArrayList("Tous", "Critique", "Élevé", "Modéré"));
        comboSeverite.setValue("Tous");
    }

    private void configurerColonnesHistorique() {
        colAdherentH.setCellValueFactory(new PropertyValueFactory<>("nomAdherent"));
        colLivreH.setCellValueFactory(new PropertyValueFactory<>("titreLivre"));
        colDateEmpruntH.setCellValueFactory(new PropertyValueFactory<>("dateEmprunt"));
        colDateRetourH.setCellValueFactory(new PropertyValueFactory<>("dateRetourPrevue"));
        colStatutH.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colCreeParH.setCellValueFactory(new PropertyValueFactory<>("creePar"));
    }

    private void configurerCombos() {
        listeAdherents = FXCollections.observableArrayList(adherentDAO.listerTous());
        comboAdherent.setItems(listeAdherents);
        comboAdherent.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Adherent a) { return a == null ? "" : a.getNom() + " " + a.getPrenom() + " – " + a.getNumCarte(); }
            public Adherent fromString(String s) {
                if (s == null || s.isBlank()) return null;
                return listeAdherents.stream()
                        .filter(a -> (a.getNom() + " " + a.getPrenom() + " – " + a.getNumCarte()).equals(s))
                        .findFirst().orElse(null);
            }
        });
        comboAdherent.getEditor().focusedProperty().addListener((obs, o, focused) -> {
            if (!focused) return; // on n'agit que quand l'éditeur reçoit le focus
        });
        comboAdherent.getEditor().setOnKeyTyped(ev -> {
            if (comboAdherent.getValue() != null) {
                comboAdherent.setValue(null);
                comboAdherent.getEditor().setText(comboAdherent.getEditor().getText());
            }
            String t = comboAdherent.getEditor().getText().toLowerCase().trim();
            if (t.isEmpty()) { comboAdherent.setItems(listeAdherents); }
            else {
                ObservableList<Adherent> m = listeAdherents.filtered(a -> (a.getNom()+" "+a.getPrenom()+" "+a.getNumCarte()).toLowerCase().contains(t));
                ObservableList<Adherent> r = listeAdherents.filtered(a -> !(a.getNom()+" "+a.getPrenom()+" "+a.getNumCarte()).toLowerCase().contains(t));
                ObservableList<Adherent> c = FXCollections.observableArrayList(); c.addAll(m); c.addAll(r);
                comboAdherent.setItems(c);
            }
            if (!comboAdherent.isShowing()) comboAdherent.show();
        });

        listeLivres = FXCollections.observableArrayList(livreDAO.listerTous());
        comboLivre.setItems(listeLivres);
        comboLivre.setConverter(new javafx.util.StringConverter<>() {
            public String toString(Livre l) { return l == null ? "" : l.getTitre() + " – " + l.getAuteur(); }
            public Livre fromString(String s) {
                if (s == null || s.isBlank()) return null;
                return listeLivres.stream()
                        .filter(l -> (l.getTitre() + " – " + l.getAuteur()).equals(s))
                        .findFirst().orElse(null);
            }
        });
       comboLivre.getEditor().setOnKeyTyped(ev -> {
            if (comboLivre.getValue() != null) {
                comboLivre.setValue(null);
                comboLivre.getEditor().setText(comboLivre.getEditor().getText());
            }
            String t = comboLivre.getEditor().getText().toLowerCase().trim();
            if (t.isEmpty()) { comboLivre.setItems(listeLivres); }
            else {
                ObservableList<Livre> m = listeLivres.filtered(l -> (l.getTitre()+" "+l.getAuteur()).toLowerCase().contains(t));
                ObservableList<Livre> r = listeLivres.filtered(l -> !(l.getTitre()+" "+l.getAuteur()).toLowerCase().contains(t));
                ObservableList<Livre> c = FXCollections.observableArrayList(); c.addAll(m); c.addAll(r);
                comboLivre.setItems(c);
            }
            if (!comboLivre.isShowing()) comboLivre.show();
        });
    }

    // ── Chargement ────────────────────────────────────────────
    private void chargerEnCours() {
        tousEmprunts = FXCollections.observableArrayList(empruntDAO.listerEnCours());
        tableEmprunts.setItems(tousEmprunts);
        kpiEnCoursTotal.setText(String.valueOf(tousEmprunts.size()));
        kpiARendreSemaine.setText(String.valueOf(empruntDAO.compterARendreCetteSemaine()));
        kpiTotalLivres.setText(String.valueOf(empruntDAO.compterTotalLivresEmpruntes()));
        btnRetour.setDisable(true);
        btnHistoriqueAdherent.setDisable(true);
    }

    private void chargerRetards() {
        kpiRetardsActifs.setText(String.valueOf(retardDAO.compterRetardsActifs()));
        kpiJoursCumules.setText(String.valueOf(retardDAO.joursRetardCumules()));
        kpiRetardMax.setText(retardDAO.joursRetardMax() + " j");
        tousRetards = FXCollections.observableArrayList(empruntDAO.listerRetards());
        tableRetards.setItems(tousRetards);
        btnRetourRetard.setDisable(true);
        btnRelancer.setDisable(true);
    }

    private void chargerHistorique() {
        List<Emprunt> historique = empruntDAO.listerHistorique();
        List<Emprunt> enCours = empruntDAO.listerEnCours();
        historique.addAll(enCours);
        tousHistorique = FXCollections.observableArrayList(historique);
        tableHistorique.setItems(tousHistorique);
    }

    

    // ── Changement onglet ─────────────────────────────────────
    @FXML
    private void onTabChange() {
        fermerPanneau();
        Tab sel = tabPane.getSelectionModel().getSelectedItem();
        if (sel == tabRetards)    chargerRetards();
        else if (sel == tabHistorique) chargerHistorique();
        else chargerEnCours();
    }

    // ── Recherche ─────────────────────────────────────────────
    private boolean contientTermeAdherentEtLivre(Emprunt e, String terme) {
        if (e == null || terme == null || terme.isBlank()) return true;
        long joursRetard = e.getDateRetourPrevue() != null
                ? ChronoUnit.DAYS.between(e.getDateRetourPrevue(), LocalDate.now()) : 0;
        String severite = joursRetard >= 14 ? "Critique" : joursRetard >= 7 ? "Élevé" : "Modéré";
        return RechercheUtil.contient(terme,
                e.getNomAdherent(), e.getNumCarteAdherent(), e.getTitreLivre(),
                e.getCreePar(), e.getJoursLabel(), severite, e.getStatut(),
                e.getDateEmprunt(), e.getDateRetourPrevue(), e.getDateRetourEffectif());
    }

    @FXML
    private void onRecherche() {
        String t = fieldRecherche.getText().toLowerCase().trim();
        tableEmprunts.setItems(t.isEmpty() ? tousEmprunts :
                tousEmprunts.filtered(e -> contientTermeAdherentEtLivre(e, t)));
    }

    @FXML
    private void onRechercheRetards() {
        String t = fieldRechercheRetards.getText().toLowerCase().trim();
        appliquerFiltreRetards(t, comboSeverite.getValue());
    }

    @FXML
    private void onRechercheHisto() {
        String t = fieldRechercheHisto.getText().toLowerCase().trim();
        tableHistorique.setItems(t.isEmpty() ? tousHistorique :
                tousHistorique.filtered(e -> contientTermeAdherentEtLivre(e, t)));
    }

    @FXML
    private void onFiltreSeverite() {
        appliquerFiltreRetards(
                fieldRechercheRetards.getText().toLowerCase().trim(),
                comboSeverite.getValue());
    }

    private void appliquerFiltreRetards(String terme, String sev) {
        tableRetards.setItems(tousRetards.filtered(e -> {
            boolean ok = terme.isEmpty() || contientTermeAdherentEtLivre(e, terme);
            if (!ok) return false;
            if (sev == null || sev.equals("Tous")) return true;
            long j = ChronoUnit.DAYS.between(e.getDateRetourPrevue(), LocalDate.now());
            String s = j >= 14 ? "Critique" : j >= 7 ? "Élevé" : "Modéré";
            return s.equals(sev);
        }));
    }

    // ── Sélection ligne En cours ──────────────────────────────
    @FXML
    private void onSelectionLigne(MouseEvent ev) {
        Emprunt sel = tableEmprunts.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        btnRetour.setDisable(sel.getStatut() == Emprunt.Statut.RENDU);
        btnHistoriqueAdherent.setDisable(false);
    }

    // ── Sélection ligne Retards ───────────────────────────────
    @FXML
    private void onSelectionLigneRetard(MouseEvent ev) {
        Emprunt sel = tableRetards.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        btnRetourRetard.setDisable(false);
        btnRelancer.setDisable(false);

        // Panneau fiche retard
        labelTitrePanneau.setText("Fiche retard");
        afficherNode(contenuEmprunt, false);
        afficherNode(contenuFicheRetard, true);
        afficherNode(footerEmprunt, false);
        afficherNode(footerRetard, true);
        labelNomAdherent.setText(sel.getNomAdherent());
        labelInfoAdherent.setText(sel.getNumCarteAdherent());
        labelFicheTitre.setText(sel.getTitreLivre());
        labelFicheAuteur.setText("—");
        labelFicheEmprunt.setText(sel.getDateEmprunt().format(FMT));
        labelFicheRetourPrevu.setText(sel.getDateRetourPrevue().format(FMT));
        long j = ChronoUnit.DAYS.between(sel.getDateRetourPrevue(), LocalDate.now());
        labelFicheRetardJours.setText("−" + j + " jours");
        labelFicheSeverite.setText(j >= 14 ? "Critique" : j >= 7 ? "Élevé" : "Modéré");
        panneauFormulaire.setVisible(true);
        panneauFormulaire.setManaged(true);
    }

    // ── Actions ───────────────────────────────────────────────
    @FXML
private void onEnregistrerRetour() {
    Emprunt sel = tabPane.getSelectionModel().getSelectedItem() == tabRetards
            ? tableRetards.getSelectionModel().getSelectedItem()
            : tableEmprunts.getSelectionModel().getSelectedItem();
    if (sel == null) return;

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Confirmer le retour de « " + sel.getTitreLivre() + " » ?",
            ButtonType.YES, ButtonType.NO);
    confirm.setHeaderText(null);
    confirm.showAndWait().ifPresent(r -> {
        if (r != ButtonType.YES) return;

        final int idEmprunt = sel.getIdEmprunt();
        final int idLivre   = sel.getIdLivre();

        StackPane root = getContenuPrincipal();
        StackPane overlay = ChargementUtil.creerOverlay();
        root.getChildren().add(overlay);
        ChargementUtil.afficher(overlay);

        Task<Boolean> tache = new Task<>() {
            @Override
            protected Boolean call() {
                return empruntDAO.enregistrerRetour(idEmprunt, idLivre);
            }
        };

        tache.setOnSucceeded(e -> {
            root.getChildren().remove(overlay);
            fermerPanneau();
            onTabChange();
        });

        tache.setOnFailed(e -> {
            root.getChildren().remove(overlay);
            new Alert(Alert.AlertType.ERROR, "Erreur lors du retour.").showAndWait();
        });

        new Thread(tache).start();
    });
}

    @FXML
    private void onRelancer() {
        Emprunt sel = tableRetards.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        new Alert(Alert.AlertType.INFORMATION,
                "Relance envoyée à " + sel.getNomAdherent() + " (" + sel.getNumCarteAdherent() + ").\n" +
                        "(Fonctionnalité à connecter à un système de notification.)").showAndWait();
    }

    @FXML
    private void onVoirHistorique() {
        Emprunt sel = tableEmprunts.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        tabPane.getSelectionModel().select(tabHistorique);
        fieldRechercheHisto.setText(sel.getNumCarteAdherent());
        onRechercheHisto();
    }

    // ── Nouvel emprunt ────────────────────────────────────────
    @FXML
    private void onNouvelEmprunt() {
        labelTitrePanneau.setText("Nouvel emprunt");
        comboAdherent.setValue(null); comboAdherent.getEditor().clear();
        comboLivre.setValue(null);    comboLivre.getEditor().clear();
        labelDateRetour.setText("— auto —");
        masquerBloc(blocRG03); masquerBloc(blocRG01);
        afficherErreur(false, "");
        afficherNode(contenuEmprunt, true);
        afficherNode(contenuFicheRetard, false);
        afficherNode(footerEmprunt, true);
        afficherNode(footerRetard, false);
        panneauFormulaire.setVisible(true);
        panneauFormulaire.setManaged(true);
    }

    @FXML private void onFermerPanneau() { fermerPanneau(); }

    private void fermerPanneau() {
        if (panneauFormulaire == null) return;
        panneauFormulaire.setVisible(false);
        panneauFormulaire.setManaged(false);
    }

    // ── RG-03 ────────────────────────────────────────────────
    @FXML
    private void onAdherentChange() {
        Adherent a = comboAdherent.getValue();
        if (a == null) { masquerBloc(blocRG03); return; }
        int actifs = empruntDAO.compterEmpruntsActifs(a.getIdAdherent());
        boolean retard = empruntDAO.aUnRetard(a.getIdAdherent());
        labelQuota.setText(actifs + " / " + QUOTA_MAX);
        afficherBloc(blocRG03);
        if (retard) {
            labelRetardAdherent.setText("⚠ Retard en cours — emprunt bloqué.");
            labelRetardAdherent.setVisible(true); labelRetardAdherent.setManaged(true);
            btnConfirmer.setDisable(true);
        } else if (actifs >= QUOTA_MAX) {
            labelRetardAdherent.setText("⚠ Quota de " + QUOTA_MAX + " emprunts atteint.");
            labelRetardAdherent.setVisible(true); labelRetardAdherent.setManaged(true);
            btnConfirmer.setDisable(true);
        } else {
            labelRetardAdherent.setVisible(false); labelRetardAdherent.setManaged(false);
            btnConfirmer.setDisable(false);
        }
    }

    // ── RG-01 ────────────────────────────────────────────────
    @FXML
    private void onLivreChange() {
        Livre l = comboLivre.getValue();
        if (l == null) { masquerBloc(blocRG01); return; }
        int dispo = empruntDAO.getExemplairesDisponibles(l.getIdLivre());
        labelDispo.setText(dispo + " / " + l.getNombreExemplaires());
        afficherBloc(blocRG01);
        if (dispo <= 0) {
            afficherErreur(true, "⚠ Aucun exemplaire disponible (RG-01).");
            btnConfirmer.setDisable(true);
        } else {
            afficherErreur(false, "");
            labelDateRetour.setText(LocalDate.now().plusDays(14).format(FMT) + "  (+14 j)");
            Adherent a = comboAdherent.getValue();
            if (a != null) btnConfirmer.setDisable(
                    empruntDAO.compterEmpruntsActifs(a.getIdAdherent()) >= QUOTA_MAX
                            || empruntDAO.aUnRetard(a.getIdAdherent()));
        }
    }

    // ── Confirmer emprunt ─────────────────────────────────────
    @FXML
private void onConfirmerEmprunt() {
    Adherent a = comboAdherent.getValue();
    Livre    l = comboLivre.getValue();
    if (a == null) { afficherErreur(true, "Sélectionnez un adhérent."); return; }
    if (l == null) { afficherErreur(true, "Sélectionnez un livre.");    return; }
    if (empruntDAO.compterEmpruntsActifs(a.getIdAdherent()) >= QUOTA_MAX) { afficherErreur(true, "Quota atteint (RG-03)."); return; }
    if (empruntDAO.aUnRetard(a.getIdAdherent())) { afficherErreur(true, "Adhérent en retard — emprunt refusé."); return; }
    if (empruntDAO.getExemplairesDisponibles(l.getIdLivre()) <= 0) { afficherErreur(true, "Aucun exemplaire disponible (RG-01)."); return; }

    final int idAdherent = a.getIdAdherent();
    final int idLivre    = l.getIdLivre();

    StackPane root = getContenuPrincipal();
    StackPane overlay = ChargementUtil.creerOverlay();
    root.getChildren().add(overlay);
    ChargementUtil.afficher(overlay);
    btnConfirmer.setDisable(true);

    Task<Boolean> tache = new Task<>() {
        @Override
        protected Boolean call() {
            return empruntDAO.enregistrerEmprunt(idAdherent, idLivre);
        }
    };

    tache.setOnSucceeded(e -> {
        root.getChildren().remove(overlay);
        btnConfirmer.setDisable(false);
        if (tache.getValue()) {
            fermerPanneau();
            chargerEnCours();
        } else {
            afficherErreur(true, "Erreur lors de l'enregistrement.");
        }
    });

    tache.setOnFailed(e -> {
        root.getChildren().remove(overlay);
        btnConfirmer.setDisable(false);
        afficherErreur(true, "Erreur lors de l'enregistrement.");
    });

    new Thread(tache).start();
}

    // ── Helpers ───────────────────────────────────────────────
    private void afficherBloc(VBox b)  { b.setVisible(true);  b.setManaged(true); }
    private void masquerBloc(VBox b)   { b.setVisible(false); b.setManaged(false); }
    private void afficherNode(javafx.scene.Node n, boolean v) {
        if (n == null) return;
        n.setVisible(v); n.setManaged(v);
    }
    private void afficherErreur(boolean v, String msg) {
        labelErreur.setText(msg); labelErreur.setVisible(v); labelErreur.setManaged(v);
    }
}