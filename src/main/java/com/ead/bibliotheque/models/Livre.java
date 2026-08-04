package com.ead.bibliotheque.models;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Livre {

    private final IntegerProperty idLivre            = new SimpleIntegerProperty();
    private final StringProperty  titre              = new SimpleStringProperty();
    private final StringProperty  auteur             = new SimpleStringProperty();
    private final StringProperty  genre              = new SimpleStringProperty();
    private final ObjectProperty<Integer>    annee   = new SimpleObjectProperty<>();
    private final StringProperty  isbn              = new SimpleStringProperty();
    private final IntegerProperty nombreExemplaires  = new SimpleIntegerProperty();
    private final IntegerProperty exemplairesDisponibles = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> dateAjout = new SimpleObjectProperty<>();
    private final StringProperty imageCouverture = new SimpleStringProperty();

    public Livre() {}

    public Livre(int idLivre, String titre, String auteur, String genre,
                 Integer annee, String isbn, int nombreExemplaires,
                 int exemplairesDisponibles, LocalDate dateAjout, String imageCouverture) {
        setIdLivre(idLivre);
        setTitre(titre);
        setAuteur(auteur);
        setGenre(genre);
        setAnnee(annee);
        setIsbn(isbn);
        setNombreExemplaires(nombreExemplaires);
        setExemplairesDisponibles(exemplairesDisponibles);
        setDateAjout(dateAjout);
        setImageCouverture(imageCouverture);
    }

    /** Utilisé par la colonne "Dispo" du TableView */
    public String getDispoLabel() {
        return exemplairesDisponibles.get() + " / " + nombreExemplaires.get();
    }

    /** Vrai si le livre a été ajouté dans les 7 derniers jours (RG "Nouveau") */
    public boolean isNouveaute() {
        LocalDate d = dateAjout.get();
        return d != null && d.isAfter(LocalDate.now().minusDays(7));
    }

    // ── Getters / Setters ─────────────────────────────────────
    public int getIdLivre()                        { return idLivre.get(); }
    public void setIdLivre(int v)                  { idLivre.set(v); }
    public IntegerProperty idLivreProperty()       { return idLivre; }

    public String getTitre()                       { return titre.get(); }
    public void setTitre(String v)                 { titre.set(v); }
    public StringProperty titreProperty()          { return titre; }

    public String getAuteur()                      { return auteur.get(); }
    public void setAuteur(String v)                { auteur.set(v); }
    public StringProperty auteurProperty()         { return auteur; }

    public String getGenre()                       { return genre.get(); }
    public void setGenre(String v)                 { genre.set(v); }
    public StringProperty genreProperty()          { return genre; }

    public Integer getAnnee()                      { return annee.get(); }
    public void setAnnee(Integer v)                { annee.set(v); }
    public ObjectProperty<Integer> anneeProperty() { return annee; }

    public String getIsbn()                        { return isbn.get(); }
    public void setIsbn(String v)                  { isbn.set(v); }
    public StringProperty isbnProperty()           { return isbn; }

    public int getNombreExemplaires()              { return nombreExemplaires.get(); }
    public void setNombreExemplaires(int v)        { nombreExemplaires.set(v); }
    public IntegerProperty nombreExemplairesProperty() { return nombreExemplaires; }

    public int getExemplairesDisponibles()         { return exemplairesDisponibles.get(); }
    public void setExemplairesDisponibles(int v)   { exemplairesDisponibles.set(v); }
    public IntegerProperty exemplairesDisponiblesProperty() { return exemplairesDisponibles; }

    public LocalDate getDateAjout()                { return dateAjout.get(); }
    public void setDateAjout(LocalDate v)          { dateAjout.set(v); }
    public ObjectProperty<LocalDate> dateAjoutProperty() { return dateAjout; }

    public String getImageCouverture()           { return imageCouverture.get(); }
    public void setImageCouverture(String v)     { imageCouverture.set(v); }
    public StringProperty imageCouvertureProperty() { return imageCouverture; }
}