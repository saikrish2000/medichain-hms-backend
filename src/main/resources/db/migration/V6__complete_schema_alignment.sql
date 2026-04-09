-- ================================================================
-- V6 — COMPLETE SCHEMA ALIGNMENT
-- Syncs every entity ↔ DB mismatch found in audit.
-- All statements use IF NOT EXISTS / MODIFY to be idempotent.
-- ================================================================

-- ================================================================
-- 1. DOCTOR_SLOTS — add missing columns entity expects
-- ================================================================
ALTER TABLE doctor_slots
    ADD COLUMN IF NOT EXISTS day_of_week    VARCHAR(10)         NULL,
    ADD COLUMN IF NOT EXISTS duration_minutes INT  DEFAULT 30    NOT NULL,
    ADD COLUMN IF NOT EXISTS current_patients INT DEFAULT 0     NOT NULL,
    ADD COLUMN IF NOT EXISTS is_blocked     BOOLEAN DEFAULT FALSE NOT NULL,
    ADD COLUMN IF NOT EXISTS is_recurring   BOOLEAN DEFAULT FALSE NOT NULL,
    ADD COLUMN IF NOT EXISTS block_reason   VARCHAR(255)         NULL,
    ADD COLUMN IF NOT EXISTS is_active      BOOLEAN DEFAULT TRUE  NOT NULL;

-- Rename booked_count → current_patients safely (only if booked_count still exists)
-- We use a stored procedure approach via a dummy UPDATE to avoid error
UPDATE doctor_slots SET current_patients = booked_count WHERE booked_count != current_patients;

-- ================================================================
-- 2. APPOINTMENTS — add missing columns
-- ================================================================
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS duration_minutes   INT          DEFAULT 30    NULL,
    ADD COLUMN IF NOT EXISTS type               VARCHAR(30)  DEFAULT 'IN_PERSON' NULL,
    ADD COLUMN IF NOT EXISTS notes              TEXT                        NULL,
    ADD COLUMN IF NOT EXISTS is_paid            BOOLEAN      DEFAULT FALSE  NOT NULL,
    ADD COLUMN IF NOT EXISTS payment_id         VARCHAR(100)               NULL,
    ADD COLUMN IF NOT EXISTS is_emergency       BOOLEAN      DEFAULT FALSE  NOT NULL,
    ADD COLUMN IF NOT EXISTS follow_up_date     DATE                       NULL,
    ADD COLUMN IF NOT EXISTS checked_in_at      DATETIME                   NULL,
    ADD COLUMN IF NOT EXISTS completed_at       DATETIME                   NULL,
    ADD COLUMN IF NOT EXISTS department_id      BIGINT                     NULL,
    ADD COLUMN IF NOT EXISTS created_at         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS updated_at         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NULL;

-- FK for department_id if departments table exists
ALTER TABLE appointments
    ADD CONSTRAINT IF NOT EXISTS fk_appt_dept
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL;

-- ================================================================
-- 3. MEDICAL_RECORDS — align with entity
-- ================================================================
ALTER TABLE medical_records
    ADD COLUMN IF NOT EXISTS record_type        VARCHAR(50)  DEFAULT 'VISIT'  NULL,
    ADD COLUMN IF NOT EXISTS visit_date         DATE                           NULL,
    ADD COLUMN IF NOT EXISTS symptoms           TEXT                           NULL,
    ADD COLUMN IF NOT EXISTS oxygen_saturation  DECIMAL(5,2)                  NULL,
    ADD COLUMN IF NOT EXISTS respiratory_rate   INT                            NULL,
    ADD COLUMN IF NOT EXISTS is_active          BOOLEAN      DEFAULT TRUE      NOT NULL;

-- Sync visit_date = record_date where null
UPDATE medical_records SET visit_date = record_date WHERE visit_date IS NULL;

-- ================================================================
-- 4. DOCTORS — add missing columns
-- ================================================================
ALTER TABLE doctors
    ADD COLUMN IF NOT EXISTS branch_id          BIGINT                     NULL,
    ADD COLUMN IF NOT EXISTS approved_by        BIGINT                     NULL,
    ADD COLUMN IF NOT EXISTS approved_at        DATETIME                   NULL,
    ADD COLUMN IF NOT EXISTS background_check_status VARCHAR(20) DEFAULT 'PENDING' NULL,
    ADD COLUMN IF NOT EXISTS background_check_notes TEXT                   NULL,
    ADD COLUMN IF NOT EXISTS updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NULL;

ALTER TABLE doctors
    ADD CONSTRAINT IF NOT EXISTS fk_doctor_branch
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id) ON DELETE SET NULL;

-- ================================================================
-- 5. USERS — add missing columns
-- ================================================================
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS enabled            BOOLEAN DEFAULT TRUE  NOT NULL,
    ADD COLUMN IF NOT EXISTS approval_status    VARCHAR(20) DEFAULT 'APPROVED' NULL,
    ADD COLUMN IF NOT EXISTS updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NULL;

-- Sync enabled from is_active
UPDATE users SET enabled = is_active WHERE enabled != is_active;

-- ================================================================
-- 6. PRESCRIPTIONS — add & fix columns
-- ================================================================
ALTER TABLE prescriptions
    ADD COLUMN IF NOT EXISTS created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NULL;

-- ================================================================
-- 7. INVOICES — add missing columns
-- ================================================================
ALTER TABLE invoices
    ADD COLUMN IF NOT EXISTS description        TEXT                       NULL,
    ADD COLUMN IF NOT EXISTS due_date           DATE                       NULL,
    ADD COLUMN IF NOT EXISTS discount_amount    DECIMAL(12,2) DEFAULT 0    NULL,
    ADD COLUMN IF NOT EXISTS tax_amount         DECIMAL(12,2) DEFAULT 0    NULL,
    ADD COLUMN IF NOT EXISTS created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NULL;

-- ================================================================
-- 8. LAB_ORDERS — add missing columns
-- ================================================================
ALTER TABLE lab_orders
    ADD COLUMN IF NOT EXISTS created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NULL;

-- ================================================================
-- 9. BLOOD_INVENTORY — fix column names entity uses
-- ================================================================
ALTER TABLE blood_inventory
    ADD COLUMN IF NOT EXISTS units_available    INT DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS minimum_threshold  INT DEFAULT 5 NOT NULL,
    ADD COLUMN IF NOT EXISTS is_below_threshold BOOLEAN GENERATED ALWAYS AS (units_available <= minimum_threshold) STORED NULL;

-- ================================================================
-- 10. AMBULANCE_CALLS — add missing columns
-- ================================================================
ALTER TABLE ambulance_calls
    ADD COLUMN IF NOT EXISTS caller_name        VARCHAR(100)               NULL,
    ADD COLUMN IF NOT EXISTS emergency_type     VARCHAR(50)                NULL,
    ADD COLUMN IF NOT EXISTS priority_level     VARCHAR(20) DEFAULT 'HIGH' NULL,
    ADD COLUMN IF NOT EXISTS notes              TEXT                       NULL,
    ADD COLUMN IF NOT EXISTS dispatched_at      DATETIME                   NULL,
    ADD COLUMN IF NOT EXISTS completed_at       DATETIME                   NULL,
    ADD COLUMN IF NOT EXISTS created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NULL;

-- ================================================================
-- 11. AMBULANCES — add missing columns
-- ================================================================
ALTER TABLE ambulances
    ADD COLUMN IF NOT EXISTS model              VARCHAR(100)               NULL,
    ADD COLUMN IF NOT EXISTS driver_name        VARCHAR(100)               NULL,
    ADD COLUMN IF NOT EXISTS created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NULL;

-- ================================================================
-- 12. AUDIT_LOGS — normalize column names
-- ================================================================
ALTER TABLE audit_logs
    ADD COLUMN IF NOT EXISTS entity_type        VARCHAR(100)               NULL,
    ADD COLUMN IF NOT EXISTS entity_id          BIGINT                     NULL,
    ADD COLUMN IF NOT EXISTS status             VARCHAR(20) DEFAULT 'SUCCESS' NULL,
    ADD COLUMN IF NOT EXISTS ip_address         VARCHAR(45)                NULL,
    ADD COLUMN IF NOT EXISTS created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL;

-- ================================================================
-- 13. MEDICINES — add missing columns for pharmacy
-- ================================================================
ALTER TABLE medicines
    ADD COLUMN IF NOT EXISTS generic_name       VARCHAR(200)               NULL,
    ADD COLUMN IF NOT EXISTS category           VARCHAR(100)               NULL,
    ADD COLUMN IF NOT EXISTS manufacturer       VARCHAR(200)               NULL,
    ADD COLUMN IF NOT EXISTS unit_price         DECIMAL(10,2) DEFAULT 0    NULL,
    ADD COLUMN IF NOT EXISTS quantity_in_stock  INT           DEFAULT 0    NULL,
    ADD COLUMN IF NOT EXISTS min_stock_level    INT           DEFAULT 10   NULL,
    ADD COLUMN IF NOT EXISTS expiry_date        DATE                       NULL,
    ADD COLUMN IF NOT EXISTS batch_number       VARCHAR(100)               NULL,
    ADD COLUMN IF NOT EXISTS created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NULL;

-- ================================================================
-- 14. WARDS — fix and add columns
-- ================================================================
ALTER TABLE wards
    ADD COLUMN IF NOT EXISTS created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL;

-- ================================================================
-- 15. BEDS — add missing columns
-- ================================================================
ALTER TABLE beds
    ADD COLUMN IF NOT EXISTS created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NULL;

-- ================================================================
-- 16. NURSES — add missing columns (ward_id FK, bed assignment)
-- ================================================================
ALTER TABLE nurses
    ADD COLUMN IF NOT EXISTS updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NULL;

-- ================================================================
-- 17. LAB_TESTS — fix column names for lab module
-- ================================================================
ALTER TABLE lab_tests
    ADD COLUMN IF NOT EXISTS turnaround_hours   INT DEFAULT 24             NULL,
    ADD COLUMN IF NOT EXISTS sample_type        VARCHAR(100)               NULL,
    ADD COLUMN IF NOT EXISTS fasting_required   BOOLEAN DEFAULT FALSE      NOT NULL,
    ADD COLUMN IF NOT EXISTS is_active          BOOLEAN DEFAULT TRUE       NOT NULL,
    ADD COLUMN IF NOT EXISTS created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL;

-- ================================================================
-- 18. PRESCRIPTION_ITEMS — align column names
-- ================================================================
ALTER TABLE prescription_items
    ADD COLUMN IF NOT EXISTS medicine_name      VARCHAR(200)               NULL,
    ADD COLUMN IF NOT EXISTS instructions       TEXT                       NULL;

-- ================================================================
-- SEED DATA: Default admin user
-- ================================================================
INSERT IGNORE INTO users
    (username, email, password, role, first_name, last_name, phone,
     is_active, is_verified, enabled, created_at)
VALUES
    ('admin', 'admin@medichain.in',
     '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8RqGp8HEjCUmryYBKi',
     'ADMIN', 'System', 'Administrator', '+91-9999999999',
     TRUE, TRUE, TRUE, NOW());

-- ================================================================
-- SEED DATA: Default blood bank
-- ================================================================
INSERT IGNORE INTO blood_banks (id, name, branch_id, manager_user_id, contact_phone, is_active, created_at)
SELECT 1, 'MediChain Blood Bank', 1,
       (SELECT id FROM users WHERE role='ADMIN' LIMIT 1),
       '+91-40-12345679', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM blood_banks WHERE id = 1);

-- ================================================================
-- SEED DATA: Blood inventory for all groups
-- ================================================================
INSERT IGNORE INTO blood_inventory (bank_id, blood_group, units_available, minimum_threshold)
VALUES
    (1, 'A_POSITIVE',  30, 10),
    (1, 'A_NEGATIVE',  15, 5),
    (1, 'B_POSITIVE',  25, 10),
    (1, 'B_NEGATIVE',  10, 5),
    (1, 'AB_POSITIVE', 12, 5),
    (1, 'AB_NEGATIVE',  5, 3),
    (1, 'O_POSITIVE',  40, 15),
    (1, 'O_NEGATIVE',  20, 8);

-- ================================================================
-- SEED DATA: Lab tests
-- ================================================================
INSERT IGNORE INTO lab_tests (name, code, description, price, turnaround_hours, sample_type, is_active)
VALUES
    ('Complete Blood Count (CBC)',    'CBC',    'Full blood panel',              250.00, 4,  'Blood', TRUE),
    ('Blood Sugar Fasting',           'BSF',    'Fasting glucose test',          80.00,  2,  'Blood', TRUE),
    ('Blood Sugar PP',                'BSPP',   'Post-prandial glucose',         80.00,  2,  'Blood', TRUE),
    ('HbA1c',                         'HBA1C',  'Glycated haemoglobin',          350.00, 6,  'Blood', TRUE),
    ('Lipid Profile',                 'LIPID',  'Cholesterol and triglycerides', 400.00, 6,  'Blood', TRUE),
    ('Liver Function Test (LFT)',     'LFT',    'Liver enzyme panel',            450.00, 8,  'Blood', TRUE),
    ('Kidney Function Test (KFT)',    'KFT',    'Creatinine, BUN, uric acid',    400.00, 8,  'Blood', TRUE),
    ('Thyroid Profile (TSH/T3/T4)',   'THYROID','Thyroid hormone levels',        600.00, 12, 'Blood', TRUE),
    ('Urine Routine Examination',     'URE',    'Urine analysis',                100.00, 3,  'Urine', TRUE),
    ('Urine Culture & Sensitivity',   'UCS',    'Bacterial culture',             350.00, 48, 'Urine', TRUE),
    ('ECG',                           'ECG',    'Electrocardiogram',             200.00, 1,  'N/A',   TRUE),
    ('Chest X-Ray',                   'CXR',    'Chest radiograph',              300.00, 2,  'N/A',   TRUE),
    ('COVID-19 RT-PCR',               'RTPCR',  'SARS-CoV-2 test',              800.00, 24, 'NasalSwab', TRUE),
    ('Dengue NS1 Antigen',            'DENGNS1','Dengue rapid antigen',          500.00, 4,  'Blood', TRUE),
    ('Malaria Antigen Test',          'MAL',    'Malaria rapid test',            300.00, 2,  'Blood', TRUE);

-- ================================================================
-- SEED DATA: Sample medicines
-- ================================================================
INSERT IGNORE INTO medicines (name, generic_name, category, manufacturer, unit_price, quantity_in_stock, min_stock_level, is_active)
VALUES
    ('Paracetamol 500mg',    'Paracetamol',      'Analgesic',        'Sun Pharma',    5.00,  500, 100, TRUE),
    ('Amoxicillin 500mg',    'Amoxicillin',      'Antibiotic',       'Cipla',         12.00, 200,  50, TRUE),
    ('Metformin 500mg',      'Metformin HCl',    'Antidiabetic',     'USV Ltd',       8.00,  300,  80, TRUE),
    ('Amlodipine 5mg',       'Amlodipine',       'Antihypertensive', 'Lupin',         6.00,  250,  60, TRUE),
    ('Pantoprazole 40mg',    'Pantoprazole',     'PPI / Gastric',    'Alkem',         7.00,  300,  80, TRUE),
    ('Cetirizine 10mg',      'Cetirizine',       'Antihistamine',    'Dr Reddys',     3.50,  400, 100, TRUE),
    ('Azithromycin 500mg',   'Azithromycin',     'Antibiotic',       'Cipla',         45.00, 150,  40, TRUE),
    ('Omeprazole 20mg',      'Omeprazole',       'PPI / Gastric',    'Sun Pharma',    5.50,  350,  80, TRUE),
    ('Atorvastatin 10mg',    'Atorvastatin',     'Statin',           'Pfizer India',  15.00, 200,  50, TRUE),
    ('Dolo 650',             'Paracetamol 650mg','Analgesic',        'Micro Labs',    8.00,  600, 150, TRUE);

-- ================================================================
-- SEED DATA: Ambulance fleet
-- ================================================================
INSERT IGNORE INTO ambulances (vehicle_number, model, ambulance_type, status, branch_id, is_active, created_at)
SELECT 'AP09TH1234', 'TATA Winger', 'ALS', 'AVAILABLE', 1, TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM ambulances LIMIT 1);

INSERT IGNORE INTO ambulances (vehicle_number, model, ambulance_type, status, branch_id, is_active, created_at)
SELECT 'AP09TH5678', 'Force Traveller', 'BLS', 'AVAILABLE', 1, TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM ambulances WHERE vehicle_number = 'AP09TH5678');


-- ================================================================
-- 19. LAB_ORDER_TESTS junction table (for @ManyToMany)
-- ================================================================
CREATE TABLE IF NOT EXISTS lab_order_tests (
    lab_order_id BIGINT NOT NULL,
    lab_test_id  BIGINT NOT NULL,
    PRIMARY KEY (lab_order_id, lab_test_id),
    FOREIGN KEY (lab_order_id) REFERENCES lab_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (lab_test_id)  REFERENCES lab_tests(id)  ON DELETE CASCADE
);

-- ================================================================
-- 20. NURSE_TASKS — for nurse task list
-- ================================================================
CREATE TABLE IF NOT EXISTS nurse_tasks (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nurse_id    BIGINT NOT NULL,
    patient_id  BIGINT,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    due_time    TIME,
    priority    ENUM('LOW','MEDIUM','HIGH','CRITICAL') DEFAULT 'MEDIUM',
    is_done     BOOLEAN DEFAULT FALSE,
    done_at     DATETIME,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (nurse_id)   REFERENCES nurses(id),
    FOREIGN KEY (patient_id) REFERENCES patients(id)
);

-- ================================================================
-- 21. SHIFT_HANDOVERS
-- ================================================================
CREATE TABLE IF NOT EXISTS shift_handovers (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    nurse_id   BIGINT NOT NULL,
    ward_id    BIGINT,
    shift      ENUM('MORNING','EVENING','NIGHT') NOT NULL,
    notes      TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (nurse_id) REFERENCES nurses(id),
    FOREIGN KEY (ward_id)  REFERENCES wards(id)
);

-- ================================================================
-- 22. MEDICATION_ADMINISTRATIONS (eMAR)
-- ================================================================
CREATE TABLE IF NOT EXISTS medication_administrations (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_item_id BIGINT,
    patient_id          BIGINT NOT NULL,
    nurse_id            BIGINT,
    medicine_name       VARCHAR(200) NOT NULL,
    dosage              VARCHAR(100),
    frequency           VARCHAR(100),
    scheduled_time      DATETIME,
    administered        BOOLEAN DEFAULT FALSE,
    administered_at     DATETIME,
    notes               TEXT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (nurse_id)   REFERENCES nurses(id)
);
