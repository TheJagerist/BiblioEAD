package com.ead.bibliotheque.models;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Modèle de projection pour la vue Retards.
 * Pas de table dédiée : les retards sont des emprunts avec statut = 'RETARD'.
 * Cette classe agrège les données nécessaires à l'affichage.
 */
public class Retard {

    public enum Severite { MODERE, ELEVE, CRITIQUE }

    private final IntegerProperty idEmprunt        = new SimpleIntegerProperty();
    private final IntegerProperty idAdherent       = new SimpleIntegerProperty();
    private final IntegerProperty idLivre          = new SimpleIntegerProperty();
    private final StringProperty  nomAdherent      = new SimpleStringProperty();
    private final StringProperty  prenomAdherent   = new SimpleStringProperty();
    private final StringProperty  numCarte         = new SimpleStringProperty();
    private final StringProperty  classe           = new SimpleStringProperty();
    private final StringProperty  titreLivre       = new SimpleStringProperty();
    private final StringProperty  auteurLivre      = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateEmprunt      = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dateRetourPrevue = new SimpleObjectProperty<>();

    public Retard() {}

    public Retard(int idEmprunt, int idAdherent, int idLivre,
                  String nomAdherent, String prenomAdherent, String numCarte, String classe,
                  String titreLivre, String auteurLivre,
                  LocalDate dateEmprunt, LocalDate dateRetourPrevue) {
        setIdEmprunt(idEmprunt);
        setIdAdherent(idAdherent);
        setIdLivre(idLivre);
        setNomAdherent(nomAdherent);
        setPrenomAdherent(prenomAdherent);
        setNumCarte(numCarte);
        setClasse(classe);
        setTitreLivre(titreLivre);
        setAuteurLivre(auteurLivre);
        setDateEmprunt(dateEmprunt);
        setDateRetourPrevue(dateRetourPrevue);
    }

    /** Nombre de jours de retard (positif). */
    public long getJoursRetard() {
        if (dateRetourPrevue.get() == null) return 0;
        return Math.max(0, ChronoUnit.DAYS.between(dateRetourPrevue.get(), LocalDate.now()));
    }

    /** Label affiché dans la colonne Retard : "−X jours". */
    public String getRetardLabel() {
        return "−" + getJoursRetard() + " jours";
    }

    /**
     * Sévérité selon l'ancienneté du retard :
     * < 7 j → MODERE, 7–13 j → ELEVE, ≥ 14 j → CRITIQUE
     */
    public Severite getSeverite() {
        long j = getJoursRetard();
        if (j >= 14) return Severite.CRITIQUE;
        if (j >= 7)  return Severite.ELEVE;
        return Severite.MODERE;
    }

    public String getNomComplet() {
        return nomAdherent.get() + " " + prenomAdherent.get();
    }

    // ── Getters / Setters / Properties ───────────────────────

    public int getIdEmprunt()           { return idEmprunt.get(); }
    public void setIdEmprunt(int v)     { idEmprunt.set(v); }
    public IntegerProperty idEmpruntProperty() { return idEmprunt; }

    public int getIdAdherent()          { return idAdherent.get(); }
    public void setIdAdherent(int v)    { idAdherent.set(v); }
    public IntegerProperty idAdherentProperty() { return idAdherent; }

    public int getIdLivre()             { return idLivre.get(); }
    public void setIdLivre(int v)       { idLivre.set(v); }
    public IntegerProperty idLivreProperty() { return idLivre; }

    public String getNomAdherent()          { return nomAdherent.get(); }
    public void setNomAdherent(String v)    { nomAdherent.set(v); }
    public StringProperty nomAdherentProperty() { return nomAdherent; }

    public String getPrenomAdherent()       { return prenomAdherent.get(); }
    public void setPrenomAdherent(String v) { prenomAdherent.set(v); }
    public StringProperty prenomAdherentProperty() { return prenomAdherent; }

    public String getNumCarte()             { return numCarte.get(); }
    public void setNumCarte(String v)       { numCarte.set(v); }
    public StringProperty numCarteProperty() { return numCarte; }

    public String getClasse()               { return classe.get(); }
    public void setClasse(String v)         { classe.set(v); }
    public StringProperty classeProperty()  { return classe; }

    public String getTitreLivre()           { return titreLivre.get(); }
    public void setTitreLivre(String v)     { titreLivre.set(v); }
    public StringProperty titreLivreProperty() { return titreLivre; }

    public String getAuteurLivre()          { return auteurLivre.get(); }
    public void setAuteurLivre(String v)    { auteurLivre.set(v); }
    public StringProperty auteurLivreProperty() { return auteurLivre; }

    public LocalDate getDateEmprunt()              { return dateEmprunt.get(); }
    public void setDateEmprunt(LocalDate v)        { dateEmprunt.set(v); }
    public ObjectProperty<LocalDate> dateEmpruntProperty() { return dateEmprunt; }

    public LocalDate getDateRetourPrevue()         { return dateRetourPrevue.get(); }
    public void setDateRetourPrevue(LocalDate v)   { dateRetourPrevue.set(v); }
    public ObjectProperty<LocalDate> dateRetourPrevueProperty() { return dateRetourPrevue; }
}
