-- Aligne domaine sur VARCHAR : les noms d'enum (ex. COMMUNICATION, INFORMATIQUE) dépassent souvent un ENUM/VARCHAR MySQL trop court.
ALTER TABLE entretiens MODIFY COLUMN domaine VARCHAR(64) NULL;
-- Tests généraux sans seuil
ALTER TABLE entretiens MODIFY COLUMN seuil_reussite INT NULL;

-- Messagerie bidirectionnelle (recruteur <-> candidat)
ALTER TABLE messages ADD COLUMN IF NOT EXISTS receiver_email VARCHAR(255) NULL;
ALTER TABLE messages ADD COLUMN IF NOT EXISTS receiver_name VARCHAR(255) NULL;

-- Allow NULL sender_id for system notifications (profile incomplete, etc.)
ALTER TABLE notifications MODIFY COLUMN sender_id BIGINT NULL;

