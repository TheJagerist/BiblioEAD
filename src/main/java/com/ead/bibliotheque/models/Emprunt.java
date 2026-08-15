package com.ead.bibliotheque.models;

import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * Modèle Emprunt conforme au CDC (section 10.3).
 * Colonnes BDD : id_emprunt, id_adherent, id_livre, date_emprunt, date_retour_prevue,
 *                date_retour_effectif (nullable), statut (EN_COURS | RENDU | RETARD)
 */
public class Emprunt {

    public enum Statut { EN_COURS, RENDU, RETARD }

    private final IntegerProperty idEmprunt          = new SimpleIntegerProperty();
    private final IntegerProperty idAdherent         = new SimpleIntegerProperty();
    private final IntegerProperty idLivre            = new SimpleIntegerProperty();
    private final StringProperty  nomAdherent        = new SimpleStringProperty();
    private final StringProperty  numCarteAdherent   = new SimpleStringProperty();
    private final StringProperty  titreLivre         = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateEmprunt        = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dateRetourPrevue   = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dateRetourEffectif = new SimpleObjectProperty<>();
    private final ObjectProperty<Statut>    statut             = new SimpleObjectProperty<>();
    private final StringProperty creePar = new SimpleStringProperty();

    public String getCreePar() { return creePar.get(); }
    public void setCreePar(String v) { creePar.set(v); }
    public StringProperty creeParProperty() { return creePar; }

    public Emprunt() {}

    public Emprunt(int idEmprunt, int idAdherent, int idLivre,
                   String nomAdherent, String numCarteAdherent, String titreLivre,
                   LocalDate dateEmprunt, LocalDate dateRetourPrevue,
                   LocalDate dateRetourEffectif, Statut statut) {
        setIdEmprunt(idEmprunt);
        setIdAdherent(idAdherent);
        setIdLivre(idLivre);
        setNomAdherent(nomAdherent);
        setNumCarteAdherent(numCarteAdherent);
        setTitreLivre(titreLivre);
        setDateEmprunt(dateEmprunt);
        setDateRetourPrevue(dateRetourPrevue);
        setDateRetourEffectif(dateRetourEffectif);
        setStatut(statut);
    }

    /** Retourne les jours restants (négatif = retard). */
    public long getJoursRestants() {
        if (dateRetourPrevue.get() == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dateRetourPrevue.get());
    }

    /** Label lisible pour la colonne "Jours restants". */
    public String getJoursLabel() {
        long j = getJoursRestants();
        return (j >= 0 ? "+" : "") + j + " j";
    }

    // ── Getters / Setters / Properties ───────────────────────

    public int getIdEmprunt()          { return idEmprunt.get(); }
    public void setIdEmprunt(int v)    { idEmprunt.set(v); }
    public IntegerProperty idEmpruntProperty() { return idEmprunt; }

    public int getIdAdherent()         { return idAdherent.get(); }
    public void setIdAdherent(int v)   { idAdherent.set(v); }
    public IntegerProperty idAdherentProperty() { return idAdherent; }

    public int getIdLivre()            { return idLivre.get(); }
    public void setIdLivre(int v)      { idLivre.set(v); }
    public IntegerProperty idLivreProperty() { return idLivre; }

    public String getNomAdherent()           { return nomAdherent.get(); }
    public void setNomAdherent(String v)     { nomAdherent.set(v); }
    public StringProperty nomAdherentProperty()  { return nomAdherent; }

    public String getNumCarteAdherent()          { return numCarteAdherent.get(); }
    public void setNumCarteAdherent(String v)    { numCarteAdherent.set(v); }
    public StringProperty numCarteAdherentProperty() { return numCarteAdherent; }

    public String getTitreLivre()            { return titreLivre.get(); }
    public void setTitreLivre(String v)      { titreLivre.set(v); }
    public StringProperty titreLivreProperty()   { return titreLivre; }

    public LocalDate getDateEmprunt()              { return dateEmprunt.get(); }
    public void setDateEmprunt(LocalDate v)        { dateEmprunt.set(v); }
    public ObjectProperty<LocalDate> dateEmpruntProperty()      { return dateEmprunt; }

    public LocalDate getDateRetourPrevue()         { return dateRetourPrevue.get(); }
    public void setDateRetourPrevue(LocalDate v)   { dateRetourPrevue.set(v); }
    public ObjectProperty<LocalDate> dateRetourPrevueProperty() { return dateRetourPrevue; }

    public LocalDate getDateRetourEffectif()       { return dateRetourEffectif.get(); }
    public void setDateRetourEffectif(LocalDate v) { dateRetourEffectif.set(v); }
    public ObjectProperty<LocalDate> dateRetourEffectifProperty() { return dateRetourEffectif; }

    public Statut getStatut()            { return statut.get(); }
    public void setStatut(Statut v)      { statut.set(v); }
    public ObjectProperty<Statut> statutProperty() { return statut; }
}
