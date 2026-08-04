# Gestion de Bibliothèque Scolaire – EAD (Groupe 4)

Application native JavaFX (MVC) pour la gestion d'une bibliothèque scolaire.
Ce dépôt implémente pour l'instant : **l'authentification** et **la gestion des adhérents**,
conformément à la maquette et au cahier des charges (sections 8.1 et 8.2).

## Stack technique

- Java 17
- JavaFX 21 (FXML + Scene Builder)
- PostgreSQL (adapté du CDC, qui prévoyait MySQL — voir note ci-dessous)
- JDBC (PreparedStatement uniquement — protection contre les injections SQL)
- BCrypt (jBCrypt) pour le hachage des mots de passe
- Maven

> **Note sur l'écart avec le CDC** : le cahier des charges (section 9.2) spécifie MySQL 8.x.
> Ce projet utilise PostgreSQL à la demande de l'équipe. Le schéma SQL (`database/schema_postgresql.sql`)
> a été adapté en conséquence (types `SERIAL`, `BOOLEAN`, etc.). Pensez à documenter cet écart
> dans votre rapport final.

## Structure du projet

```
GestionBibliotheque/
├── pom.xml
├── database/
│   └── schema_postgresql.sql       # Script de création des tables
├── src/main/java/com/ead/bibliotheque/
│   ├── MainApp.java                # Point d'entrée JavaFX
│   ├── controllers/
│   │   ├── LoginController.java
│   │   └── MenuPrincipalController.java
│   ├── models/
│   │   ├── Administrateur.java
│   │   └── Adherent.java
│   ├── dao/
│   │   ├── AdministrateurDAO.java
│   │   └── AdherentDAO.java
│   └── util/
│       ├── DatabaseConnection.java
│       ├── SessionManager.java
│       └── InitAdministrateur.java # Script à exécuter une fois pour créer le 1er admin
└── src/main/resources/com/ead/bibliotheque/
    ├── fxml/
    │   ├── login.fxml
    │   └── menu_principal.fxml
    └── css/
        └── styles.css
```

## Installation

### 1. Base de données

PostgreSQL étant déjà installé chez vous :

```bash
psql -U postgres -f database/schema_postgresql.sql
```

Ou bien, dans un client comme pgAdmin / DBeaver : exécutez le contenu du fichier
`database/schema_postgresql.sql` (il crée la base `bibliotheque_ead` et toutes les tables).

### 2. Configuration de la connexion

Ouvrez `src/main/java/com/ead/bibliotheque/util/DatabaseConnection.java` et adaptez si besoin :

```java
private static final String URL = "jdbc:postgresql://localhost:5432/bibliotheque_ead";
private static final String USER = "postgres";
private static final String PASSWORD = "postgres"; // votre mot de passe PostgreSQL
```

### 3. Ouvrir le projet dans IntelliJ

1. `File > Open` → sélectionner le dossier `GestionBibliotheque`
2. IntelliJ doit reconnaître le `pom.xml` et télécharger les dépendances Maven automatiquement
   (JavaFX, driver PostgreSQL, jBCrypt)
3. Vérifiez que le SDK du projet est en Java 17 (`File > Project Structure > SDK`)

### 4. Créer le compte administrateur initial

Exécutez une seule fois la classe `InitAdministrateur.java`
(clic droit sur le fichier → `Run 'InitAdministrateur.main()'`).

Cela crée un compte : **login = `admin`**, **mot de passe = `admin123`**
(mot de passe haché en BCrypt en base — jamais stocké en clair).

### 5. Lancer l'application

Avec Maven :

```bash
mvn clean javafx:run
```

Ou directement depuis IntelliJ en exécutant `MainApp.java` (le plugin JavaFX Maven est configuré
dans le `pom.xml`, mais si vous lancez `MainApp` directement sans passer par `javafx:run`,
ajoutez les VM options JavaFX si IntelliJ le demande — Scene Builder / IntelliJ vous guidera).

## Édition des FXML avec Scene Builder

Les fichiers `login.fxml` et `menu_principal.fxml` sont dans
`src/main/resources/com/ead/bibliotheque/fxml/` et s'ouvrent directement dans Scene Builder
(clic droit sur le fichier dans IntelliJ → `Open in SceneBuilder`, si le plugin est configuré).

## Ce qui est fait / à faire

**Fait (cette itération) :**
- [x] Écran de connexion (authentification sécurisée, BCrypt)
- [x] Menu principal — onglet Gestion des adhérents (liste, recherche, filtre, tri, CRUD)

**À faire (prochaines itérations, selon le CDC section 10.1) :**
- [ ] Gestion du Catalogue de livres (LivreController)
- [ ] Gestion des Emprunts et Retours (EmpruntController) — règles RG-01 à RG-05
- [ ] Tableau de Bord (DashboardController)
- [ ] Historique des emprunts et retours par adhérent
