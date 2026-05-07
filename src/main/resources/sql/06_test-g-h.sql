-- Ajouter une cotisation inactive pour tester
INSERT INTO membership_fee (id, collectivity_id, eligible_from, frequency, amount, label, status) VALUES
    ('cot-1-inactive', 'col-1', '2025-01-01', 'MONTHLY', 50000, 'Cotison mensuelle 2025', 'INACTIVE');