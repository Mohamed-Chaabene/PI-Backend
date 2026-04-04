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

-- -- ══════════════════════════════════════════════════════════════════
-- -- FORMATIONS
-- -- ══════════════════════════════════════════════════════════════════
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE formation_competence;
-- TRUNCATE TABLE inscription_formation;
-- TRUNCATE TABLE formation;
-- SET FOREIGN_KEY_CHECKS = 1;
--
-- INSERT INTO formation (titre, categorie, plateforme, statut, duree, niveau, lien_externe, youtube_id) VALUES
--
-- -- ══ FRONTEND ══════════════════════════════════════════════════════
-- ('React JS Full Course for Beginners',
--  'Frontend', 'YouTube', 'Disponible', '9h', 'Débutant',
--  'https://www.youtube.com/watch?v=RVFAyFWO4go',
--  'RVFAyFWO4go'),
--
-- ('JavaScript Full Course for Beginners',
--  'Frontend', 'YouTube', 'Disponible', '8h', 'Débutant',
--  'https://www.youtube.com/watch?v=lfmg-EJ8gm4',
--  'lfmg-EJ8gm4'),
--
-- ('CSS Full Course — Flexbox and Grid Tutorial',
--  'Frontend', 'YouTube', 'Disponible', '11h', 'Débutant',
--  'https://www.youtube.com/watch?v=n4R2E7O-Ngo',
--  'n4R2E7O-Ngo'),
--
-- ('Angular Full Course',
--  'Frontend', 'YouTube', 'Disponible', '7h', 'Intermédiaire',
--  'https://www.youtube.com/watch?v=3qBXWUpoPHo',
--  '3qBXWUpoPHo'),
--
-- -- ══ BACKEND ═══════════════════════════════════════════════════════
-- ('Spring Boot Full Course',
--  'Backend', 'YouTube', 'Disponible', '3h', 'Intermédiaire',
--  'https://www.youtube.com/watch?v=9SGDpanrc8U',
--  '9SGDpanrc8U'),
--
-- ('Node.js and Express Full Course',
--  'Backend', 'YouTube', 'Disponible', '8h', 'Intermédiaire',
--  'https://www.youtube.com/watch?v=Oe421EPjeBE',
--  'Oe421EPjeBE'),
--
-- ('Python Full Course for Beginners',
--  'Backend', 'YouTube', 'Disponible', '12h', 'Débutant',
--  'https://www.youtube.com/watch?v=XKHEtdqhLK8',
--  'XKHEtdqhLK8'),
--
-- ('SQL Full Course',
--  'Backend', 'YouTube', 'Disponible', '4h', 'Débutant',
--  'https://www.youtube.com/watch?v=HXV3zeQKqGY',
--  'HXV3zeQKqGY'),
--
-- -- ══ IA ════════════════════════════════════════════════════════════
-- ('Machine Learning Full Course',
--  'IA', 'YouTube', 'Disponible', '10h', 'Intermédiaire',
--  'https://www.youtube.com/watch?v=GwIo3gDZCVQ',
--  'GwIo3gDZCVQ'),
--
-- ('Deep Learning Full Course',
--  'IA', 'YouTube', 'Disponible', '6h', 'Avancé',
--  'https://www.youtube.com/watch?v=aircAruvnKk',
--  'aircAruvnKk'),
--
-- ('TensorFlow Full Course',
--  'IA', 'YouTube', 'Disponible', '7h', 'Avancé',
--  'https://www.youtube.com/watch?v=tPYj3fFJGjk',
--  'tPYj3fFJGjk'),
--
-- -- ══ DATA ══════════════════════════════════════════════════════════
-- ('Python for Data Science Full Course',
--  'Data', 'YouTube', 'Disponible', '12h', 'Intermédiaire',
--  'https://www.youtube.com/watch?v=LHBE6Q9oYEs',
--  'LHBE6Q9oYEs'),
--
-- ('Data Analysis with Pandas Full Course',
--  'Data', 'YouTube', 'Disponible', '4h', 'Intermédiaire',
--  'https://www.youtube.com/watch?v=vmEHCJofslg',
--  'vmEHCJofslg'),
--
-- -- ══ DEVOPS ════════════════════════════════════════════════════════
-- ('Docker Full Course for Beginners',
--  'DevOps', 'YouTube', 'Disponible', '4h', 'Intermédiaire',
--  'https://www.youtube.com/watch?v=fqMOX6JJhGo',
--  'fqMOX6JJhGo'),
--
-- ('Kubernetes Full Course',
--  'DevOps', 'YouTube', 'Disponible', '4h', 'Avancé',
--  'https://www.youtube.com/watch?v=X48VuDVv0do',
--  'X48VuDVv0do'),
--
-- ('AWS Full Course',
--  'DevOps', 'YouTube', 'Disponible', '5h', 'Avancé',
--  'https://www.youtube.com/watch?v=ZB5ONbD_SMY',
--  'ZB5ONbD_SMY'),
--
-- -- ══ DESIGN ════════════════════════════════════════════════════════
-- ('Figma Full Course for Beginners',
--  'Design', 'YouTube', 'Disponible', '3h', 'Débutant',
--  'https://www.youtube.com/watch?v=kbZejnPXyLM',
--  'kbZejnPXyLM'),
--
-- ('UI/UX Design Full Course',
--  'Design', 'YouTube', 'Disponible', '5h', 'Débutant',
--  'https://www.youtube.com/watch?v=c9Wg6Cb_YlU',
--  'c9Wg6Cb_YlU'),

-- -- ══ DÉVELOPPEMENT MOBILE ══════════════════════════════════════════
-- ('Flutter Full Course for Beginners',
--  'Développement', 'YouTube', 'Disponible', '6h', 'Intermédiaire',
--  'https://www.youtube.com/watch?v=VPvVD8t02U8',
--  'VPvVD8t02U8'),

-- ('React Native Full Course',
--  'Développement', 'YouTube', 'Disponible', '5h', 'Intermédiaire',
--  'https://www.youtube.com/watch?v=0-S5a0eXPoc',
--  '0-S5a0eXPoc');
