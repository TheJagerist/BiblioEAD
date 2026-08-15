package com.ead.bibliotheque.controllers;

import com.ead.bibliotheque.MainApp;
import com.ead.bibliotheque.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MenuPrincipalController {

    @FXML private StackPane contenuPrincipal;
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
        // Charge le dashboard par défaut
        chargerVue("dashboard.fxml", "Tableau de bord");
        btnDashboard.getStyleClass().remove("lien-navigation");
        btnDashboard.getStyleClass().add("lien-navigation-actif");
        hboxProfil.setOnMouseClicked(event -> ouvrirMonProfil());
    }

    private void chargerVue(String fxmlFile, String titre) {
        try {
            Parent vue = FXMLLoader.load(
                getClass().getResource("/com/ead/bibliotheque/fxml/" + fxmlFile));
            contenuPrincipal.getChildren().setAll(vue);
            labelTitreVue.setText(titre);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
