package com.ead.bibliotheque.controllers;

import com.ead.bibliotheque.util.ChargementUtil;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;
import com.ead.bibliotheque.dao.AdherentDAO;
import com.ead.bibliotheque.dao.EmpruntDAO;
import com.ead.bibliotheque.models.Adherent;
import com.ead.bibliotheque.models.Emprunt;
import com.ead.bibliotheque.util.RechercheUtil;
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
import java.util.List;
import java.util.ResourceBundle;

public class AdherentsController implements Initializable {

    // ── Tableau ──────────────────────────────────────────────
    @FXML private TableView<Adherent>      tableAdherents;
    @FXML private TableColumn<Adherent, String>    colNom;
    @FXML private TableColumn<Adherent, String>    colPrenom;
    @FXML private TableColumn<Adherent, String>    colClasse;
    @FXML private TableColumn<Adherent, String>    colFiliere;
    @FXML private TableColumn<Adherent, String>    colNumCarte;
    @FXML private TableColumn<Adherent, LocalDate> colInscription;
    @FXML private TableColumn<Adherent, String> colModifiePar;
    @FXML private TableColumn<Adherent, java.time.LocalDateTime> colModifieLe;

    // ── Toolbar ──────────────────────────────────────────────
    @FXML private TextField  fieldRecherche;
    @FXML private ComboBox<String> comboFiliere;

    // ── Boutons d'action ─────────────────────────────────────
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;
    @FXML private Button btnHistorique;

    // ── Panneau latéral ──────────────────────────────────────
    @FXML private VBox  panneauFormulaire;
    @FXML private Label labelTitrePanneau;
    @FXML private TextField  fieldNom;
    @FXML private TextField  fieldPrenom;
    @FXML private ComboBox<String> comboAnnee;
    @FXML private Label labelClasseAuto;
    @FXML private ComboBox<String> comboFiliereForm;
    @FXML private Label      labelNumCarte;
    @FXML private DatePicker dateInscription;
    @FXML private Label      labelErreur;
    @FXML private Button     btnSauvegarder;

    // ── Données ──────────────────────────────────────────────
    private final AdherentDAO dao = new AdherentDAO();
    private ObservableList<Adherent> tousLesAdherents = FXCollections.observableArrayList();
    private Adherent adherentEnCours = null; // null = ajout, sinon = modification

    private static final List<String> FILIERES = List.of(
            "Génie Logiciel (GL)", "Génie Civil (GC)",
            "Électronique & Maintenance Industrielle (EMI)",
            "Maintenance Industrielle et Instrumentation (MII)",
            "Génie Chimique (GCH)", "Gestion des Ressources Humaines (GRH)",
            "Réseaux Informatiques (RI)", "Réseaux et Télécommunications (RT)",
            "Génie Minier Pétrolier (GMP)", "Enseignant"
    );

    private static final java.util.Map<String, String> ABREVIATIONS = java.util.Map.of(
            "Génie Logiciel (GL)", "GL",
            "Génie Civil (GC)", "GC",
            "Électronique & Maintenance Industrielle (EMI)", "EMI",
            "Maintenance Industrielle et Instrumentation (MII)", "MII",
            "Génie Chimique (GCH)", "GCH",
            "Gestion des Ressources Humaines (GRH)", "GRH",
            "Réseaux Informatiques (RI)", "RI",
            "Réseaux et Télécommunications (RT)", "RT",
            "Génie Minier Pétrolier (GMP)", "GMP"
    );

    private static final List<String> ANNEES = List.of(
            "1ère année", "2ème année", "3ème année", "4ème année", "5ème année"
    );

    // ─────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Colonnes
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colClasse.setCellValueFactory(new PropertyValueFactory<>("classe"));
        colFiliere.setCellValueFactory(new PropertyValueFactory<>("filiere"));
        colNumCarte.setCellValueFactory(new PropertyValueFactory<>("numCarte"));
        colInscription.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        colModifiePar.setCellValueFactory(new PropertyValueFactory<>("modifiePar"));
        colModifieLe.setCellValueFactory(new PropertyValueFactory<>("modifieLe"));
        colModifieLe.setCellFactory(col -> new TableCell<>() {
            private static final java.time.format.DateTimeFormatter FMT =
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            @Override protected void updateItem(java.time.LocalDateTime v, boolean empty) {
                super.updateItem(v, empty);
                setText(v == null || empty ? null : v.format(FMT));
            }
        });

        // Combos
        comboFiliere.setItems(FXCollections.observableArrayList(FILIERES));
        comboFiliereForm.setItems(FXCollections.observableArrayList(FILIERES));
        comboAnnee.setItems(FXCollections.observableArrayList(ANNEES));

        chargerAdherents();
    }

    // ── Chargement ───────────────────────────────────────────
    private void chargerAdherents() {
        tousLesAdherents = FXCollections.observableArrayList(dao.listerTous());
        tableAdherents.setItems(tousLesAdherents);
    }

    // ── Sélection dans le tableau ─────────────────────────────
    @FXML
    private void onSelectionLigne(MouseEvent e) {
        Adherent sel = tableAdherents.getSelectionModel().getSelectedItem();
        boolean actif = sel != null;
        btnModifier.setDisable(!actif);
        btnSupprimer.setDisable(!actif);
        btnHistorique.setDisable(!actif);
    }

    // ── Recherche / filtre ────────────────────────────────────
    @FXML
    private void onRecherche() {
        filtrer();
    }

    @FXML
    private void onFiltreFiliere() {
        filtrer();
    }

    private void filtrer() {
        String terme = fieldRecherche.getText().toLowerCase().trim();
        String filiere = comboFiliere.getValue();

        List<Adherent> filtre = tousLesAdherents.stream()
                .filter(a -> RechercheUtil.contient(terme,
                        a.getNom(), a.getPrenom(), a.getClasse(), a.getFiliere(),
                        a.getNumCarte(), a.getModifiePar(),
                        a.getDateInscription(), a.getModifieLe()))
                .filter(a -> filiere == null || filiere.isEmpty() || a.getFiliere().equals(filiere))
                .toList();

        tableAdherents.setItems(FXCollections.observableArrayList(filtre));
    }

    // ── Ouvrir panneau : AJOUT ────────────────────────────────
    @FXML
    private void onNouvelAdherent() {
        adherentEnCours = null;
        labelTitrePanneau.setText("Nouvel adhérent");
        viderFormulaire();
        labelNumCarte.setText(dao.genererProchainNumCarte());
        ouvrirPanneau();
    }

    // ── Ouvrir panneau : MODIFICATION ────────────────────────
    @FXML
    private void onModifier() {
        Adherent sel = tableAdherents.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        adherentEnCours = sel;
        labelTitrePanneau.setText("Modifier l'adhérent");
        fieldNom.setText(sel.getNom());
        fieldPrenom.setText(sel.getPrenom());
        labelClasseAuto.setText(sel.getClasse());
        // Pré-sélectionner filière et année si possible
        comboFiliereForm.setValue(sel.getFiliere());
        onAnneeOuFiliereChange();
        comboFiliereForm.setValue(sel.getFiliere());
        labelNumCarte.setText(sel.getNumCarte());
        dateInscription.setValue(sel.getDateInscription());
        ouvrirPanneau();
    }

    // ── Supprimer ─────────────────────────────────────────────
   @FXML
    private void onSupprimer() {
        Adherent sel = tableAdherents.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        if (new EmpruntDAO().compterEmpruntsActifs(sel.getIdAdherent()) > 0) {
            new Alert(Alert.AlertType.WARNING,
                    "Impossible de supprimer " + sel.getNom() + " : il a des emprunts en cours ou en retard.")
                    .showAndWait();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer " + sel.getNom() + " " + sel.getPrenom() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(r -> {
            if (r != ButtonType.YES) return;

            StackPane overlay = ChargementUtil.creerOverlay();
            javafx.scene.layout.HBox racine = (javafx.scene.layout.HBox) panneauFormulaire.getParent();
            racine.getChildren().add(overlay);
            ChargementUtil.afficher(overlay);
            btnSupprimer.setDisable(true);

            Task<Void> tache = new Task<>() {
                @Override
                protected Void call() {
                    dao.supprimer(sel.getIdAdherent());
                    return null;
                }
            };

            tache.setOnSucceeded(e -> {
                racine.getChildren().remove(overlay);
                btnSupprimer.setDisable(false);
                chargerAdherents();
                btnModifier.setDisable(true);
                btnSupprimer.setDisable(true);
                btnHistorique.setDisable(true);
            });

            tache.setOnFailed(e -> {
                racine.getChildren().remove(overlay);
                btnSupprimer.setDisable(false);
                new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression.").showAndWait();
            });

            new Thread(tache).start();
        });
    }

    // ── Historique des emprunts de l'adhérent sélectionné ──
    @FXML
    private void onHistorique() {
        Adherent sel = tableAdherents.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        List<Emprunt> historique = new EmpruntDAO().listerParAdherent(sel.getIdAdherent());
        String contenu = construireHistoriqueTexte(sel, historique);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Historique des emprunts");
        dialog.setHeaderText("Activité de " + sel.getNom() + " " + sel.getPrenom());

        TextArea area = new TextArea(contenu);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(18);
        area.setPrefColumnCount(90);

        dialog.getDialogPane().setContent(area);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private String construireHistoriqueTexte(Adherent adherent, List<Emprunt> historique) {
        if (historique == null || historique.isEmpty()) {
            return "Aucun emprunt enregistré pour " + adherent.getNom() + " " + adherent.getPrenom() + ".";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Adhérent : ").append(adherent.getNom()).append(" ").append(adherent.getPrenom())
          .append("\nCarte : ").append(adherent.getNumCarte())
          .append("\n\n");

        for (Emprunt e : historique) {
            sb.append("- Livre : ").append(e.getTitreLivre())
              .append("\n  Emprunt : ").append(e.getDateEmprunt())
              .append(" | Retour prévu : ").append(e.getDateRetourPrevue());

            if (e.getDateRetourEffectif() != null) {
                sb.append(" | Retourné le : ").append(e.getDateRetourEffectif());
            }

            sb.append(" | Statut : ").append(e.getStatut());
            sb.append("\n\n");
        }

        return sb.toString();
    }

    // ── Sauvegarder (ajout ou modif) ─────────────────────────
    @FXML
private void onSauvegarder() {
    if (!validerFormulaire()) return;

    // Snapshot des valeurs du formulaire avant de passer au thread BDD
    final String nom    = fieldNom.getText().trim().toUpperCase();
    final String prenom = fieldPrenom.getText().trim();
    final String classe = getClasseGeneree();
    final String filiere = comboFiliereForm.getValue();
    final java.time.LocalDate dateInscr = dateInscription.getValue();
    final String numCarte = labelNumCarte.getText();

    // Overlay sur le panneau latéral uniquement
    StackPane overlay = ChargementUtil.creerOverlay();
    // On récupère le parent du panneau (le HBox racine) pour y superposer le spinner
    javafx.scene.layout.HBox racine = (javafx.scene.layout.HBox) panneauFormulaire.getParent();
    racine.getChildren().add(overlay);
    ChargementUtil.afficher(overlay);
    btnSauvegarder.setDisable(true);

    final Adherent cible = adherentEnCours;
    Task<Void> tache = new Task<>() {
        @Override
        protected Void call() {
            if (cible == null) {
                Adherent nouveau = new Adherent(0, nom, prenom, classe, filiere, numCarte, dateInscr);
                dao.ajouter(nouveau);
            } else {
                cible.setNom(nom);
                cible.setPrenom(prenom);
                cible.setClasse(classe);
                cible.setFiliere(filiere);
                cible.setDateInscription(dateInscr);
                dao.modifier(cible);
            }
            return null;
        }
    };

    tache.setOnSucceeded(e -> {
        racine.getChildren().remove(overlay);
        btnSauvegarder.setDisable(false);
        chargerAdherents();
        fermerPanneau();
    });

    tache.setOnFailed(e -> {
        racine.getChildren().remove(overlay);
        btnSauvegarder.setDisable(false);
        afficherErreur(true, "Erreur lors de l'enregistrement.");
    });

    new Thread(tache).start();
}
    private String getClasseGeneree() {
        return labelClasseAuto.getText().equals("— auto —") ? "" : labelClasseAuto.getText();
    }

    // ── Fermer panneau ────────────────────────────────────────
    @FXML
    private void onFermerPanneau() {
        fermerPanneau();
    }

    // ── Helpers ───────────────────────────────────────────────
    private void ouvrirPanneau() {
        afficherErreur(false, "");
        panneauFormulaire.setVisible(true);
        panneauFormulaire.setManaged(true);
    }

    private void fermerPanneau() {
        panneauFormulaire.setVisible(false);
        panneauFormulaire.setManaged(false);
        adherentEnCours = null;
    }

    private void viderFormulaire() {
        fieldNom.clear();
        fieldPrenom.clear();
        comboAnnee.setValue(null);
        labelClasseAuto.setText("— auto —");
        comboFiliereForm.setValue(null);
        dateInscription.setValue(LocalDate.now());
    }

    private boolean validerFormulaire() {
        if (fieldNom.getText().isBlank()) {
            afficherErreur(true, "Le nom est obligatoire.");
            return false;
        }
        if (fieldPrenom.getText().isBlank()) {
            afficherErreur(true, "Le prénom est obligatoire.");
            return false;
        }
        String classe = getClasseGeneree();
        if (classe.isBlank() || classe.equals("?") || classe.contains("?")) {
            afficherErreur(true, "Sélectionnez une filière et une année.");
            return false;
        }
        if (comboFiliereForm.getValue() == null) {
            afficherErreur(true, "Sélectionnez une filière.");
            return false;
        }
        if (dateInscription.getValue() == null) {
            afficherErreur(true, "La date d'inscription est obligatoire.");
            return false;
        }
        afficherErreur(false, "");
        return true;
    }

    private void afficherErreur(boolean visible, String msg) {
        labelErreur.setText(msg);
        labelErreur.setVisible(visible);
        labelErreur.setManaged(visible);
    }
    @FXML
    private void onAnneeOuFiliereChange() {
        String filiere = comboFiliereForm.getValue();
        boolean estEnseignant = "Enseignant".equals(filiere);

        comboAnnee.setVisible(!estEnseignant);
        comboAnnee.setManaged(!estEnseignant);

        if (estEnseignant) {
            labelClasseAuto.setText("ENS");
        } else {
            String abrev = filiere != null ? ABREVIATIONS.getOrDefault(filiere, "?") : "?";
            String annee = comboAnnee.getValue();
            String num = annee != null ? annee.substring(0, 1) : "?";
            labelClasseAuto.setText(abrev + num);
        }
    }

}