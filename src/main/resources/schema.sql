-- Aligne domaine sur VARCHAR : les noms d'enum (ex. COMMUNICATION, INFORMATIQUE) dépassent souvent un ENUM/VARCHAR MySQL trop court.
ALTER TABLE entretiens MODIFY COLUMN domaine VARCHAR(64) NULL;
-- Tests généraux sans seuil
ALTER TABLE entretiens MODIFY COLUMN seuil_reussite INT NULL;
