package com.ead.bibliotheque.controllers;

import com.ead.bibliotheque.dao.LivreDAO;
import com.ead.bibliotheque.models.Livre;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.scene.layout.StackPane;


import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CatalogueController implements Initializable {

    // ── Toolbar ──────────────────────────────────────────────
    @FXML private TextField fieldRecherche;
    @FXML private ComboBox<String> comboGenre;
    @FXML private CheckBox checkDispo;

    // ── Nouveautés ───────────────────────────────────────────
    @FXML private HBox boxNouveautes;

    // ── Tableau ──────────────────────────────────────────────
    @FXML private TableView<Livre> tableCatalogue;
    @FXML private TableColumn<Livre, String>  colTitre;
    @FXML private TableColumn<Livre, String>  colAuteur;
    @FXML private TableColumn<Livre, String>  colGenre;
    @FXML private TableColumn<Livre, Integer> colAnnee;
    @FXML private TableColumn<Livre, String>  colIsbn;
    @FXML private TableColumn<Livre, String>  colDispo;
    @FXML private TableColumn<Livre, String> colModifiePar;
    @FXML private TableColumn<Livre, java.time.LocalDateTime> colModifieLe;

    // ── Boutons ──────────────────────────────────────────────
    @FXML private Button btnModifier;
    @FXML private Button btnSupprimer;

    // ── Panneau latéral ──────────────────────────────────────
    @FXML private VBox panneauFormulaire;
    @FXML private Label labelTitrePanneau;
    @FXML private TextField fieldTitre;
    @FXML private TextField fieldAuteur;
    @FXML private ComboBox<String> comboGenreForm;
    @FXML private TextField fieldAnnee;
    @FXML private TextField fieldIsbn;
    @FXML private Spinner<Integer> spinnerExemplaires;
    @FXML private Label labelErreur;
    @FXML private TextField fieldImageCouverture;
    @FXML private ImageView imagePreview;
    @FXML private Label labelPreviewVide;
    @FXML private StackPane previewCouverture;

    // ── Données ──────────────────────────────────────────────
    private final LivreDAO dao = new LivreDAO();
    private ObservableList<Livre> tousLesLivres = FXCollections.observableArrayList();
    private Livre livreEnCours = null;

    private static final List<String> GENRES = List.of(
            "Littérature", "Informatique", "Sciences", "Histoire",
            "Mathématiques", "Droit", "Économie", "Autre"
    );

    

    // ─────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colAuteur.setCellValueFactory(new PropertyValueFactory<>("auteur"));
        colGenre.setCellValueFactory(new PropertyValueFactory<>("genre"));
        colAnnee.setCellValueFactory(new PropertyValueFactory<>("annee"));
        colIsbn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        // Colonne dispo : affiche "X / Y"
        colDispo.setCellValueFactory(new PropertyValueFactory<>("dispoLabel"));
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

        comboGenre.setItems(FXCollections.observableArrayList(GENRES));
        comboGenreForm.setItems(FXCollections.observableArrayList(GENRES));

        chargerLivres();
    }

    // ── Chargement ───────────────────────────────────────────
    private void chargerLivres() {
        tousLesLivres = FXCollections.observableArrayList(dao.listerTous());
        tableCatalogue.setItems(tousLesLivres);
        rafraichirNouveautes();
    }

    private static final int MAX_NOUVEAUTES_VISIBLE = 8;
private List<Livre> toutesLesNouveautes = new java.util.ArrayList<>();

private void rafraichirNouveautes() {
    boxNouveautes.getChildren().clear();
    toutesLesNouveautes = tousLesLivres.stream()
            .filter(Livre::isNouveaute)
            .collect(java.util.stream.Collectors.toList());

    toutesLesNouveautes.stream().limit(MAX_NOUVEAUTES_VISIBLE)
            .forEach(l -> boxNouveautes.getChildren().add(creerCarteNouveaute(l)));

    if (toutesLesNouveautes.size() > MAX_NOUVEAUTES_VISIBLE) {
        Button voirPlus = new Button("Voir\ntout →");
        voirPlus.setStyle("-fx-background-color: #f0f4f8; -fx-border-color: #cccccc; -fx-border-width:1;" +
                "-fx-border-radius:6; -fx-background-radius:6; -fx-font-family:'PublicSans';" +
                "-fx-font-size:11px; -fx-text-fill:#2E7A98; -fx-cursor:hand; -fx-min-width:60;");
        voirPlus.setPrefHeight(138);
        voirPlus.setOnAction(e -> afficherToutesNouveautes());
        boxNouveautes.getChildren().add(voirPlus);
    }
}

private void afficherToutesNouveautes() {
    javafx.scene.layout.FlowPane flow = new javafx.scene.layout.FlowPane(12, 12);
    flow.setPadding(new javafx.geometry.Insets(12));
    toutesLesNouveautes.forEach(l -> flow.getChildren().add(creerCarteNouveaute(l)));

    javafx.scene.control.ScrollPane sp = new javafx.scene.control.ScrollPane(flow);
    sp.setFitToWidth(true);
    sp.setPrefSize(700, 500);

    javafx.scene.Scene scene = new javafx.scene.Scene(sp);
    javafx.stage.Stage stage = new javafx.stage.Stage();
    stage.setTitle("Ajoutés cette semaine — tous les livres");
    stage.setScene(scene);
    stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
    stage.show();
}

    private VBox creerCarteNouveaute(Livre l) {
        // Placeholder coloré
        String[] couleurs = {"#1a3a5c","#3a1a2c","#1a3a1a","#3a2a0d","#2a1a3a"};
        String couleur = couleurs[Math.abs(l.getTitre().hashCode()) % couleurs.length];

        StackPane cover = new StackPane();
        cover.setPrefSize(90, 124);
        cover.setStyle("-fx-background-color:" + couleur + "; -fx-background-radius:6;");

        // Image de couverture si disponible
        String imgPath = l.getImageCouverture();
        if (imgPath != null && !imgPath.isBlank()) {
            try {
                String url = imgPath.startsWith("http") ? imgPath : new java.io.File(imgPath).toURI().toString();
                ImageView iv = new ImageView(new Image(url, 90, 124, false, true, true));
                iv.setFitWidth(90); iv.setFitHeight(124);
                iv.setPreserveRatio(false);
                
                cover.getChildren().add(iv);
            } catch (Exception ignored) {}
        }

        // Badge "Nouveau"
        if (l.isNouveaute()) {
            Label badge = new Label("Nouveau");
            badge.setStyle("-fx-background-color:#58cc02;-fx-text-fill:#16213e;-fx-font-size:8px;" +
                    "-fx-padding:2 5 2 5;-fx-background-radius:3;");
            StackPane.setAlignment(badge, javafx.geometry.Pos.TOP_RIGHT);
            cover.getChildren().add(badge);
        }

        // Infos sous la couverture
        Label titre = new Label(l.getTitre());
        titre.setPrefWidth(90); titre.setWrapText(true);
        titre.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:#1a1a1a;");

        Label auteur = new Label(l.getAuteur());
        auteur.setStyle("-fx-font-size:9px; -fx-text-fill:#888888;");

        VBox carte = new VBox(5, cover, titre, auteur);
        carte.setPrefWidth(90);
        carte.setOnMouseClicked(e -> {
    tableCatalogue.getSelectionModel().select(l);
    tableCatalogue.scrollTo(l);
    btnModifier.setDisable(false);
    btnSupprimer.setDisable(false);
    onModifier();
    });
    carte.setStyle("-fx-cursor: hand;");
        return carte;
    }

    // ── Sélection ────────────────────────────────────────────
    @FXML
    private void onSelectionLigne(MouseEvent e) {
        boolean actif = tableCatalogue.getSelectionModel().getSelectedItem() != null;
        btnModifier.setDisable(!actif);
        btnSupprimer.setDisable(!actif);
    }

    @FXML
    private void onImageUrlChange() {
        actualiserPreview(fieldImageCouverture.getText().trim());
    }

    // ── Recherche / filtres ───────────────────────────────────
    @FXML private void onRecherche()    { filtrer(); }
    @FXML private void onFiltreGenre()  { filtrer(); }
    @FXML private void onFiltreDispo()  { filtrer(); }

    private void filtrer() {
        String terme  = fieldRecherche.getText().toLowerCase().trim();
        String genre  = comboGenre.getValue();
        boolean dispo = checkDispo.isSelected();

        List<Livre> filtre = tousLesLivres.stream()
                .filter(l -> terme.isEmpty()
                        || l.getTitre().toLowerCase().contains(terme)
                        || l.getAuteur().toLowerCase().contains(terme)
                        || (l.getIsbn() != null && l.getIsbn().contains(terme)))
                .filter(l -> genre == null || genre.isEmpty() || l.getGenre().equals(genre))
                .filter(l -> !dispo || l.getExemplairesDisponibles() > 0)
                .toList();

        tableCatalogue.setItems(FXCollections.observableArrayList(filtre));
    }

    // ── Ajout ────────────────────────────────────────────────
    @FXML
    private void onNouveauLivre() {
        livreEnCours = null;
        labelTitrePanneau.setText("Nouveau livre");
        viderFormulaire();
        ouvrirPanneau();
    }

    // ── Modification ─────────────────────────────────────────
    @FXML
    private void onModifier() {
        Livre sel = tableCatalogue.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        livreEnCours = sel;
        labelTitrePanneau.setText("Modifier le livre");
        fieldTitre.setText(sel.getTitre());
        fieldAuteur.setText(sel.getAuteur());
        comboGenreForm.setValue(sel.getGenre());
        fieldAnnee.setText(sel.getAnnee() != null ? String.valueOf(sel.getAnnee()) : "");
        fieldIsbn.setText(sel.getIsbn());
        spinnerExemplaires.getValueFactory().setValue(sel.getNombreExemplaires());
        ouvrirPanneau();
        fieldImageCouverture.setText(sel.getImageCouverture() != null ? sel.getImageCouverture() : "");
        actualiserPreview(sel.getImageCouverture());
    }

    // ── Suppression ──────────────────────────────────────────
    @FXML
    private void onSupprimer() {
        Livre sel = tableCatalogue.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer « " + sel.getTitre() + " » et tous ses exemplaires ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                dao.supprimer(sel.getIdLivre());
                chargerLivres();
                desactiverBoutons();
            }
        });
    }

    // ── Sauvegarder ──────────────────────────────────────────
    @FXML
    private void onSauvegarder() {
        if (!validerFormulaire()) return;

        Integer annee = null;
        try { annee = Integer.parseInt(fieldAnnee.getText().trim()); } catch (NumberFormatException ignored) {}

        if (livreEnCours == null) {
            Livre nouveau = new Livre(
        0,
        fieldTitre.getText().trim(),
        fieldAuteur.getText().trim(),
        comboGenreForm.getValue(),
        annee,
        fieldIsbn.getText().trim(),
        spinnerExemplaires.getValue(),
        spinnerExemplaires.getValue(),
        null,
        fieldImageCouverture.getText().trim()
);
            dao.ajouter(nouveau);
        } else {
            livreEnCours.setTitre(fieldTitre.getText().trim());
            livreEnCours.setAuteur(fieldAuteur.getText().trim());
            livreEnCours.setGenre(comboGenreForm.getValue());
            livreEnCours.setAnnee (annee) ;
            livreEnCours.setIsbn(fieldIsbn.getText().trim());
            livreEnCours.setImageCouverture(fieldImageCouverture.getText().trim());
            livreEnCours.setNombreExemplaires(spinnerExemplaires.getValue());
            dao.modifier(livreEnCours);
        }

        chargerLivres();
        fermerPanneau();
    }

    // ── Panneau ───────────────────────────────────────────────
    @FXML private void onFermerPanneau() { fermerPanneau(); }

    private void ouvrirPanneau() {
        afficherErreur(false, "");
        panneauFormulaire.setVisible(true);
        panneauFormulaire.setManaged(true);
    }

    private void fermerPanneau() {
        panneauFormulaire.setVisible(false);
        panneauFormulaire.setManaged(false);
        livreEnCours = null;
    }

    private void viderFormulaire() {
        fieldTitre.clear(); fieldAuteur.clear();
        fieldAnnee.clear(); fieldIsbn.clear();
        fieldImageCouverture.clear();
        comboGenreForm.setValue(null);
        spinnerExemplaires.getValueFactory().setValue(1);
        imagePreview.setVisible(false);
        labelPreviewVide.setVisible(true);
    }

    private boolean validerFormulaire() {
        if (fieldTitre.getText().isBlank())  { afficherErreur(true, "Le titre est obligatoire."); return false; }
        if (fieldAuteur.getText().isBlank())  { afficherErreur(true, "L'auteur est obligatoire."); return false; }
        if (comboGenreForm.getValue() == null){ afficherErreur(true, "Sélectionnez un genre."); return false; }
        String isbn = fieldIsbn.getText().trim();
        if (!isbn.isBlank()) {
            int idExclu = livreEnCours != null ? livreEnCours.getIdLivre() : 0;
            if (dao.isbnExiste(isbn, idExclu)) {
                afficherErreur(true, "Cet ISBN existe déjà dans le catalogue.");
                return false;
            }
        }
        afficherErreur(false, "");
        return true;
    }

    private void afficherErreur(boolean v, String msg) {
        labelErreur.setText(msg);
        labelErreur.setVisible(v);
        labelErreur.setManaged(v);
    }

    @FXML
    private void onChoisirImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image de couverture");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png","*.jpg","*.jpeg","*.gif","*.webp"));
        java.io.File f = fc.showOpenDialog(fieldTitre.getScene().getWindow());
        if (f != null) {
            fieldImageCouverture.setText(f.getAbsolutePath());
            actualiserPreview(f.getAbsolutePath());
        }
    }

    private void actualiserPreview(String path) {
        if (path == null || path.isBlank()) {
            imagePreview.setVisible(false);
            labelPreviewVide.setVisible(true);
            return;
        }
        try {
            String url = path.startsWith("http") ? path : new java.io.File(path).toURI().toString();
            imagePreview.setImage(new Image(url, 80, 110, false, true, true));
            imagePreview.setVisible(true);
            labelPreviewVide.setVisible(false);
        } catch (Exception e) {
            imagePreview.setVisible(false);
            labelPreviewVide.setVisible(true);
        }
    }

    private void desactiverBoutons() {
        btnModifier.setDisable(true);
        btnSupprimer.setDisable(true);
    }
}