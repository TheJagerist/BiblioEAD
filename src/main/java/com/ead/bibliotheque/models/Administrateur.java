package com.ead.bibliotheque.models;

public class Administrateur {

    private int idAdmin;
    private String login;
    private String motDePasse; // hash BCrypt, jamais le mot de passe en clair
    private String nom;
    private String prenom;
    private java.time.LocalDateTime creeLe;

    public Administrateur() {
    }

    public Administrateur(int idAdmin, String login, String motDePasse, String nom, String prenom) {
        this.idAdmin = idAdmin;
        this.login = login;
        this.motDePasse = motDePasse;
        this.nom = nom;
        this.prenom = prenom;
    }

    public int getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(int idAdmin) {
        this.idAdmin = idAdmin;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public java.time.LocalDateTime getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(java.time.LocalDateTime creeLe) {
        this.creeLe = creeLe;
    }
}
