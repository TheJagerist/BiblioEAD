package com.ead.bibliotheque;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {

    private static Stage stagePrincipal;

    @Override
    public void start(Stage stage) throws IOException {
        stagePrincipal = stage;
        stage.setTitle("Gestion de Bibliothèque Scolaire - EAD");
        stage.setWidth(1280);
        stage.setHeight(800);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.centerOnScreen();
        afficherConnexion();
        stage.show();
    }

    public static void afficherConnexion() throws IOException {
        changerScene("/com/ead/bibliotheque/fxml/login.fxml", "Connexion");
    }

    public static void afficherMenuPrincipal() throws IOException {
        stagePrincipal.setMaximized(true);
        changerScene("/com/ead/bibliotheque/fxml/menu_principal.fxml", "Menu Principal - Gestion des adhérents");
    }

    private static void changerScene(String fxmlPath, String titre) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
        Parent racine = loader.load();
        Scene scene = new Scene(racine);
        scene.getStylesheets().add(
                Objects.requireNonNull(MainApp.class.getResource("/com/ead/bibliotheque/css/styles.css")).toExternalForm());
        stagePrincipal.setTitle("Gestion de Bibliothèque Scolaire - EAD | " + titre);
        stagePrincipal.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}