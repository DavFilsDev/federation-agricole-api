-- =====================================================
-- 1. Suppression et recréation des tables (ordre inverse des dépendances)
-- =====================================================
DROP TABLE IF EXISTS transaction CASCADE;
DROP TABLE IF EXISTS membership_fee CASCADE;
DROP TABLE IF EXISTS financial_account CASCADE;
DROP TABLE IF EXISTS reference CASCADE;
DROP TABLE IF EXISTS membership CASCADE;
DROP TABLE IF EXISTS member CASCADE;
DROP TABLE IF EXISTS collectivity CASCADE;

-- Types ENUM (suppression si existants)
DROP TYPE IF EXISTS gender_enum CASCADE;
DROP TYPE IF EXISTS member_occupation_enum CASCADE;
DROP TYPE IF EXISTS frequency_enum CASCADE;
DROP TYPE IF EXISTS activity_status_enum CASCADE;
DROP TYPE IF EXISTS payment_mode_enum CASCADE;
DROP TYPE IF EXISTS mobile_banking_service_enum CASCADE;
DROP TYPE IF EXISTS bank_enum CASCADE;
DROP TYPE IF EXISTS financial_account_type_enum CASCADE;

CREATE TYPE gender_enum AS ENUM ('MALE', 'FEMALE');
CREATE TYPE member_occupation_enum AS ENUM ('JUNIOR', 'SENIOR', 'SECRETARY', 'TREASURER', 'VICE_PRESIDENT', 'PRESIDENT');
CREATE TYPE frequency_enum AS ENUM ('WEEKLY', 'MONTHLY', 'ANNUALLY', 'PUNCTUALLY');
CREATE TYPE activity_status_enum AS ENUM ('ACTIVE', 'INACTIVE');
CREATE TYPE payment_mode_enum AS ENUM ('CASH', 'MOBILE_BANKING', 'BANK_TRANSFER');
CREATE TYPE mobile_banking_service_enum AS ENUM ('AIRTEL_MONEY', 'MVOLA', 'ORANGE_MONEY');
CREATE TYPE bank_enum AS ENUM ('BRED', 'MCB', 'BMOI', 'BOA', 'BGFI', 'AFG', 'ACCES_BAQUE', 'BAOBAB', 'SIPEM');
CREATE TYPE financial_account_type_enum AS ENUM ('CASH', 'MOBILE_BANKING', 'BANK');

-- =====================================================
-- 2. Tables (clés primaires en VARCHAR)
-- =====================================================

CREATE TABLE collectivity (
                              id VARCHAR(50) PRIMARY KEY,
                              name VARCHAR(100) UNIQUE,
                              number INTEGER UNIQUE,
                              location VARCHAR(255) NOT NULL,
                              specialite_agricole VARCHAR(255) NOT NULL,
                              annual_dues_amount INTEGER NOT NULL,
                              date_creation DATE NOT NULL,
                              federation_approval BOOLEAN NOT NULL
);

CREATE TABLE member (
                        id VARCHAR(50) PRIMARY KEY,
                        first_name VARCHAR(100) NOT NULL,
                        last_name VARCHAR(100) NOT NULL,
                        birth_date DATE NOT NULL,
                        gender gender_enum NOT NULL,
                        address TEXT,
                        profession VARCHAR(100),
                        phone_number VARCHAR(20),
                        email VARCHAR(255) UNIQUE NOT NULL,
                        date_adhesion_federation DATE NOT NULL
);

CREATE TABLE membership (
                            member_id VARCHAR(50),
                            collectivity_id VARCHAR(50),
                            occupation member_occupation_enum NOT NULL,
                            registration_fee_paid BOOLEAN NOT NULL,
                            membership_dues_paid BOOLEAN NOT NULL,
                            date_adhesion DATE NOT NULL,
                            payment_date DATE,
                            PRIMARY KEY (member_id, collectivity_id),
                            FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
                            FOREIGN KEY (collectivity_id) REFERENCES collectivity(id) ON DELETE CASCADE
);

CREATE TABLE reference (
                           candidate_id VARCHAR(50),
                           sponsor_id VARCHAR(50),
                           relation_nature VARCHAR(50) NOT NULL,
                           sponsorship_date DATE NOT NULL,
                           PRIMARY KEY (candidate_id, sponsor_id),
                           FOREIGN KEY (candidate_id) REFERENCES member(id) ON DELETE CASCADE,
                           FOREIGN KEY (sponsor_id) REFERENCES member(id) ON DELETE CASCADE
);

CREATE TABLE financial_account (
                                   id VARCHAR(50) PRIMARY KEY,
                                   type financial_account_type_enum NOT NULL,
                                   amount DECIMAL(15,2) NOT NULL DEFAULT 0,
                                   collectivity_id VARCHAR(50) NOT NULL,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   FOREIGN KEY (collectivity_id) REFERENCES collectivity(id) ON DELETE CASCADE
);

-- Tables filles (héritage)
CREATE TABLE cash_account (
                              id VARCHAR(50) PRIMARY KEY,
                              FOREIGN KEY (id) REFERENCES financial_account(id) ON DELETE CASCADE
);
CREATE TABLE mobile_banking_account (
                                        id VARCHAR(50) PRIMARY KEY,
                                        holder_name VARCHAR(255),
                                        mobile_service mobile_banking_service_enum,
                                        mobile_number VARCHAR(20),
                                        FOREIGN KEY (id) REFERENCES financial_account(id) ON DELETE CASCADE
);
CREATE TABLE bank_account (
                              id VARCHAR(50) PRIMARY KEY,
                              holder_name VARCHAR(255),
                              bank_name bank_enum,
                              bank_code INTEGER,
                              branch_code INTEGER,
                              account_number INTEGER,
                              account_key INTEGER,
                              FOREIGN KEY (id) REFERENCES financial_account(id) ON DELETE CASCADE
);

CREATE TABLE membership_fee (
                                id VARCHAR(50) PRIMARY KEY,
                                collectivity_id VARCHAR(50) NOT NULL,
                                eligible_from DATE NOT NULL,
                                frequency frequency_enum NOT NULL,
                                amount DECIMAL(15,2) NOT NULL CHECK (amount >= 0),
                                label VARCHAR(255),
                                status activity_status_enum NOT NULL DEFAULT 'ACTIVE',
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (collectivity_id) REFERENCES collectivity(id) ON DELETE CASCADE
);

CREATE TABLE transaction (
                             id VARCHAR(50) PRIMARY KEY,
                             member_id VARCHAR(50) NOT NULL,
                             collectivity_id VARCHAR(50) NOT NULL,
                             amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
                             payment_mode payment_mode_enum NOT NULL,
                             account_credited_id VARCHAR(50) NOT NULL,
                             membership_fee_id VARCHAR(50),
                             creation_date DATE NOT NULL,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE,
                             FOREIGN KEY (collectivity_id) REFERENCES collectivity(id) ON DELETE CASCADE,
                             FOREIGN KEY (account_credited_id) REFERENCES financial_account(id),
                             FOREIGN KEY (membership_fee_id) REFERENCES membership_fee(id)
);

-- =====================================================
-- 3. Insertion des collectivités (tableau 1)
-- =====================================================
INSERT INTO collectivity (id, number, name, location, specialite_agricole, annual_dues_amount, date_creation, federation_approval) VALUES
                                                                                                                                       ('col-1', 1, 'Mpanorina', 'Ambatondrazaka', 'Riziculture', 100000, '2024-01-01', true),
                                                                                                                                       ('col-2', 2, 'Dobo voalohany', 'Ambatondrazaka', 'Pisciculture', 100000, '2024-01-01', true),
                                                                                                                                       ('col-3', 3, 'Tantely mamy', 'Brickaville', 'Apiculture', 50000, '2024-01-01', true);

-- =====================================================
-- 4. Insertion des membres (tableaux 2, 3, 4) et parrainages
-- =====================================================

-- Membres de la collectivité 1 (C1-M1 à C1-M8)
INSERT INTO member (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, date_adhesion_federation) VALUES
                                                                                                                                           ('C1-M1', 'Nom membre 1', 'Prénom membre 1', '1980-02-01', 'MALE', 'Lot II V M Ambato.', 'Riziculteur', '0341234567', 'member.1@fed-agri.mg', '2024-01-01'),
                                                                                                                                           ('C1-M2', 'Nom membre 2', 'Prénom membre 2', '1982-03-05', 'MALE', 'Lot II F Ambato.', 'Agriculteur', '0321234567', 'member.2@fed-agri.mg', '2024-01-01'),
                                                                                                                                           ('C1-M3', 'Nom membre 3', 'Prénom membre 3', '1992-03-10', 'MALE', 'Lot II J Ambato.', 'Collecteur', '0331234567', 'member.3@fed-agri.mg', '2024-01-01'),
                                                                                                                                           ('C1-M4', 'Nom membre 4', 'Prénom membre 4', '1988-05-22', 'FEMALE', 'Lot A K 50 Ambato.', 'Distributeur', '0381234567', 'member.4@fed-agri.mg', '2024-01-01'),
                                                                                                                                           ('C1-M5', 'Nom membre 5', 'Prénom membre 5', '1999-08-21', 'MALE', 'Lot UV 80 Ambato.', 'Riziculteur', '0373434567', 'member.5@fed-agri.mg', '2024-01-01'),
                                                                                                                                           ('C1-M6', 'Nom membre 6', 'Prénom membre 6', '1998-08-22', 'FEMALE', 'Lot UV 6 Ambato.', 'Riziculteur', '0372234567', 'member.6@fed-agri.mg', '2024-01-01'),
                                                                                                                                           ('C1-M7', 'Nom membre 7', 'Prénom membre 7', '1998-01-31', 'MALE', 'Lot UV 7 Ambato.', 'Riziculteur', '0374234567', 'member.7@fed-agri.mg', '2024-01-01'),
                                                                                                                                           ('C1-M8', 'Nom membre 8', 'Prénom membre 8', '1975-08-20', 'MALE', 'Lot UV 8 Ambato.', 'Riziculteur', '0370234567', 'member.8@fed-agri.mg', '2024-01-01');

-- Membres de la collectivité 2 (C2-Mx) : certains sont les mêmes personnes que C1-Mx, donc on réutilise les IDs existants ?
-- D'après le tableau 3, les IDs sont comme "C2-M1 ou C1-M1", ce qui signifie que ce sont les mêmes membres qui appartiennent à plusieurs collectivités.
-- Nous devons donc insérer les mêmes membres (déjà existants) pour la collectivité 2 dans la table membership.
-- Aucun nouveau membre à insérer, car ils sont déjà présents.

-- Membres de la collectivité 3 ? Il n'y a pas de membres listés pour col-3. Donc aucun.

-- Insertion des appartenances (membership) pour la collectivité 1
INSERT INTO membership (member_id, collectivity_id, occupation, registration_fee_paid, membership_dues_paid, date_adhesion, payment_date) VALUES
                                                                                                                                              ('C1-M1', 'col-1', 'PRESIDENT', true, true, '2024-01-01', '2024-01-01'),
                                                                                                                                              ('C1-M2', 'col-1', 'VICE_PRESIDENT', true, true, '2024-01-01', '2024-01-01'),
                                                                                                                                              ('C1-M3', 'col-1', 'SECRETARY', true, true, '2024-01-01', '2024-01-01'),
                                                                                                                                              ('C1-M4', 'col-1', 'TREASURER', true, true, '2024-01-01', '2024-01-01'),
                                                                                                                                              ('C1-M5', 'col-1', 'SENIOR', true, true, '2024-01-01', '2024-01-01'),
                                                                                                                                              ('C1-M6', 'col-1', 'SENIOR', true, true, '2024-01-01', '2024-01-01'),
                                                                                                                                              ('C1-M7', 'col-1', 'SENIOR', true, true, '2024-01-01', '2024-01-01'),
                                                                                                                                              ('C1-M8', 'col-1', 'SENIOR', true, true, '2024-01-01', '2024-01-01');

-- Insertion des appartenances pour la collectivité 2 (mêmes membres)
INSERT INTO membership (member_id, collectivity_id, occupation, registration_fee_paid, membership_dues_paid, date_adhesion, payment_date) VALUES
                                                                                                                                              ('C1-M1', 'col-2', 'SENIOR', true, true, '2024-01-01', '2024-01-01'),
                                                                                                                                              ('C1-M2', 'col-2', 'SENIOR', true, true, '2024-01-01', '2024-01-01'),
                                                                                                                                              ('C1-M3', 'col-2', 'SENIOR', true, true, '2024-01-01', '2024-01-01'),
                                                                                                                                              ('C1-M4', 'col-2', 'SENIOR', true, true, '2024-01-01', '2024-01-01'),
                                                                                                                                              ('C1-M5', 'col-2', 'PRESIDENT', true, true, '2024-01-01', '2024-01-01'),
                                                                                                                                              ('C1-M6', 'col-2', 'VICE_PRESIDENT', true, true, '2024-01-01', '2024-01-01'),
                                                                                                                                              ('C1-M7', 'col-2', 'SECRETARY', true, true, '2024-01-01', '2024-01-01'),
                                                                                                                                              ('C1-M8', 'col-2', 'TREASURER', true, true, '2024-01-01', '2024-01-01');

-- Insertion des parrainages (table reference) d'après les colonnes "ID des membres référents"
-- Pour C1-M3 : référents C1-M1; C1-M2
INSERT INTO reference (candidate_id, sponsor_id, relation_nature, sponsorship_date) VALUES
                                                                                        ('C1-M3', 'C1-M1', 'unknown', '2024-01-01'),
                                                                                        ('C1-M3', 'C1-M2', 'unknown', '2024-01-01');
-- Pour C1-M4 : référents C1-M1; C1-M2
INSERT INTO reference (candidate_id, sponsor_id, relation_nature, sponsorship_date) VALUES
                                                                                        ('C1-M4', 'C1-M1', 'unknown', '2024-01-01'),
                                                                                        ('C1-M4', 'C1-M2', 'unknown', '2024-01-01');
-- Pour C1-M5 : référents C1-M1; C1-M2
INSERT INTO reference (candidate_id, sponsor_id, relation_nature, sponsorship_date) VALUES
                                                                                        ('C1-M5', 'C1-M1', 'unknown', '2024-01-01'),
                                                                                        ('C1-M5', 'C1-M2', 'unknown', '2024-01-01');
-- Pour C1-M6 : référents C1-M1; C1-M2
INSERT INTO reference (candidate_id, sponsor_id, relation_nature, sponsorship_date) VALUES
                                                                                        ('C1-M6', 'C1-M1', 'unknown', '2024-01-01'),
                                                                                        ('C1-M6', 'C1-M2', 'unknown', '2024-01-01');
-- Pour C1-M7 : référents C1-M1; C1-M2
INSERT INTO reference (candidate_id, sponsor_id, relation_nature, sponsorship_date) VALUES
                                                                                        ('C1-M7', 'C1-M1', 'unknown', '2024-01-01'),
                                                                                        ('C1-M7', 'C1-M2', 'unknown', '2024-01-01');
-- Pour C1-M8 : référents C1-M6; C1-M7
INSERT INTO reference (candidate_id, sponsor_id, relation_nature, sponsorship_date) VALUES
                                                                                        ('C1-M8', 'C1-M6', 'unknown', '2024-01-01'),
                                                                                        ('C1-M8', 'C1-M7', 'unknown', '2024-01-01');

-- =====================================================
-- 5. Cotisations (membership_fee) – tableaux 5,6,7
-- =====================================================
-- Collectivité col-1
INSERT INTO membership_fee (id, collectivity_id, eligible_from, frequency, amount, label, status) VALUES
    ('cot-1', 'col-1', '2026-01-01', 'ANNUALLY', 100000, 'Cotisation annuelle', 'ACTIVE');
-- Collectivité col-2
INSERT INTO membership_fee (id, collectivity_id, eligible_from, frequency, amount, label, status) VALUES
    ('cot-2', 'col-2', '2026-01-01', 'ANNUALLY', 100000, 'Cotisation annuelle', 'ACTIVE');
-- Collectivité col-3
INSERT INTO membership_fee (id, collectivity_id, eligible_from, frequency, amount, label, status) VALUES
    ('cot-3', 'col-3', '2026-01-01', 'ANNUALLY', 50000, 'Cotisation annuelle', 'ACTIVE');

-- =====================================================
-- 6. Comptes financiers (financial_account et tables filles)
-- =====================================================
-- Collectivité col-1
INSERT INTO financial_account (id, type, amount, collectivity_id) VALUES
                                                                      ('C1-A-CASH', 'CASH', 0, 'col-1'),
                                                                      ('C1-A-MOBILE-1', 'MOBILE_BANKING', 0, 'col-1');
INSERT INTO cash_account (id) VALUES ('C1-A-CASH');
INSERT INTO mobile_banking_account (id, holder_name, mobile_service, mobile_number) VALUES
    ('C1-A-MOBILE-1', 'Mpanorina', 'ORANGE_MONEY', '0370489612');

-- Collectivité col-2
INSERT INTO financial_account (id, type, amount, collectivity_id) VALUES
                                                                      ('C2-A-CASH', 'CASH', 0, 'col-2'),
                                                                      ('C2-A-MOBILE-1', 'MOBILE_BANKING', 0, 'col-2');
INSERT INTO cash_account (id) VALUES ('C2-A-CASH');
INSERT INTO mobile_banking_account (id, holder_name, mobile_service, mobile_number) VALUES
    ('C2-A-MOBILE-1', 'Dobo voalohany', 'ORANGE_MONEY', '0320489612');

-- Collectivité col-3
INSERT INTO financial_account (id, type, amount, collectivity_id) VALUES
    ('C3-A-CASH', 'CASH', 0, 'col-3');
INSERT INTO cash_account (id) VALUES ('C3-A-CASH');

-- =====================================================
-- 7. Transactions et paiements (tableaux 8,9,10,11)
-- Note : les paiements sont ici représentés par des transactions.
-- Les montants sont en Ariary.
-- =====================================================

-- Collectivité col-1 (paiements du 01/01/2026)
INSERT INTO transaction (id, member_id, collectivity_id, amount, payment_mode, account_credited_id, membership_fee_id, creation_date) VALUES
                                                                                                                                          ('tx-C1-M1', 'C1-M1', 'col-1', 100000, 'CASH', 'C1-A-CASH', 'cot-1', '2026-01-01'),
                                                                                                                                          ('tx-C1-M2', 'C1-M2', 'col-1', 100000, 'CASH', 'C1-A-CASH', 'cot-1', '2026-01-01'),
                                                                                                                                          ('tx-C1-M3', 'C1-M3', 'col-1', 100000, 'CASH', 'C1-A-CASH', 'cot-1', '2026-01-01'),
                                                                                                                                          ('tx-C1-M4', 'C1-M4', 'col-1', 100000, 'CASH', 'C1-A-CASH', 'cot-1', '2026-01-01'),
                                                                                                                                          ('tx-C1-M5', 'C1-M5', 'col-1', 100000, 'CASH', 'C1-A-CASH', 'cot-1', '2026-01-01'),
                                                                                                                                          ('tx-C1-M6', 'C1-M6', 'col-1', 100000, 'CASH', 'C1-A-CASH', 'cot-1', '2026-01-01'),
                                                                                                                                          ('tx-C1-M7', 'C1-M7', 'col-1', 60000, 'CASH', 'C1-A-CASH', 'cot-1', '2026-01-01'),
                                                                                                                                          ('tx-C1-M8', 'C1-M8', 'col-1', 90000, 'CASH', 'C1-A-CASH', 'cot-1', '2026-01-01');

-- Collectivité col-2 (paiements du 01/01/2026)
INSERT INTO transaction (id, member_id, collectivity_id, amount, payment_mode, account_credited_id, membership_fee_id, creation_date) VALUES
                                                                                                                                          ('tx-C2-M1', 'C1-M1', 'col-2', 60000, 'CASH', 'C2-A-CASH', 'cot-2', '2026-01-01'),
                                                                                                                                          ('tx-C2-M2', 'C1-M2', 'col-2', 90000, 'CASH', 'C2-A-CASH', 'cot-2', '2026-01-01'),
                                                                                                                                          ('tx-C2-M3', 'C1-M3', 'col-2', 100000, 'CASH', 'C2-A-CASH', 'cot-2', '2026-01-01'),
                                                                                                                                          ('tx-C2-M4', 'C1-M4', 'col-2', 100000, 'CASH', 'C2-A-CASH', 'cot-2', '2026-01-01'),
                                                                                                                                          ('tx-C2-M5', 'C1-M5', 'col-2', 100000, 'CASH', 'C2-A-CASH', 'cot-2', '2026-01-01'),
                                                                                                                                          ('tx-C2-M6', 'C1-M6', 'col-2', 100000, 'CASH', 'C2-A-CASH', 'cot-2', '2026-01-01'),
                                                                                                                                          ('tx-C2-M7', 'C1-M7', 'col-2', 40000, 'MOBILE_BANKING', 'C2-A-MOBILE-1', 'cot-2', '2026-01-01'),
                                                                                                                                          ('tx-C2-M8', 'C1-M8', 'col-2', 60000, 'MOBILE_BANKING', 'C2-A-MOBILE-1', 'cot-2', '2026-01-01');

-- =====================================================
-- 8. Mise à jour des soldes des comptes financiers (cumul des transactions)
-- =====================================================
-- Compte C1-A-CASH : somme des transactions de col-1 créditant ce compte
UPDATE financial_account SET amount = (
    SELECT COALESCE(SUM(amount), 0) FROM transaction WHERE account_credited_id = 'C1-A-CASH'
) WHERE id = 'C1-A-CASH';

-- Compte C2-A-CASH : somme des transactions de col-2 créditant ce compte
UPDATE financial_account SET amount = (
    SELECT COALESCE(SUM(amount), 0) FROM transaction WHERE account_credited_id = 'C2-A-CASH'
) WHERE id = 'C2-A-CASH';

-- Compte C2-A-MOBILE-1
UPDATE financial_account SET amount = (
    SELECT COALESCE(SUM(amount), 0) FROM transaction WHERE account_credited_id = 'C2-A-MOBILE-1'
) WHERE id = 'C2-A-MOBILE-1';

-- Les comptes sans transaction restent à 0 (C3-A-CASH, C1-A-MOBILE-1)