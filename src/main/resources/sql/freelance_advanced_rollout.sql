-- Freelance Advanced Rollout - schema migration helper
-- Execute manually when running with managed DB migrations.

ALTER TABLE utilisateur
    ADD COLUMN IF NOT EXISTS email_verified BIT(1) DEFAULT b'0',
    ADD COLUMN IF NOT EXISTS identity_verification_status VARCHAR(32) DEFAULT 'UNVERIFIED',
    ADD COLUMN IF NOT EXISTS email_verification_token VARCHAR(255),
    ADD COLUMN IF NOT EXISTS email_verification_expiry DATETIME;

ALTER TABLE fl_missions
    ADD COLUMN IF NOT EXISTS experience_level VARCHAR(64),
    ADD COLUMN IF NOT EXISTS location VARCHAR(128),
    ADD COLUMN IF NOT EXISTS remote_available BIT(1),
    ADD COLUMN IF NOT EXISTS availability VARCHAR(64);

CREATE TABLE IF NOT EXISTS fl_availability_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    freelancer_id BIGINT NOT NULL,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    booked BIT(1) NOT NULL DEFAULT b'0',
    booked_by_user_id BIGINT NULL,
    created_at DATETIME NULL,
    CONSTRAINT fk_availability_freelancer FOREIGN KEY (freelancer_id) REFERENCES utilisateur(id)
);

CREATE TABLE IF NOT EXISTS fl_invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    payment_id BIGINT NULL,
    client_id BIGINT NOT NULL,
    freelancer_id BIGINT NOT NULL,
    invoice_number VARCHAR(128) NOT NULL UNIQUE,
    subtotal DOUBLE,
    vat_rate DOUBLE,
    vat_amount DOUBLE,
    total_amount DOUBLE,
    status VARCHAR(32),
    created_at DATETIME,
    CONSTRAINT fk_invoice_contract FOREIGN KEY (contract_id) REFERENCES freelance_contract(id),
    CONSTRAINT fk_invoice_payment FOREIGN KEY (payment_id) REFERENCES freelance_payment(id),
    CONSTRAINT fk_invoice_client FOREIGN KEY (client_id) REFERENCES utilisateur(id),
    CONSTRAINT fk_invoice_freelancer FOREIGN KEY (freelancer_id) REFERENCES utilisateur(id)
);
