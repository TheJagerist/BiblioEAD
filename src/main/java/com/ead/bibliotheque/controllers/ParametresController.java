package com.ead.bibliotheque.controllers;

import com.ead.bibliotheque.util.ChargementUtil;
import javafx.concurrent.Task;
import javafx.scene.layout.StackPane;
import com.ead.bibliotheque.dao.AdministrateurDAO;
import com.ead.bibliotheque.models.Administrateur;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class ParametresController implements Initializable {

    @FXML private TableView<Administrateur> tableAdmins;
    @FXML private TableColumn<Administrateur, String> colLogin, colNom, colPrenom;
    @FXML private Button btnSupprimer, btnAjouter, btnReinitialiserMdp;
    @FXML private VBox panneauFormulaire;
    @FXML private TextField fieldLogin, fieldNom, fieldPrenom;
    @FXML private PasswordField fieldMdp, fieldMdpConfirm;
    @FXML private Label labelErreur;

    private final AdministrateurDAO dao = new AdministrateurDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colLogin.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getLogin()));
        colNom.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getNom()));
        colPrenom.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getPrenom()));
            tableAdmins.setOnMouseClicked(e -> {
            boolean actif = tableAdmins.getSelectionModel().getSelectedItem() != null;
            btnSupprimer.setDisable(!actif);
            btnReinitialiserMdp.setDisable(!actif);
        });
        charger();
    }

        private void charger() {
        tableAdmins.setItems(FXCollections.observableArrayList(dao.listerTous()));
        btnSupprimer.setDisable(true);
        btnReinitialiserMdp.setDisable(true);
        fermerPanneau();
    }

    @FXML
    private void onReinitialiserMdp() {
        Administrateur sel = tableAdmins.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Réinitialiser le mot de passe");
        dialog.setHeaderText("Nouveau mot de passe pour « " + sel.getLogin() + " »");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        PasswordField mdp = new PasswordField();
        mdp.setPromptText("Nouveau mot de passe");
        PasswordField confirm = new PasswordField();
        confirm.setPromptText("Confirmer le mot de passe");

        VBox box = new VBox(8, mdp, confirm);
        box.setPadding(new javafx.geometry.Insets(12));
        dialog.getDialogPane().setContent(box);

        dialog.showAndWait().ifPresent(r -> {
            if (r != ButtonType.OK) return;
            if (mdp.getText().isBlank()) { err("Le mot de passe ne peut pas être vide."); return; }
            if (!mdp.getText().equals(confirm.getText())) { err("Les mots de passe ne correspondent pas."); return; }

            StackPane root = getContenuPrincipal();
            StackPane overlay = ChargementUtil.creerOverlay();
            root.getChildren().add(overlay);
            ChargementUtil.afficher(overlay);

            Task<Boolean> tache = new Task<>() {
                @Override
                protected Boolean call() {
                    return dao.reinitialiserMotDePasse(sel.getLogin(), mdp.getText());
                }
            };

            tache.setOnSucceeded(e -> {
                root.getChildren().remove(overlay);
                new Alert(Alert.AlertType.INFORMATION, "Mot de passe réinitialisé avec succès.").showAndWait();
            });

            tache.setOnFailed(e -> {
                root.getChildren().remove(overlay);
                err("Erreur lors de la réinitialisation.");
            });

            new Thread(tache).start();
        });
    }

    @FXML
    private void onAjouter() {
        fieldLogin.clear();
        fieldNom.clear();
        fieldPrenom.clear();
        fieldMdp.clear();
        fieldMdpConfirm.clear();
        labelErreur.setVisible(false);
        panneauFormulaire.setVisible(true);
        panneauFormulaire.setManaged(true);
    }

    @FXML
private void onSupprimer() {
    Administrateur sel = tableAdmins.getSelectionModel().getSelectedItem();
    if (sel == null) return;

    new Alert(Alert.AlertType.CONFIRMATION,
            "Supprimer l'admin « " + sel.getLogin() + " » ?",
            ButtonType.YES, ButtonType.NO)
            .showAndWait()
            .ifPresent(r -> {
                if (r != ButtonType.YES) return;

                StackPane root = getContenuPrincipal();
                StackPane overlay = ChargementUtil.creerOverlay();
                root.getChildren().add(overlay);
                ChargementUtil.afficher(overlay);

                Task<Boolean> tache = new Task<>() {
                    @Override
                    protected Boolean call() {
                        return dao.supprimerAdministrateur(sel.getIdAdmin());
                    }
                };

                tache.setOnSucceeded(e -> {
                    root.getChildren().remove(overlay);
                    charger();
                });

                tache.setOnFailed(e -> {
                    root.getChildren().remove(overlay);
                    new Alert(Alert.AlertType.ERROR, "Erreur lors de la suppression.").showAndWait();
                });

                new Thread(tache).start();
            });
}

    @FXML
private void onSauvegarder() {
    if (fieldLogin.getText().isBlank() || fieldMdp.getText().isBlank()) {
        err("Login et mot de passe obligatoires.");
        return;
    }
    if (!fieldMdp.getText().equals(fieldMdpConfirm.getText())) {
        err("Les mots de passe ne correspondent pas.");
        return;
    }

    final String login  = fieldLogin.getText().trim();
    final String mdp    = fieldMdp.getText();
    final String nom    = fieldNom.getText().trim();
    final String prenom = fieldPrenom.getText().trim();

    StackPane root = getContenuPrincipal();
    StackPane overlay = ChargementUtil.creerOverlay();
    root.getChildren().add(overlay);
    ChargementUtil.afficher(overlay);

    Task<Boolean> tache = new Task<>() {
        @Override
        protected Boolean call() {
            return dao.creerAdministrateur(login, mdp, nom, prenom);
        }
    };

    tache.setOnSucceeded(e -> {
        root.getChildren().remove(overlay);
        if (tache.getValue()) {
            charger();
        } else {
            err("Login déjà utilisé ou erreur BDD.");
        }
    });

    tache.setOnFailed(e -> {
        root.getChildren().remove(overlay);
        err("Erreur lors de la création.");
    });

    new Thread(tache).start();
}

    private StackPane getContenuPrincipal() {
    return (StackPane) fieldLogin.getScene().lookup("#contenuPrincipal");
    }

    @FXML
    private void onFermerPanneau() {
        fermerPanneau();
    }

    private void fermerPanneau() {
        panneauFormulaire.setVisible(false);
        panneauFormulaire.setManaged(false);
    }

    private void err(String msg) {
        labelErreur.setText(msg);
        labelErreur.setVisible(true);
    }
}
