-- Insert test data for development
-- Password is 'password' hashed with BCrypt

-- Insert a test recruiter
INSERT INTO utilisateurs (nom, email, mot_de_passe, role, actif, date_creation, dtype) VALUES
    ('Test Recruteur', 'recruteur@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'RECRUTEUR', true, NOW(), 'RECRUTEUR');

SET @recruteur_id = LAST_INSERT_ID();

INSERT INTO recruteur (id, entreprise, poste, secteur) VALUES
    (@recruteur_id, 'Tech Corp', 'HR Manager', 'Technology');

INSERT INTO utilisateurs (nom, email, mot_de_passe, role, actif, date_creation, dtype) VALUES
    ('Test Candidat', 'candidat@test.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'CANDIDAT', true, NOW(), 'CANDIDAT');

SET @candidat_id = LAST_INSERT_ID();

INSERT INTO candidat (id, prenom, telephone, niveau_etude) VALUES
    (@candidat_id, 'Jean', '+33123456789', 'Master');
