CREATE TYPE frequency_enum AS ENUM ('WEEKLY', 'MONTHLY', 'ANNUALLY', 'PUNCTUALLY');
CREATE TYPE activity_status_enum AS ENUM ('ACTIVE', 'INACTIVE');
CREATE TYPE payment_mode_enum AS ENUM ('CASH', 'MOBILE_BANKING', 'BANK_TRANSFER');
CREATE TYPE mobile_banking_service_enum AS ENUM ('AIRTEL_MONEY', 'MVOLA', 'ORANGE_MONEY');
CREATE TYPE bank_enum AS ENUM ('BRED', 'MCB', 'BMOI', 'BOA', 'BGFI', 'AFG', 'ACCES_BAQUE', 'BAOBAB', 'SIPEM');
CREATE TYPE financial_account_type_enum AS ENUM ('CASH', 'MOBILE_BANKING', 'BANK');

-- Table parent FinancialAccount
CREATE TABLE financial_account (
                                   id SERIAL PRIMARY KEY,
                                   type financial_account_type_enum NOT NULL,
                                   amount DECIMAL(15,2) NOT NULL DEFAULT 0,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tables filles (héritage)
CREATE TABLE cash_account (
                              id INTEGER PRIMARY KEY,
                              FOREIGN KEY (id) REFERENCES financial_account(id) ON DELETE CASCADE
);

CREATE TABLE mobile_banking_account (
                                        id INTEGER PRIMARY KEY,
                                        holder_name VARCHAR(255),
                                        mobile_service mobile_banking_service_enum,
                                        mobile_number VARCHAR(20),
                                        FOREIGN KEY (id) REFERENCES financial_account(id) ON DELETE CASCADE
);

CREATE TABLE bank_account (
                              id INTEGER PRIMARY KEY,
                              holder_name VARCHAR(255),
                              bank_name bank_enum,
                              bank_code INTEGER,
                              branch_code INTEGER,
                              account_number INTEGER,
                              account_key INTEGER,
                              FOREIGN KEY (id) REFERENCES financial_account(id) ON DELETE CASCADE
);

-- Table MembershipFee
CREATE TABLE membership_fee (
                                id SERIAL PRIMARY KEY,
                                collectivity_id INTEGER NOT NULL REFERENCES collectivity(id) ON DELETE CASCADE,
                                eligible_from DATE NOT NULL,
                                frequency frequency_enum NOT NULL,
                                amount DECIMAL(15,2) NOT NULL CHECK (amount >= 0),
                                label VARCHAR(255),
                                status activity_status_enum NOT NULL DEFAULT 'ACTIVE',
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table Transaction (collectivity transaction)
CREATE TABLE transaction (
                             id SERIAL PRIMARY KEY,
                             member_id INTEGER NOT NULL REFERENCES member(id) ON DELETE CASCADE,
                             collectivity_id INTEGER NOT NULL REFERENCES collectivity(id) ON DELETE CASCADE,
                             amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
                             payment_mode payment_mode_enum NOT NULL,
                             account_credited_id INTEGER NOT NULL REFERENCES financial_account(id),
                             membership_fee_id INTEGER REFERENCES membership_fee(id),
                             creation_date DATE NOT NULL,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index
CREATE INDEX idx_membership_fee_collectivity ON membership_fee(collectivity_id);
CREATE INDEX idx_transaction_collectivity ON transaction(collectivity_id);
CREATE INDEX idx_transaction_member ON transaction(member_id);
CREATE INDEX idx_transaction_date ON transaction(creation_date);
CREATE INDEX idx_financial_account_type ON financial_account(type);