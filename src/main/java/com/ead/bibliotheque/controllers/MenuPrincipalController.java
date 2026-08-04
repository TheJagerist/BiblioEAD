package com.ead.bibliotheque.controllers;

import com.ead.bibliotheque.MainApp;
import com.ead.bibliotheque.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class MenuPrincipalController {

    @FXML private StackPane contenuPrincipal;
    @FXML private Label labelTitreVue;
    @FXML private Label labelAdmin;
    @FXML private Label labelAvatar;

    @FXML private Button btnDashboard;
    @FXML private Button btnAdherents;
    @FXML private Button btnCatalogue;
    @FXML private Button btnEmprunts;

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
        Button src = (Button) e.getSource();
        if (src == btnDashboard)  chargerVue("dashboard.fxml",  "Tableau de bord");
        if (src == btnAdherents)  chargerVue("adherents.fxml",  "Gestion des adhérents");
        if (src == btnCatalogue)  chargerVue("catalogue.fxml",  "Catalogue de livres");
        if (src == btnEmprunts)   chargerVue("emprunts.fxml",   "Gestion des Activités");
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