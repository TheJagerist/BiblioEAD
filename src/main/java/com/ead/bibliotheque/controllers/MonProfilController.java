package com.ead.bibliotheque.controllers;

import com.ead.bibliotheque.dao.AdherentDAO;
import com.ead.bibliotheque.dao.AdministrateurDAO;
import com.ead.bibliotheque.dao.EmpruntDAO;
import com.ead.bibliotheque.dao.LivreDAO;
import com.ead.bibliotheque.models.Administrateur;
import com.ead.bibliotheque.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class MonProfilController implements Initializable {

    @FXML private Label labelAvatarGrand;
    @FXML private Label labelNomComplet;
    @FXML private Label labelLoginProfil;
    @FXML private Label labelIdProfil;

    @FXML private Label statCreeLe;
    @FXML private Label statTotalEmprunts;
    @FXML private Label statTotalRetours;
    @FXML private Label statTotalAdherents;
    @FXML private Label statTotalLivres;

    @FXML private TextField fieldNouveauLogin;
    @FXML private Label labelErreurLogin;
    @FXML private Label labelSuccesLogin;

    @FXML private PasswordField fieldAncienMdp;
    @FXML private PasswordField fieldNouveauMdp;
    @FXML private PasswordField fieldConfirmMdp;
    @FXML private Label labelErreurMdp;
    @FXML private Label labelSuccesMdp;

    private final AdministrateurDAO dao         = new AdministrateurDAO();
    private final EmpruntDAO        empruntDAO  = new EmpruntDAO();
    private final AdherentDAO       adherentDAO = new AdherentDAO();
    private final LivreDAO          livreDAO    = new LivreDAO();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chargerProfil();
    }

    private void chargerProfil() {
        Administrateur admin = SessionManager.getAdministrateurConnecte();
        if (admin == null) return;

        String nom    = admin.getNom()    != null ? admin.getNom().trim()    : "";
        String prenom = admin.getPrenom() != null ? admin.getPrenom().trim() : "";

        String initiales = "";
        if (!nom.isBlank())    initiales += nom.substring(0, 1).toUpperCase();
        if (!prenom.isBlank()) initiales += prenom.substring(0, 1).toUpperCase();
        if (initiales.isBlank())
            initiales = admin.getLogin().substring(0, Math.min(2, admin.getLogin().length())).toUpperCase();

        labelAvatarGrand.setText(initiales);
        String nomComplet = (nom + " " + prenom).trim();
        labelNomComplet.setText(nomComplet.isBlank() ? admin.getLogin() : nomComplet);
        labelLoginProfil.setText("@" + admin.getLogin());
        labelIdProfil.setText("ID : " + admin.getIdAdmin());
        fieldNouveauLogin.setText(admin.getLogin());

        statCreeLe.setText(admin.getCreeLe() != null ? admin.getCreeLe().format(FMT) : "—");
        statTotalEmprunts.setText(String.valueOf(empruntDAO.compterTotalEmprunts()));
        statTotalRetours.setText(String.valueOf(empruntDAO.compterTotalRetours()));
        statTotalAdherents.setText(String.valueOf(adherentDAO.listerTous().size()));
        statTotalLivres.setText(String.valueOf(livreDAO.getTotalExemplaires()));
    }

    @FXML
    private void onSauvegarderLogin() {
        cacherFeedbackLogin();
        String nouveau = fieldNouveauLogin.getText().trim();
        if (nouveau.isBlank()) { erreurLogin("Le login ne peut pas être vide."); return; }
        Administrateur admin = SessionManager.getAdministrateurConnecte();
        if (admin == null) return;
        if (nouveau.equals(admin.getLogin())) { erreurLogin("C'est déjà votre login actuel."); return; }
        if (!dao.mettreAJourLogin(admin.getIdAdmin(), nouveau)) {
            erreurLogin("Login déjà utilisé ou erreur base de données."); return;
        }
        admin.setLogin(nouveau);
        labelLoginProfil.setText("@" + nouveau);
        succes(labelSuccesLogin, "Login modifié avec succès.");
    }

    @FXML
    private void onSauvegarderMdp() {
        cacherFeedbackMdp();
        String ancien  = fieldAncienMdp.getText();
        String nouveau = fieldNouveauMdp.getText();
        String confirm = fieldConfirmMdp.getText();
        if (ancien.isBlank() || nouveau.isBlank()) {
            erreurMdp("Tous les champs sont obligatoires."); return;
        }
        if (!nouveau.equals(confirm)) {
            erreurMdp("Les nouveaux mots de passe ne correspondent pas."); return;
        }
        if (nouveau.length() < 6) {
            erreurMdp("Le mot de passe doit contenir au moins 6 caractères."); return;
        }
        Administrateur admin = SessionManager.getAdministrateurConnecte();
        if (admin == null) return;
        if (!dao.changerMotDePasse(admin.getIdAdmin(), ancien, nouveau)) {
            erreurMdp("Mot de passe actuel incorrect."); return;
        }
        fieldAncienMdp.clear(); fieldNouveauMdp.clear(); fieldConfirmMdp.clear();
        succes(labelSuccesMdp, "Mot de passe changé avec succès.");
    }

    private void erreurLogin(String msg) {
        labelErreurLogin.setText(msg);
        labelErreurLogin.setVisible(true); labelErreurLogin.setManaged(true);
    }
    private void erreurMdp(String msg) {
        labelErreurMdp.setText(msg);
        labelErreurMdp.setVisible(true); labelErreurMdp.setManaged(true);
    }
    private void succes(Label label, String msg) {
        label.setText(msg); label.setVisible(true); label.setManaged(true);
    }
    private void cacherFeedbackLogin() {
        labelErreurLogin.setVisible(false); labelErreurLogin.setManaged(false);
        labelSuccesLogin.setVisible(false); labelSuccesLogin.setManaged(false);
    }
    private void cacherFeedbackMdp() {
        labelErreurMdp.setVisible(false); labelErreurMdp.setManaged(false);
        labelSuccesMdp.setVisible(false); labelSuccesMdp.setManaged(false);
    }
}
