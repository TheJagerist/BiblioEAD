package com.ead.bibliotheque.controllers;

import com.ead.bibliotheque.MainApp;
import com.ead.bibliotheque.dao.AdministrateurDAO;
import com.ead.bibliotheque.models.Administrateur;
import com.ead.bibliotheque.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
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

    @FXML
    private void onMotDePasseOublie() {
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.setTitle("Réinitialisation du mot de passe");
    dialog.setHeaderText("Entrez votre identifiant et un nouveau mot de passe.");
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    javafx.scene.control.TextField login = new javafx.scene.control.TextField();
    login.setPromptText("Identifiant");
    javafx.scene.control.PasswordField mdp = new javafx.scene.control.PasswordField();
    mdp.setPromptText("Nouveau mot de passe");
    javafx.scene.control.PasswordField confirm = new javafx.scene.control.PasswordField();
    confirm.setPromptText("Confirmer le mot de passe");

    javafx.scene.layout.VBox box = new javafx.scene.layout.VBox(8, login, mdp, confirm);
    box.setPadding(new javafx.geometry.Insets(12));
    dialog.getDialogPane().setContent(box);

    dialog.showAndWait().ifPresent(r -> {
        if (r == ButtonType.OK) {
            if (login.getText().isBlank() || mdp.getText().isBlank()) {
                afficherErreur("Identifiant et mot de passe obligatoires.");
                return;
            }
            if (!mdp.getText().equals(confirm.getText())) {
                afficherErreur("Les mots de passe ne correspondent pas.");
                return;
            }
            boolean ok = administrateurDAO.reinitialiserMotDePasse(login.getText().trim(), mdp.getText());
            if (ok) {
                afficherErreur(""); // clear
                new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION,
                        "Mot de passe mis à jour. Connectez-vous avec vos nouveaux identifiants.")
                        .showAndWait();
            } else {
                afficherErreur("Identifiant introuvable.");
            }
        }
    });
}
}
