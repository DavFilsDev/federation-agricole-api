-- Type énuméré pour le type d'activité (conforme à spec)
CREATE TYPE activity_type_enum AS ENUM ('MEETING', 'TRAINING', 'OTHER');
CREATE TYPE attendance_status_enum AS ENUM ('UNDEFINED', 'ATTENDED', 'MISSING');
-- Type énuméré pour les jours de semaine (utilisé dans la règle de récurrence)
CREATE TYPE weekly_day_enum AS ENUM ('MO', 'TU', 'WE', 'TH', 'FR', 'SA', 'SU');

CREATE TABLE collectivity_activity (
                                       id VARCHAR(50) PRIMARY KEY,
                                       collectivity_id VARCHAR(50) NOT NULL,
                                       label VARCHAR(255) NOT NULL,
                                       activity_type activity_type_enum NOT NULL,
                                       recurrence_week_ordinal INTEGER CHECK (recurrence_week_ordinal BETWEEN 1 AND 5),
                                       recurrence_day_of_week weekly_day_enum,
                                       executive_date DATE,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       FOREIGN KEY (collectivity_id) REFERENCES collectivity(id) ON DELETE CASCADE,
                                       CHECK (
                                           (executive_date IS NOT NULL AND recurrence_week_ordinal IS NULL AND recurrence_day_of_week IS NULL)
                                               OR
                                           (executive_date IS NULL AND recurrence_week_ordinal IS NOT NULL AND recurrence_day_of_week IS NOT NULL)
                                           )
);

CREATE TABLE activity_concerned_occupation (
                                               activity_id VARCHAR(50) NOT NULL,
                                               occupation member_occupation_enum NOT NULL,
                                               PRIMARY KEY (activity_id, occupation),
                                               FOREIGN KEY (activity_id) REFERENCES collectivity_activity(id) ON DELETE CASCADE
);

CREATE TABLE activity_member_attendance (
                                            id VARCHAR(50) PRIMARY KEY,
                                            activity_id VARCHAR(50) NOT NULL,
                                            member_id VARCHAR(50) NOT NULL,
                                            status attendance_status_enum NOT NULL DEFAULT 'UNDEFINED',
                                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            UNIQUE (activity_id, member_id),
                                            FOREIGN KEY (activity_id) REFERENCES collectivity_activity(id) ON DELETE CASCADE,
                                            FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE
);