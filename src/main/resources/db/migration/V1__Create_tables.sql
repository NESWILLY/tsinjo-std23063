-- Table des utilisateurs (donateurs et bénéficiaires)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       email VARCHAR(255) NOT NULL UNIQUE,
                       full_name VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des paiements
CREATE TABLE payments (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          psp_payment_id VARCHAR(255) NOT NULL UNIQUE,
                          psp_type VARCHAR(50) NOT NULL DEFAULT 'ORANGE_MONEY',
                          amount INTEGER,
                          payer_email VARCHAR(255) NOT NULL,
                          verification_status VARCHAR(50) NOT NULL DEFAULT 'VERIFYING',
                          verification_attempt_nb INTEGER DEFAULT 0,
                          creation_instant TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          last_verification_instant TIMESTAMP,
                          CONSTRAINT fk_payment_payer FOREIGN KEY (payer_email) REFERENCES users(email)
);

-- Table des donations
CREATE TABLE donations (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           donor_id UUID NOT NULL,
                           payment_id UUID NOT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_donation_donor FOREIGN KEY (donor_id) REFERENCES users(id),
                           CONSTRAINT fk_donation_payment FOREIGN KEY (payment_id) REFERENCES payments(id)
);

-- Table des aides
CREATE TABLE helps (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       beneficiary_id UUID NOT NULL,
                       payment_id UUID NOT NULL,
                       accident_description TEXT NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       CONSTRAINT fk_help_beneficiary FOREIGN KEY (beneficiary_id) REFERENCES users(id),
                       CONSTRAINT fk_help_payment FOREIGN KEY (payment_id) REFERENCES payments(id)
);

-- Index pour optimiser les requêtes
CREATE INDEX idx_payments_status ON payments(verification_status);
CREATE INDEX idx_donations_created_at ON donations(created_at DESC);
CREATE INDEX idx_helps_created_at ON helps(created_at DESC);


-- src/main/resources/db/migration/V2__Insert_sample_data_preprod.sql
-- Données d'exemple pour l'environnement preprod
INSERT INTO users (email, full_name) VALUES
                                         ('lou@hei.school', 'Lou Andria'),
                                         ('koto@kely.mg', 'Koto Kely'),
                                         ('mihago@rasoa.mg', 'Mihago Rasoa'),
                                         ('test@preprod.mg', 'Test Preprod User');

INSERT INTO payments (psp_payment_id, payer_email, amount, verification_status) VALUES
                                                                                    ('MP250804.0904.A01637', 'lou@hei.school', 50000, 'SUCCEEDED'),
                                                                                    ('MP250804.0908.D15807', 'koto@kely.mg', 5000, 'SUCCEEDED'),
                                                                                    ('MP250804.0910.A02057', 'mihago@rasoa.mg', 1000, 'VERIFYING');

-- Insertion des donations
INSERT INTO donations (donor_id, payment_id)
SELECT u.id, p.id
FROM users u, payments p
WHERE u.email = p.payer_email
  AND p.psp_payment_id IN ('MP250804.0904.A01637', 'MP250804.0910.A02057');

-- Insertion d'une aide
INSERT INTO helps (beneficiary_id, payment_id, accident_description)
SELECT u.id, p.id, 'Pour Koto Kely. Koto a été renversé par une moto et nécessite une chirurgie.'
FROM users u, payments p
WHERE u.email = 'koto@kely.mg'
  AND p.psp_payment_id = 'MP250804.0908.D15807';


-- src/main/resources/db/migration/V3__Insert_sample_data_prod.sql
-- Données d'exemple pour l'environnement prod (différentes de preprod)
INSERT INTO users (email, full_name) VALUES
                                         ('jean@hei.school', 'Jean Rakoto'),
                                         ('marie@prod.mg', 'Marie Rasoanirina'),
                                         ('paul@prod.mg', 'Paul Andriamanalina')
    ON CONFLICT (email) DO NOTHING;

INSERT INTO payments (psp_payment_id, payer_email, amount, verification_status) VALUES
                                                                                    ('MP250804.1224.B31974', 'jean@hei.school', 75000, 'SUCCEEDED'),
                                                                                    ('MP250804.1224.B31976', 'marie@prod.mg', 25000, 'SUCCEEDED'),
                                                                                    ('MP250804.1856.D63944', 'paul@prod.mg', 15000, 'FAILED')
    ON CONFLICT (psp_payment_id) DO NOTHING;

-- Insertion des donations pour prod
INSERT INTO donations (donor_id, payment_id)
SELECT u.id, p.id
FROM users u, payments p
WHERE u.email = p.payer_email
  AND p.psp_payment_id IN ('MP250804.1224.B31974', 'MP250804.1856.D63944')
  AND NOT EXISTS (
    SELECT 1 FROM donations d WHERE d.payment_id = p.id
);

-- Insertion d'une aide pour prod
INSERT INTO helps (beneficiary_id, payment_id, accident_description)
SELECT u.id, p.id, 'Aide d''urgence pour Marie suite à un accident de voiture nécessitant une hospitalisation.'
FROM users u, payments p
WHERE u.email = 'marie@prod.mg'
  AND p.psp_payment_id = 'MP250804.1224.B31976'
  AND NOT EXISTS (
    SELECT 1 FROM helps h WHERE h.payment_id = p.id
);