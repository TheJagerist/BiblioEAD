package com.ead.bibliotheque.models;

import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * Modèle Adhérent, conforme au CDC (section 10.3) :
 * id_adherent, nom, prenom, classe, num_carte (UNIQUE), date_inscription
 * Utilise des propriétés JavaFX pour un binding direct avec le TableView.
 */
public class Adherent {

    private final IntegerProperty idAdherent = new SimpleIntegerProperty();
    private final StringProperty nom = new SimpleStringProperty();
    private final StringProperty filiere = new SimpleStringProperty();
    private final StringProperty prenom = new SimpleStringProperty();
    private final StringProperty classe = new SimpleStringProperty();
    private final StringProperty numCarte = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateInscription = new SimpleObjectProperty<>();
    private final StringProperty modifiePar = new SimpleStringProperty();
    private final javafx.beans.property.ObjectProperty<java.time.LocalDateTime> modifieLe = new javafx.beans.property.SimpleObjectProperty<>();

    public String getModifiePar() { return modifiePar.get(); }
    public void setModifiePar(String v) { modifiePar.set(v); }
    public StringProperty modifieParProperty() { return modifiePar; }

    public java.time.LocalDateTime getModifieLe() { return modifieLe.get(); }
    public void setModifieLe(java.time.LocalDateTime v) { modifieLe.set(v); }
    public javafx.beans.property.ObjectProperty<java.time.LocalDateTime> modifieLeProperty() { return modifieLe; }

    public Adherent() {
    }

    public Adherent(int idAdherent, String nom, String prenom, String classe,
                    String filiere, String numCarte, LocalDate dateInscription) {
        setIdAdherent(idAdherent);
        setNom(nom);
        setPrenom(prenom);
        setClasse(classe);
        setFiliere(filiere);
        setNumCarte(numCarte);
        setDateInscription(dateInscription);
    }

    public int getIdAdherent() {
        return idAdherent.get();
    }

    public void setIdAdherent(int value) {
        idAdherent.set(value);
    }

    public IntegerProperty idAdherentProperty() {
        return idAdherent;
    }

    public String getNom() {
        return nom.get();
    }

    public void setNom(String value) {
        nom.set(value);
    }

    public StringProperty nomProperty() {
        return nom;
    }

    public String getPrenom() {
        return prenom.get();
    }

    public void setPrenom(String value) {
        prenom.set(value);
    }

    public StringProperty prenomProperty() {
        return prenom;
    }

    public String getClasse() {
        return classe.get();
    }

    public void setClasse(String value) {
        classe.set(value);
    }

    public StringProperty classeProperty() {
        return classe;
    }

    public String getNumCarte() {
        return numCarte.get();
    }

    public void setNumCarte(String value) {
        numCarte.set(value);
    }

    public StringProperty numCarteProperty() {
        return numCarte;
    }

    public LocalDate getDateInscription() {
        return dateInscription.get();
    }

    public void setDateInscription(LocalDate value) {
        dateInscription.set(value);
    }

    public ObjectProperty<LocalDate> dateInscriptionProperty() {
        return dateInscription;
    }

    public String getFiliere() { return filiere.get(); }

    public void setFiliere(String value) { filiere.set(value); }

    public StringProperty filiereProperty() { return filiere; }
}
