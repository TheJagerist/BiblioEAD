package com.ead.bibliotheque.controllers;

import com.ead.bibliotheque.MainApp;
import com.ead.bibliotheque.dao.AdministrateurDAO;
import com.ead.bibliotheque.models.Administrateur;
import com.ead.bibliotheque.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Optional;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private final AdministrateurDAO administrateurDAO = new AdministrateurDAO();

    @FXML
    private void initialize() {
        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    @FXML
    private void onSoumettre() {
        String login = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String motDePasse = passwordField.getText() == null ? "" : passwordField.getText();

        if (login.isEmpty() || motDePasse.isEmpty()) {
            afficherErreur("Veuillez renseigner l'identifiant et le mot de passe.");
            return;
        }
        /*
         * Bloc temporaire de connexion hardcodée, commenté pour revenir à la version de base.
         * NE SUPPRIME PAS, il peut être réutilisé plus tard.
         
        // Temporary hardcoded credentials until DB is connected
        final String HARDCODED_LOGIN = "admin";
        final String HARDCODED_PASSWORD = "password";

        if (login.equals(HARDCODED_LOGIN) && motDePasse.equals(HARDCODED_PASSWORD)) {
            try {
                MainApp.afficherMenuPrincipal();
            } catch (IOException e) {
                afficherErreur("Erreur lors du chargement du menu principal.");
                e.printStackTrace();
            }
            return;
        }*/


        // Fallback to DAO authentication when not matching hardcoded credentials
        Optional<Administrateur> resultat = administrateurDAO.authentifier(login, motDePasse);

        if (resultat.isPresent()) {
            SessionManager.connecter(resultat.get());
            try {
                MainApp.afficherMenuPrincipal();
            } catch (IOException e) {
                afficherErreur("Erreur lors du chargement du menu principal.");
                e.printStackTrace();
            }
        } else {
            afficherErreur("Identifiant ou mot de passe incorrect.");
        }
    }

    private void afficherErreur(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, message);
            alert.setHeaderText(null);
            alert.showAndWait();
        }
    }
}
