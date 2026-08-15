package com.ead.bibliotheque.controllers;

import com.ead.bibliotheque.dao.AdministrateurDAO;
import com.ead.bibliotheque.models.Administrateur;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.util.ResourceBundle;

public class ParametresController implements Initializable {

    @FXML private TableView<Administrateur> tableAdmins;
    @FXML private TableColumn<Administrateur, String> colLogin, colNom, colPrenom;
    @FXML private Button btnSupprimer, btnAjouter;
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
        tableAdmins.setOnMouseClicked(e ->
                btnSupprimer.setDisable(tableAdmins.getSelectionModel().getSelectedItem() == null));
        charger();
    }

    private void charger() {
        tableAdmins.setItems(FXCollections.observableArrayList(dao.listerTous()));
        btnSupprimer.setDisable(true);
        fermerPanneau();
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
                    if (r == ButtonType.YES) {
                        dao.supprimerAdministrateur(sel.getIdAdmin());
                        charger();
                    }
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
        boolean ok = dao.creerAdministrateur(
                fieldLogin.getText().trim(),
                fieldMdp.getText(),
                fieldNom.getText().trim(),
                fieldPrenom.getText().trim());
        if (!ok) {
            err("Login déjà utilisé ou erreur BDD.");
            return;
        }
        charger();
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
