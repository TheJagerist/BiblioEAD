package com.ead.bibliotheque.util;

import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;

/**
 * Overlay de chargement réutilisable (spinner + fond semi-transparent),
 * à empiler par-dessus n'importe quel StackPane (vue principale, panneau latéral...).
 */
public final class ChargementUtil {

    private ChargementUtil() {}

    public static StackPane creerOverlay() {
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setStyle("-fx-progress-color: #D0021B;");
        spinner.setMaxSize(44, 44);

        StackPane overlay = new StackPane(spinner);
        overlay.setStyle("-fx-background-color: rgba(255,255,255,0.65); -fx-background-radius: 16;");
        overlay.setVisible(false);
        overlay.setManaged(false);
        overlay.setPickOnBounds(true);
        return overlay;
    }

    public static void afficher(StackPane overlay) {
        overlay.setVisible(true);
        overlay.setManaged(true);
        overlay.toFront();
    }

    public static void masquer(StackPane overlay) {
        overlay.setVisible(false);
        overlay.setManaged(false);
    }
}