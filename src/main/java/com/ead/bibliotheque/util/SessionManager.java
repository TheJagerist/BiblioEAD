package com.ead.bibliotheque.util;

import com.ead.bibliotheque.models.Administrateur;

/**
 * Conserve l'administrateur authentifié pendant la durée de la session (RG-06).
 */
public class SessionManager {

    private static Administrateur administrateurConnecte;

    private SessionManager() {
    }

    public static void connecter(Administrateur admin) {
        administrateurConnecte = admin;
    }

    public static Administrateur getAdministrateurConnecte() {
        return administrateurConnecte;
    }

    public static boolean estConnecte() {
        return administrateurConnecte != null;
    }

    public static void deconnecter() {
        administrateurConnecte = null;
    }
}
