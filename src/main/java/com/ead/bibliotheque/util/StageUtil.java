package com.ead.bibliotheque.util;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.List;

/**
 * Ajuste la fenêtre principale pour rester dans la zone visible de l'écran
 * (évite le débordement lié au style TRANSPARENT + maximize natif).
 */
public final class StageUtil {

    private StageUtil() {}

    public static void configurerLimites(Stage stage) {
        Rectangle2D bounds = zoneVisible(stage);
        stage.setMaxWidth(bounds.getWidth());
        stage.setMaxHeight(bounds.getHeight());
    }

    /** Utilise le maximize natif du Stage : seule méthode fiable avec StageStyle.TRANSPARENT. */
    public static void etendreDansEcran(Stage stage) {
        stage.setMaximized(true);
    }

    public static void restaurerTailleStandard(Stage stage) {
        stage.setMaximized(false);
        stage.setWidth(1280);
        stage.setHeight(800);
        stage.centerOnScreen();
    }

    /** Retourne les limites de l'écran où se trouve réellement la fenêtre (pas forcément l'écran principal). */
    private static Rectangle2D zoneVisible(Stage stage) {
        List<Screen> ecrans = Screen.getScreensForRectangle(
                stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
        Screen ecran = ecrans.isEmpty() ? Screen.getPrimary() : ecrans.get(0);
        return ecran.getVisualBounds();
    }
}