-- Insérer une activité ponctuelle
INSERT INTO collectivity_activity (id, collectivity_id, label, activity_type, executive_date) VALUES
    ('act-test-1', 'col-1', 'Assemblée générale', 'MEETING', '2026-05-15');

-- Insérer une activité avec récurrence (2ème samedi du mois)
INSERT INTO collectivity_activity (id, collectivity_id, label, activity_type, recurrence_week_ordinal, recurrence_day_of_week) VALUES
    ('act-test-2', 'col-1', 'Formation mensuelle', 'TRAINING', 2, 'SA');

-- Définir les occupations concernées pour l'activité de formation (uniquement JUNIOR et SENIOR)
INSERT INTO activity_concerned_occupation (activity_id, occupation) VALUES
                                                                        ('act-test-2', 'JUNIOR'),
                                                                        ('act-test-2', 'SENIOR');

-- Insérer quelques présences initiales (UNDEFINED par défaut pour les membres concernés)
-- Pour l'activité act-test-1 (tous les membres peuvent être présents)
INSERT INTO activity_member_attendance (id, activity_id, member_id, status) VALUES
                                                                                ('att-1', 'act-test-1', 'C1-M1', 'UNDEFINED'),
                                                                                ('att-2', 'act-test-1', 'C1-M2', 'UNDEFINED'),
                                                                                ('att-3', 'act-test-1', 'C1-M3', 'UNDEFINED');