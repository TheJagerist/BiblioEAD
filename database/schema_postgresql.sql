-- ============================================================
--  BiblioEAD — Schéma PostgreSQL complet
--  Groupe 4 · GL3 · EAD
-- ============================================================

-- Extensions
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ── Tables ──────────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS administrateurs (
    id_admin      SERIAL PRIMARY KEY,
    login         VARCHAR(50)  UNIQUE NOT NULL,
    mot_de_passe  VARCHAR(100) NOT NULL,          -- hash BCrypt
    nom           VARCHAR(100),
    prenom        VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS adherents (
    id_adherent      SERIAL PRIMARY KEY,
    nom              VARCHAR(100) NOT NULL,
    prenom           VARCHAR(100) NOT NULL,
    classe           VARCHAR(20)  NOT NULL,
    filiere          VARCHAR(100),
    num_carte        VARCHAR(20)  UNIQUE NOT NULL,
    date_inscription DATE         NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE IF NOT EXISTS livres (
    id_livre                SERIAL PRIMARY KEY,
    titre                   VARCHAR(200) NOT NULL,
    auteur                  VARCHAR(150) NOT NULL,
    genre                   VARCHAR(80),
    annee                   INTEGER,
    isbn                    VARCHAR(20),
    nombre_exemplaires      INTEGER NOT NULL DEFAULT 1,
    exemplaires_disponibles INTEGER NOT NULL DEFAULT 1,
    date_ajout              DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE IF NOT EXISTS emprunts (
    id_emprunt             SERIAL PRIMARY KEY,
    id_adherent            INTEGER NOT NULL REFERENCES adherents(id_adherent) ON DELETE RESTRICT,
    id_livre               INTEGER NOT NULL REFERENCES livres(id_livre)    ON DELETE RESTRICT,
    date_emprunt           DATE    NOT NULL DEFAULT CURRENT_DATE,
    date_retour_prevue     DATE    NOT NULL,
    date_retour_effectif   DATE,                        -- NULL = non rendu
    statut                 VARCHAR(10) NOT NULL DEFAULT 'EN_COURS'
                               CHECK (statut IN ('EN_COURS', 'RENDU', 'RETARD'))
);

-- ── Index utiles ─────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_emprunts_statut      ON emprunts(statut);
CREATE INDEX IF NOT EXISTS idx_emprunts_id_adherent ON emprunts(id_adherent);
CREATE INDEX IF NOT EXISTS idx_adherents_num_carte  ON adherents(num_carte);

-- ── Données de test (optionnel, commentez si vous voulez partir vide) ──
-- Quelques livres
INSERT INTO livres (titre, auteur, genre, annee, isbn, nombre_exemplaires, exemplaires_disponibles)
VALUES
  ('Les Misérables',           'V. Hugo',        'Littérature', 1862, '978-2-07-040850-4', 4, 4),
  ('L''Étranger',              'A. Camus',       'Littérature', 1942, '978-2-07-036024-5', 3, 3),
  ('Germinal',                 'É. Zola',        'Littérature', 1885, '978-2-07-040250-2', 2, 2),
  ('Algorithmes',              'T. Cormen',      'Informatique',2022, '978-0-26-204630-5', 2, 2),
  ('Génie Logiciel',           'I. Sommerville', 'Informatique',2019, '978-0-13-703515-1', 4, 4),
  ('UML 2 par la Pratique',    'P. Roques',      'Informatique',2021, '978-2-10-083521-3', 2, 2),
  ('Le Petit Prince',          'A. de Saint-Ex', 'Littérature', 1943, '978-2-07-040850-5', 3, 3),
  ('Le Comte de Monte-Cristo', 'A. Dumas',       'Littérature', 1844, '978-2-07-036822-7', 3, 3)
ON CONFLICT DO NOTHING;