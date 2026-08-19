package com.ead.bibliotheque.controllers;

import com.ead.bibliotheque.util.ChargementUtil;
import javafx.concurrent.Task;
import com.ead.bibliotheque.MainApp;
import com.ead.bibliotheque.util.SessionManager;
import com.ead.bibliotheque.util.StageUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class MenuPrincipalController {

    private double decalageX;
    private double decalageY;
    private boolean fenetreEtendue = true;

    @FXML private StackPane overlayChargement;
    @FXML private StackPane contenuPrincipal;
    @FXML private BorderPane rootPane;
    @FXML private Label labelTitreVue;
    @FXML private Label labelAdmin;
    @FXML private Label labelAvatar;
    @FXML private HBox hboxProfil;

    @FXML private Button btnDashboard;
    @FXML private Button btnAdherents;
    @FXML private Button btnCatalogue;
    @FXML private Button btnEmprunts;
    @FXML private Button btnParametres;

    @FXML
    private void initialize() {
        // Affiche le nom de l'admin connecté
        var admin = SessionManager.getAdministrateurConnecte();
        if (admin != null) {
            labelAdmin.setText(admin.getLogin());
            String initiales = admin.getLogin().length() >= 2
                    ? admin.getLogin().substring(0, 2).toUpperCase()
                    : admin.getLogin().toUpperCase();
            labelAvatar.setText(initiales);
        }
                // Overlay de chargement, ajouté une seule fois par-dessus contenuPrincipal
        overlayChargement = ChargementUtil.creerOverlay();
        contenuPrincipal.getChildren().add(overlayChargement);

        // Charge le dashboard par défaut
        chargerVue("dashboard.fxml", "Tableau de bord");
        btnDashboard.getStyleClass().remove("lien-navigation");
        btnDashboard.getStyleClass().add("lien-navigation-actif");
        if (!rootPane.getStyleClass().contains("sans-marge"))
            rootPane.getStyleClass().add("sans-marge");
        hboxProfil.setOnMouseClicked(event -> ouvrirMonProfil());
    }

        private void chargerVue(String fxmlFile, String titre) {
        ChargementUtil.afficher(overlayChargement);

        Task<Parent> tache = new Task<>() {
            @Override
            protected Parent call() throws IOException {
                return FXMLLoader.load(
            getClass().getResource("/com/ead/bibliotheque/fxml/" + fxmlFile));
            }
        };

        tache.setOnSucceeded(e -> {
            Parent vue = tache.getValue();
            contenuPrincipal.getChildren().add(0, vue);
            contenuPrincipal.getChildren().removeIf(n -> n != overlayChargement && n != vue);
            labelTitreVue.setText(titre);
            ChargementUtil.masquer(overlayChargement);
        });

        tache.setOnFailed(e -> {
            tache.getException().printStackTrace();
            ChargementUtil.masquer(overlayChargement);
        });

        new Thread(tache).start();
    }

    @FXML
    private void handleNav(ActionEvent e) {
        // Retirer la classe active de tous les boutons
        for (Button btn : new Button[]{btnDashboard, btnAdherents, btnCatalogue, btnEmprunts, btnParametres}) {
            btn.getStyleClass().remove("lien-navigation-actif");
            if (!btn.getStyleClass().contains("lien-navigation"))
                btn.getStyleClass().add("lien-navigation");
        }

        // Appliquer la classe active au bouton cliqué
        Button src = (Button) e.getSource();
        src.getStyleClass().remove("lien-navigation");
        src.getStyleClass().add("lien-navigation-actif");

        if (src == btnDashboard) chargerVue("dashboard.fxml",  "Tableau de bord");
        if (src == btnAdherents) chargerVue("adherents.fxml",  "Gestion des adhérents");
        if (src == btnCatalogue) chargerVue("catalogue.fxml",  "Catalogue de livres");
        if (src == btnEmprunts)  chargerVue("emprunts.fxml",   "Gestion des Activités");
        if (src == btnParametres) chargerVue("parametres.fxml", "Paramètres");
    }

    @FXML
    private void ouvrirMonProfil() {
        chargerVue("mon_profil.fxml", "Mon profil");
    }

    @FXML
    private void handleDeconnexion(ActionEvent e) {
        SessionManager.deconnecter();
        try {
            MainApp.afficherConnexion();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleMinimiser() {
        getStage().setIconified(true);
    }

    @FXML
    private void handleMaximiser() {
        Stage stage = getStage();
        if (fenetreEtendue) {
            StageUtil.restaurerTailleStandard(stage);
            rootPane.getStyleClass().remove("sans-marge");
            fenetreEtendue = false;
        } else {
            StageUtil.etendreDansEcran(stage);
            if (!rootPane.getStyleClass().contains("sans-marge"))
                rootPane.getStyleClass().add("sans-marge");
            fenetreEtendue = true;
        }
    }

    @FXML
    private void handleFermer() {
        getStage().close();
    }

    @FXML
    private void handleDebutDeplacement(MouseEvent event) {
        Stage stage = getStage();
        decalageX = event.getScreenX() - stage.getX();
        decalageY = event.getScreenY() - stage.getY();
    }

    @FXML
    private void handleDeplacement(MouseEvent event) {
        Stage stage = getStage();
        if (!stage.isMaximized()) {
            stage.setX(event.getScreenX() - decalageX);
            stage.setY(event.getScreenY() - decalageY);
        }
    }

    private Stage getStage() {
        return (Stage) contenuPrincipal.getScene().getWindow();
    }
}