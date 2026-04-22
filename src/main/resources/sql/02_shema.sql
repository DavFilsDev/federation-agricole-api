
CREATE TYPE gender_enum AS ENUM ('MALE', 'FEMALE');
CREATE TYPE member_occupation_enum AS ENUM ('JUNIOR', 'SENIOR', 'SECRETARY', 'TREASURER', 'VICE_PRESIDENT', 'PRESIDENT');

-- 3. Table member
CREATE TABLE member (
                        id SERIAL PRIMARY KEY,
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

-- 4. Table collectivity
CREATE TABLE collectivity (
                              id SERIAL PRIMARY KEY,
                              location VARCHAR(255) NOT NULL,
                              specialite_agricole VARCHAR(255) NOT NULL,
                              annual_dues_amount INTEGER NOT NULL,
                              date_creation DATE NOT NULL,
                              federation_approval BOOLEAN NOT NULL
);

-- 5. Table membership
CREATE TABLE membership (
                            member_id INTEGER NOT NULL,
                            collectivity_id INTEGER NOT NULL,
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
                             candidate_id INTEGER NOT NULL,
                             sponsor_id INTEGER NOT NULL,
                             relation_nature VARCHAR(50) NOT NULL,
                             sponsorship_date DATE NOT NULL,
                             PRIMARY KEY (candidate_id, sponsor_id),
                             FOREIGN KEY (candidate_id) REFERENCES member(id) ON DELETE CASCADE,
                             FOREIGN KEY (sponsor_id) REFERENCES member(id) ON DELETE CASCADE,
                             CHECK (candidate_id != sponsor_id)
);

-- Index
CREATE INDEX idx_member_email ON member(email);
CREATE INDEX idx_membership_collectivity ON membership(collectivity_id);
CREATE INDEX idx_sponsorship_sponsor ON reference(sponsor_id);
CREATE INDEX idx_sponsorship_candidate ON reference(candidate_id);