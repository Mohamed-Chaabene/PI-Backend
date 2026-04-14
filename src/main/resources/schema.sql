-- Aligne domaine sur VARCHAR : les noms d'enum (ex. COMMUNICATION, INFORMATIQUE) dépassent souvent un ENUM/VARCHAR MySQL trop court.
ALTER TABLE entretiens MODIFY COLUMN domaine VARCHAR(64) NULL;
-- Tests généraux sans seuil
ALTER TABLE entretiens MODIFY COLUMN seuil_reussite INT NULL;

-- Messagerie bidirectionnelle (recruteur <-> candidat)
ALTER TABLE messages ADD COLUMN IF NOT EXISTS receiver_email VARCHAR(255) NULL;
ALTER TABLE messages ADD COLUMN IF NOT EXISTS receiver_name VARCHAR(255) NULL;

-- Notifications table for real-time notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES utilisateur(id),
    FOREIGN KEY (sender_id) REFERENCES utilisateur(id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);

