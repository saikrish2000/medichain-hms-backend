-- ================================================================
-- V5 — Schema Fixes, Runtime Additions, Missing Columns
-- ================================================================

-- Appointments: add missing columns added in code
ALTER TABLE appointments
    MODIFY COLUMN department_id BIGINT NULL,
    MODIFY COLUMN branch_id     BIGINT NULL;

ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS reason_for_visit TEXT,
    ADD COLUMN IF NOT EXISTS duration_minutes INT DEFAULT 30,
    ADD COLUMN IF NOT EXISTS rejection_reason VARCHAR(500);

-- Doctor slots: ensure duration default
ALTER TABLE doctor_slots
    MODIFY COLUMN slot_duration_minutes INT DEFAULT 30;

-- Users: ensure all columns present
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email_verification_token  VARCHAR(255),
    ADD COLUMN IF NOT EXISTS password_reset_token      VARCHAR(255),
    ADD COLUMN IF NOT EXISTS password_reset_expiry     DATETIME,
    ADD COLUMN IF NOT EXISTS last_login                DATETIME;

-- Medical records: add missing vitals columns if not exist
ALTER TABLE medical_records
    ADD COLUMN IF NOT EXISTS oxygen_saturation DECIMAL(5,2);

-- ================================================================
-- SEED DATA — Specializations
-- ================================================================
INSERT IGNORE INTO specializations (name, description, is_active) VALUES
('General Medicine',    'Covers diagnosis and treatment of adult diseases',   TRUE),
('Cardiology',          'Heart and cardiovascular system specialist',          TRUE),
('Orthopaedics',        'Bone, joint and musculoskeletal disorders',           TRUE),
('Gynaecology',         'Female reproductive system and obstetrics',           TRUE),
('Paediatrics',         'Medical care for infants, children and adolescents',  TRUE),
('Neurology',           'Disorders of the nervous system',                     TRUE),
('Dermatology',         'Skin, hair and nail conditions',                      TRUE),
('ENT',                 'Ear, nose and throat disorders',                      TRUE),
('Ophthalmology',       'Eye disorders and vision care',                       TRUE),
('Psychiatry',          'Mental health and behavioural disorders',             TRUE),
('Urology',             'Urinary tract and male reproductive system',          TRUE),
('Gastroenterology',    'Digestive system disorders',                          TRUE),
('Pulmonology',         'Lung and respiratory disorders',                      TRUE),
('Endocrinology',       'Hormonal and metabolic disorders including diabetes', TRUE),
('Oncology',            'Cancer diagnosis and treatment',                      TRUE),
('Nephrology',          'Kidney disorders',                                    TRUE),
('Rheumatology',        'Autoimmune and joint disorders',                      TRUE),
('Emergency Medicine',  '24x7 emergency and trauma care',                     TRUE);

-- ================================================================
-- SEED DATA — Nurse Service Types (for independent nurses)
-- ================================================================
INSERT IGNORE INTO nurse_service_types (name, description, hourly_rate) VALUES
('Post-Operative Care',   'Care after surgical procedures at home',      350.00),
('Wound Dressing',        'Regular wound care and dressing changes',      250.00),
('IV Therapy',            'Intravenous medication at home',               400.00),
('Elderly Care',          'Daily care assistance for elderly patients',   280.00),
('Physiotherapy Assist',  'Assistance with physiotherapy exercises',      320.00),
('Cancer Care',           'Palliative and chemotherapy support',          500.00),
('Maternal Care',         'Post-natal and new mother support',            350.00),
('Diabetic Management',   'Glucose monitoring and insulin administration',300.00),
('Cardiac Monitoring',    'Post-cardiac event monitoring',                450.00),
('Paediatric Nursing',    'Child care nursing at home',                   320.00)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ================================================================
-- SEED DATA — Default Hospital Branch (avoids null FK)
-- ================================================================
INSERT IGNORE INTO hospital_branches
    (id, name, code, address, city, state, pincode, phone, email, is_active)
VALUES
    (1, 'MediChain Main Hospital', 'BRANCH-01',
     '12 Health Avenue, Jubilee Hills', 'Hyderabad', 'Telangana', '500033',
     '+91-40-12345678', 'main@medichain.in', TRUE);

-- ================================================================
-- SEED DATA — Default Departments
-- ================================================================
INSERT IGNORE INTO departments (branch_id, name, code, description, floor_number, is_active) VALUES
(1, 'General Medicine',   'GEN-MED',  'General OPD and medicine',         '1', TRUE),
(1, 'Cardiology',         'CARDIO',   'Heart and cardiovascular care',     '2', TRUE),
(1, 'Orthopaedics',       'ORTHO',    'Bone and joint care',               '2', TRUE),
(1, 'Gynaecology',        'GYNAE',    'Womens health and obstetrics',      '3', TRUE),
(1, 'Paediatrics',        'PAEDS',    'Child healthcare',                  '3', TRUE),
(1, 'Neurology',          'NEURO',    'Brain and nervous system',          '4', TRUE),
(1, 'Emergency',          'ER',       '24x7 Emergency and Trauma',         'G', TRUE),
(1, 'Radiology',          'RADIO',    'Imaging and diagnostics',           'B', TRUE),
(1, 'Pathology Lab',      'LAB',      'Blood tests and pathology',         'B', TRUE),
(1, 'Pharmacy',           'PHARMA',   'In-house dispensary',               'G', TRUE);
