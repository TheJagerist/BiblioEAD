package com.ead.bibliotheque;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import com.ead.bibliotheque.util.StageUtil;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class MainApp extends Application {

    private static Stage stagePrincipal;

    @Override
    public void start(Stage stage) throws IOException {
        stagePrincipal = stage;
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Gestion de Bibliothèque Scolaire - EAD");
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setMaximized(true);
        StageUtil.configurerLimites(stage);
        stage.setMaximized(true);
        chargerPolices();
        afficherConnexion();
        stage.show();
    }

    public static void afficherConnexion() throws IOException {
        changerScene("/com/ead/bibliotheque/fxml/login.fxml", "Connexion");
    }

    public static void afficherMenuPrincipal() throws IOException {
        StageUtil.configurerLimites(stagePrincipal);
        changerScene("/com/ead/bibliotheque/fxml/menu_principal.fxml", "Menu Principal - Gestion des adhérents");
        StageUtil.etendreDansEcran(stagePrincipal);
    }

    private static void changerScene(String fxmlPath, String titre) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
        Parent racine = loader.load();
        Scene scene = new Scene(racine);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(
                Objects.requireNonNull(MainApp.class.getResource("/com/ead/bibliotheque/css/styles.css")).toExternalForm());
        stagePrincipal.setTitle("Gestion de Bibliothèque Scolaire - EAD | " + titre);
        stagePrincipal.setScene(scene);
    }

    /** Charge les polices embarquées avant le rendu CSS, sans dépendre de @font-face. */
    private static void chargerPolices() {
        chargerPolice("/com/ead/bibliotheque/fonts/Fraunces-VariableFont_SOFT,WONK,opsz,wght.ttf");
        chargerPolice("/com/ead/bibliotheque/fonts/PublicSans-VariableFont_wght.ttf");
    }

    private static void chargerPolice(String cheminRessource) {
        try (InputStream police = MainApp.class.getResourceAsStream(cheminRessource)) {
            if (police != null) {
                Font.loadFont(police, 12);
            }
        } catch (IOException ignored) {
            // Une police indisponible ne doit jamais empêcher l'application de démarrer.
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
