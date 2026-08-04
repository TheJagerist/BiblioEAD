package com.ead.bibliotheque.util;

import com.ead.bibliotheque.dao.AdministrateurDAO;

/**
 * Classe à exécuter UNE SEULE FOIS (clic droit > Run dans IntelliJ) pour créer
 * le compte administrateur initial avec un mot de passe correctement haché en BCrypt.
 * Ne fait pas partie de l'IHM : c'est un script d'initialisation.
 */
public class InitAdministrateur {

    public static void main(String[] args) {
        AdministrateurDAO dao = new AdministrateurDAO();

        String login = "admin";
        String motDePasse = "admin123"; // à changer avant la mise en production
        String nom = "Bibliothécaire";
        String prenom = "EAD";

        boolean succes = dao.creerAdministrateur(login, motDePasse, nom, prenom);

        if (succes) {
            System.out.println("Administrateur créé avec succès : login=" + login + ", mot de passe=" + motDePasse);
        } else {
            System.out.println("Échec de la création. Vérifiez que la base de données est démarrée " +
                    "et que la table 'administrateurs' existe (voir database/schema_postgresql.sql).");
        }
    }
}
