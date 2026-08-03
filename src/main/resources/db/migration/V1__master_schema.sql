-- ============================================================
-- MEDICHAIN HMS — MASTER SCHEMA (Clean Single Migration)
-- Matches ALL Java entities exactly. No ENUMs — all VARCHAR.
-- Run once on a fresh database.
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. HOSPITAL BRANCHES
CREATE TABLE IF NOT EXISTS hospital_branches (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(150) NOT NULL,
    code             VARCHAR(20)  UNIQUE NOT NULL,
    address          TEXT NOT NULL,
    city             VARCHAR(100) NOT NULL,
    state            VARCHAR(100) NOT NULL,
    pincode          VARCHAR(10)  NOT NULL,
    phone            VARCHAR(20)  NOT NULL,
    email            VARCHAR(100),
    latitude         DECIMAL(10,8),
    longitude        DECIMAL(11,8),
    is_active        BOOLEAN DEFAULT TRUE,
    established_date DATE,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. DEPARTMENTS
CREATE TABLE IF NOT EXISTS departments (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id    BIGINT NOT NULL,
    name         VARCHAR(100) NOT NULL,
    code         VARCHAR(20)  NOT NULL,
    description  TEXT,
    floor_number VARCHAR(10),
    is_active    BOOLEAN DEFAULT TRUE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id)
);

-- 3. SPECIALIZATIONS
CREATE TABLE IF NOT EXISTS specializations (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL UNIQUE,
    description   TEXT,
    department_id BIGINT,
    is_active     BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- 4. USERS (VARCHAR for role/gender/blood_group — matches entity)
CREATE TABLE IF NOT EXISTS users (
    id                         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username                   VARCHAR(50)  UNIQUE NOT NULL,
    email                      VARCHAR(100) UNIQUE NOT NULL,
    password                   VARCHAR(255) NOT NULL,
    role                       VARCHAR(30)  NOT NULL,
    first_name                 VARCHAR(100) NOT NULL,
    last_name                  VARCHAR(100) NOT NULL,
    phone                      VARCHAR(20),
    date_of_birth              DATE,
    gender                     VARCHAR(10),
    blood_group                VARCHAR(15),
    profile_photo_url          VARCHAR(500),
    branch_id                  BIGINT,
    preferred_language         VARCHAR(10)  DEFAULT 'en',
    is_active                  BOOLEAN DEFAULT TRUE,
    is_verified                BOOLEAN DEFAULT FALSE,
    email_verified             BOOLEAN DEFAULT FALSE,
    phone_verified             BOOLEAN DEFAULT FALSE,
    enabled                    BOOLEAN DEFAULT TRUE,
    approval_status            VARCHAR(20)  DEFAULT 'APPROVED',
    last_login                 DATETIME,
    password_reset_token       VARCHAR(255),
    password_reset_expiry      DATETIME,
    email_verification_token   VARCHAR(255),
    created_at                 TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                 TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id)
);

-- 5. DOCTORS
CREATE TABLE IF NOT EXISTS doctors (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                  BIGINT UNIQUE NOT NULL,
    license_number           VARCHAR(100) UNIQUE NOT NULL,
    license_document_url     VARCHAR(500),
    specialization_id        BIGINT,
    department_id            BIGINT,
    branch_id                BIGINT,
    qualification            VARCHAR(255),
    experience_years         INT DEFAULT 0,
    consultation_fee         DECIMAL(10,2) DEFAULT 0,
    bio                      TEXT,
    approval_status          VARCHAR(20) DEFAULT 'PENDING',
    rejection_reason         TEXT,
    approved_by              BIGINT,
    approved_at              DATETIME,
    background_check_status  VARCHAR(20) DEFAULT 'PENDING',
    background_check_notes   TEXT,
    is_active                BOOLEAN DEFAULT TRUE,
    created_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (specialization_id) REFERENCES specializations(id),
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id)
);

-- 6. NURSES
CREATE TABLE IF NOT EXISTS nurses (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id              BIGINT UNIQUE NOT NULL,
    license_number       VARCHAR(100) UNIQUE NOT NULL,
    license_document_url VARCHAR(500),
    department_id        BIGINT,
    assigned_doctor_id   BIGINT,
    ward                 VARCHAR(50),
    shift                VARCHAR(20),
    qualification        VARCHAR(255),
    experience_years     INT DEFAULT 0,
    approval_status      VARCHAR(20) DEFAULT 'PENDING',
    is_active            BOOLEAN DEFAULT TRUE,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (assigned_doctor_id) REFERENCES doctors(id)
);

-- 7. PATIENTS
CREATE TABLE IF NOT EXISTS patients (
    id                           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                      BIGINT UNIQUE NOT NULL,
    patient_id_number            VARCHAR(50) UNIQUE NOT NULL,
    emergency_contact_name       VARCHAR(100),
    emergency_contact_phone      VARCHAR(20),
    emergency_contact_relation   VARCHAR(50),
    insurance_provider           VARCHAR(100),
    insurance_policy_number      VARCHAR(100),
    insurance_document_url       VARCHAR(500),
    address                      TEXT,
    city                         VARCHAR(100),
    state                        VARCHAR(100),
    pincode                      VARCHAR(10),
    height_cm                    DECIMAL(5,2),
    weight_kg                    DECIMAL(5,2),
    known_allergies              TEXT,
    chronic_conditions           TEXT,
    is_active                    BOOLEAN DEFAULT TRUE,
    created_at                   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 8. WARDS
CREATE TABLE IF NOT EXISTS wards (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id  BIGINT,
    name       VARCHAR(100) NOT NULL,
    ward_type  VARCHAR(50),
    floor      VARCHAR(10),
    capacity   INT DEFAULT 0,
    is_active  BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id)
);

-- 9. BEDS
CREATE TABLE IF NOT EXISTS beds (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    ward_id    BIGINT NOT NULL,
    bed_number VARCHAR(20) NOT NULL,
    bed_type   VARCHAR(30) DEFAULT 'GENERAL',
    status     VARCHAR(20) DEFAULT 'AVAILABLE',
    patient_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ward_id) REFERENCES wards(id),
    FOREIGN KEY (patient_id) REFERENCES patients(id)
);

-- 10. DOCTOR SLOTS
CREATE TABLE IF NOT EXISTS doctor_slots (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id           BIGINT NOT NULL,
    slot_date           DATE,
    start_time          TIME NOT NULL,
    end_time            TIME NOT NULL,
    slot_type           VARCHAR(30) DEFAULT 'REGULAR',
    status              VARCHAR(20) DEFAULT 'AVAILABLE',
    max_patients        INT DEFAULT 1,
    current_patients    INT DEFAULT 0,
    duration_minutes    INT DEFAULT 30,
    day_of_week         VARCHAR(10),
    is_recurring        BOOLEAN DEFAULT FALSE,
    is_blocked          BOOLEAN DEFAULT FALSE,
    block_reason        VARCHAR(255),
    is_active           BOOLEAN DEFAULT TRUE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

-- 11. APPOINTMENTS
CREATE TABLE IF NOT EXISTS appointments (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_number  VARCHAR(30) UNIQUE,
    patient_id          BIGINT NOT NULL,
    doctor_id           BIGINT NOT NULL,
    department_id       BIGINT,
    branch_id           BIGINT,
    slot_id             BIGINT,
    appointment_date    DATE NOT NULL,
    appointment_time    TIME NOT NULL,
    duration_minutes    INT DEFAULT 30,
    type                VARCHAR(30) DEFAULT 'IN_PERSON',
    status              VARCHAR(20) DEFAULT 'PENDING',
    reason_for_visit    TEXT,
    symptoms            TEXT,
    notes               TEXT,
    rejection_reason    VARCHAR(500),
    follow_up_date      DATE,
    is_paid             BOOLEAN DEFAULT FALSE,
    payment_id          VARCHAR(100),
    payment_status      VARCHAR(20) DEFAULT 'UNPAID',
    razorpay_order_id   VARCHAR(100),
    is_emergency        BOOLEAN DEFAULT FALSE,
    checked_in_at       DATETIME,
    completed_at        DATETIME,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id)    REFERENCES patients(id),
    FOREIGN KEY (doctor_id)     REFERENCES doctors(id),
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (branch_id)     REFERENCES hospital_branches(id),
    FOREIGN KEY (slot_id)       REFERENCES doctor_slots(id)
);

-- 12. MEDICAL RECORDS
CREATE TABLE IF NOT EXISTS medical_records (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id         BIGINT NOT NULL,
    doctor_id          BIGINT,
    appointment_id     BIGINT,
    record_type        VARCHAR(50) DEFAULT 'VISIT',
    visit_date         DATE,
    record_date        DATE,
    chief_complaint    TEXT,
    symptoms           TEXT,
    diagnosis          TEXT,
    treatment_plan     TEXT,
    notes              TEXT,
    blood_pressure     VARCHAR(20),
    pulse_rate         INT,
    temperature        DECIMAL(5,2),
    weight_kg          DECIMAL(5,2),
    height_cm          DECIMAL(5,2),
    oxygen_saturation  DECIMAL(5,2),
    respiratory_rate   INT,
    blood_glucose      DECIMAL(6,2),
    is_active          BOOLEAN DEFAULT TRUE,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id)    REFERENCES patients(id),
    FOREIGN KEY (doctor_id)     REFERENCES doctors(id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

-- 13. PRESCRIPTIONS
CREATE TABLE IF NOT EXISTS prescriptions (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id        BIGINT,
    doctor_id         BIGINT,
    appointment_id    BIGINT,
    prescription_date DATE,
    valid_until       DATE,
    status            VARCHAR(20) DEFAULT 'PENDING',
    notes             TEXT,
    diagnosis_notes   TEXT,
    dispensed_by      BIGINT,
    dispensed_at      DATETIME,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id)    REFERENCES patients(id),
    FOREIGN KEY (doctor_id)     REFERENCES doctors(id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

-- 14. PRESCRIPTION ITEMS
CREATE TABLE IF NOT EXISTS prescription_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    medicine_name   VARCHAR(200) NOT NULL,
    dosage          VARCHAR(100),
    frequency       VARCHAR(100),
    duration_days   INT DEFAULT 7,
    instructions    TEXT,
    quantity        INT DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(id) ON DELETE CASCADE
);

-- 15. MEDICINES
CREATE TABLE IF NOT EXISTS medicines (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(200) NOT NULL,
    generic_name     VARCHAR(200),
    category         VARCHAR(100),
    manufacturer     VARCHAR(200),
    unit_price       DECIMAL(10,2) DEFAULT 0,
    price            DECIMAL(10,2) DEFAULT 0,
    quantity_in_stock INT DEFAULT 0,
    
    min_stock_level  INT DEFAULT 10,
    
    expiry_date      DATE,
    batch_number     VARCHAR(100),
    is_prescription_required BOOLEAN DEFAULT FALSE,
    is_active        BOOLEAN DEFAULT TRUE,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 16. LAB TESTS
CREATE TABLE IF NOT EXISTS lab_tests (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(200) NOT NULL,
    code             VARCHAR(50) UNIQUE,
    description      TEXT,
    price            DECIMAL(10,2) DEFAULT 0,
    turnaround_hours INT DEFAULT 24,
    sample_type      VARCHAR(100),
    fasting_required BOOLEAN DEFAULT FALSE,
    is_active        BOOLEAN DEFAULT TRUE,
    branch_id        BIGINT,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id)
);

-- 17. LAB ORDERS
CREATE TABLE IF NOT EXISTS lab_orders (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number        VARCHAR(30) UNIQUE,
    patient_id          BIGINT,
    doctor_id           BIGINT,
    appointment_id      BIGINT,
    status              VARCHAR(30) DEFAULT 'ORDERED',
    clinical_notes      TEXT,
    result_notes        TEXT,
    sample_collected_at DATETIME,
    completed_at        DATETIME,
    collected_by        BIGINT,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id)    REFERENCES patients(id),
    FOREIGN KEY (doctor_id)     REFERENCES doctors(id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

-- 18. LAB ORDER ↔ TEST (junction)
CREATE TABLE IF NOT EXISTS lab_order_tests (
    lab_order_id BIGINT NOT NULL,
    lab_test_id  BIGINT NOT NULL,
    PRIMARY KEY (lab_order_id, lab_test_id),
    FOREIGN KEY (lab_order_id) REFERENCES lab_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (lab_test_id)  REFERENCES lab_tests(id)  ON DELETE CASCADE
);

-- 19. LAB RESULTS
CREATE TABLE IF NOT EXISTS lab_results (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    lab_order_id   BIGINT,
    lab_test_id    BIGINT,
    result_value   TEXT,
    unit           VARCHAR(50),
    reference_range VARCHAR(100),
    is_abnormal    BOOLEAN DEFAULT FALSE,
    notes          TEXT,
    result_file_url VARCHAR(500),
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (lab_order_id) REFERENCES lab_orders(id),
    FOREIGN KEY (lab_test_id)  REFERENCES lab_tests(id)
);

-- 20. INVOICES
CREATE TABLE IF NOT EXISTS invoices (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number  VARCHAR(30) UNIQUE,
    patient_id      BIGINT,
    appointment_id  BIGINT,
    branch_id       BIGINT,
    description     TEXT,
    status          VARCHAR(20) DEFAULT 'PENDING',
    total_amount    DECIMAL(12,2) DEFAULT 0,
    amount_paid     DECIMAL(12,2) DEFAULT 0,
    discount_amount DECIMAL(12,2) DEFAULT 0,
    tax_amount      DECIMAL(12,2) DEFAULT 0,
    payment_method  VARCHAR(30),
    transaction_id  VARCHAR(100),
    razorpay_order_id VARCHAR(100),
    due_date        DATE,
    paid_at         DATETIME,
    notes           TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id)    REFERENCES patients(id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (branch_id)     REFERENCES hospital_branches(id)
);

CREATE INDEX idx_invoices_transaction ON invoices(transaction_id);

-- 21. INVOICE ITEMS
CREATE TABLE IF NOT EXISTS invoice_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id   BIGINT NOT NULL,
    description  VARCHAR(500) NOT NULL,
    quantity     INT DEFAULT 1,
    unit_price   DECIMAL(10,2) DEFAULT 0,
    total_price  DECIMAL(12,2) DEFAULT 0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

-- 22. BLOOD BANKS
CREATE TABLE IF NOT EXISTS blood_banks (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(200) NOT NULL,
    branch_id      BIGINT,
    manager_user_id BIGINT,
    contact_phone  VARCHAR(20),
    is_active      BOOLEAN DEFAULT TRUE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id)       REFERENCES hospital_branches(id),
    FOREIGN KEY (manager_user_id) REFERENCES users(id)
);

-- 23. BLOOD INVENTORY
CREATE TABLE IF NOT EXISTS blood_inventory (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    bank_id           BIGINT NOT NULL,
    blood_group       VARCHAR(15) NOT NULL,
    units_available   INT DEFAULT 0,
    
    minimum_threshold INT DEFAULT 5,
    last_updated      DATETIME,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bank_id) REFERENCES blood_banks(id)
);

-- 24. BLOOD DONATIONS
CREATE TABLE IF NOT EXISTS blood_donations (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id           BIGINT,
    bank_id            BIGINT,
    blood_group        VARCHAR(15) NOT NULL,
    units_donated      INT DEFAULT 1,
    donation_date      DATE,
    status             VARCHAR(20) DEFAULT 'PENDING',
    health_check_passed BOOLEAN,
    notes              TEXT,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (donor_id) REFERENCES users(id),
    FOREIGN KEY (bank_id)  REFERENCES blood_banks(id)
);

-- 25. BLOOD REQUESTS
CREATE TABLE IF NOT EXISTS blood_requests (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id     BIGINT,
    bank_id        BIGINT,
    blood_group    VARCHAR(15) NOT NULL,
    units_required INT,
    status         VARCHAR(20) DEFAULT 'PENDING',
    urgency        VARCHAR(20) DEFAULT 'NORMAL',
    notes          TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (bank_id)    REFERENCES blood_banks(id)
);

-- 26. ORGAN DONORS
CREATE TABLE IF NOT EXISTS organ_donors (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id                 BIGINT NOT NULL,
    organs_to_donate        TEXT,
    organs_for_donation     TEXT,
    medical_conditions      TEXT,
    medical_notes           TEXT,
    consent_document_url    VARCHAR(500),
    consent_given           BOOLEAN DEFAULT FALSE,
    registration_date       DATETIME,
    emergency_contact_name  VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    status                  VARCHAR(20) DEFAULT 'REGISTERED',
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 27. ORGAN REQUESTS
CREATE TABLE IF NOT EXISTS organ_requests (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id            BIGINT,
    doctor_id             BIGINT,
    organ_needed          VARCHAR(100),
    organ_type            VARCHAR(100),
    patient_name          VARCHAR(200),
    blood_group           VARCHAR(10),
    hospital_name         VARCHAR(200),
    contact_phone         VARCHAR(20),
    urgency_level         VARCHAR(20) DEFAULT 'MEDIUM',
    urgency               VARCHAR(20) DEFAULT 'NORMAL',
    medical_justification TEXT,
    notes                 TEXT,
    status                VARCHAR(20) DEFAULT 'WAITING',
    requested_at          DATETIME,
    fulfilled_at          DATETIME,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id)  REFERENCES doctors(id)
);

-- 28. AMBULANCES
CREATE TABLE IF NOT EXISTS ambulances (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number       VARCHAR(30) UNIQUE NOT NULL,
    model                VARCHAR(100),
    ambulance_type       VARCHAR(30),
    status               VARCHAR(20) DEFAULT 'AVAILABLE',
    driver_name          VARCHAR(100),
    driver_phone         VARCHAR(20),
    current_latitude     DECIMAL(10,8),
    current_longitude    DECIMAL(11,8),
    last_location_update DATETIME,
    branch_id            BIGINT,
    is_active            BOOLEAN DEFAULT TRUE,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id)
);

-- 29. AMBULANCE CALLS
CREATE TABLE IF NOT EXISTS ambulance_calls (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    ambulance_id     BIGINT,
    patient_id       BIGINT,
    operator_id      BIGINT,
    caller_name      VARCHAR(100),
    caller_phone     VARCHAR(20) NOT NULL,
    pickup_address   TEXT,
    pickup_latitude  DECIMAL(10,8),
    pickup_longitude DECIMAL(11,8),
    emergency_type   VARCHAR(50),
    priority_level   VARCHAR(20) DEFAULT 'HIGH',
    status           VARCHAR(30) DEFAULT 'PENDING',
    notes            TEXT,
    dispatched_at    DATETIME,
    arrived_at       DATETIME,
    completed_at     DATETIME,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ambulance_id) REFERENCES ambulances(id),
    FOREIGN KEY (patient_id)   REFERENCES patients(id),
    FOREIGN KEY (operator_id)  REFERENCES users(id)
);

-- 30. AUDIT LOGS
CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(100),
    action      VARCHAR(200),
    entity_type VARCHAR(100),
    entity_id   BIGINT,
    details     TEXT,
    status      VARCHAR(20) DEFAULT 'SUCCESS',
    ip_address  VARCHAR(45),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 31. NURSE TASKS
CREATE TABLE IF NOT EXISTS nurse_tasks (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nurse_id    BIGINT NOT NULL,
    patient_id  BIGINT,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    due_time    TIME,
    priority    VARCHAR(20) DEFAULT 'MEDIUM',
    is_done     BOOLEAN DEFAULT FALSE,
    done_at     DATETIME,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (nurse_id)   REFERENCES nurses(id),
    FOREIGN KEY (patient_id) REFERENCES patients(id)
);

-- 32. SHIFT HANDOVERS
CREATE TABLE IF NOT EXISTS shift_handovers (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    nurse_id   BIGINT NOT NULL,
    ward_id    BIGINT,
    shift      VARCHAR(20) NOT NULL,
    notes      TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (nurse_id) REFERENCES nurses(id),
    FOREIGN KEY (ward_id)  REFERENCES wards(id)
);

-- 33. MEDICATION ADMINISTRATIONS (eMAR)
CREATE TABLE IF NOT EXISTS medication_administrations (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_item_id BIGINT,
    patient_id           BIGINT NOT NULL,
    nurse_id             BIGINT,
    medicine_name        VARCHAR(200) NOT NULL,
    dosage               VARCHAR(100),
    frequency            VARCHAR(100),
    scheduled_time       DATETIME,
    administered         BOOLEAN DEFAULT FALSE,
    administered_at      DATETIME,
    notes                TEXT,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (nurse_id)   REFERENCES nurses(id)
);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- SEED DATA
-- ============================================================

-- Default hospital branch
INSERT IGNORE INTO hospital_branches (id, name, code, address, city, state, pincode, phone, email, is_active)
VALUES (1, 'MediChain Main Hospital', 'BRANCH-01', '12 Health Avenue, Jubilee Hills',
        'Hyderabad', 'Telangana', '500033', '+91-40-12345678', 'main@medichain.in', TRUE);

-- Default departments
INSERT IGNORE INTO departments (branch_id, name, code, description, floor_number, is_active) VALUES
(1, 'General Medicine', 'GEN-MED', 'General OPD and medicine', '1', TRUE),
(1, 'Cardiology',       'CARDIO',  'Heart and cardiovascular care', '2', TRUE),
(1, 'Orthopaedics',     'ORTHO',   'Bone and joint care', '2', TRUE),
(1, 'Gynaecology',      'GYNAE',   'Womens health and obstetrics', '3', TRUE),
(1, 'Paediatrics',      'PAEDS',   'Child healthcare', '3', TRUE),
(1, 'Neurology',        'NEURO',   'Brain and nervous system', '4', TRUE),
(1, 'Emergency',        'ER',      '24x7 Emergency and Trauma', 'G', TRUE),
(1, 'Radiology',        'RADIO',   'Imaging and diagnostics', 'B', TRUE),
(1, 'Pathology Lab',    'LAB',     'Blood tests and pathology', 'B', TRUE),
(1, 'Pharmacy',         'PHARMA',  'In-house dispensary', 'G', TRUE);

-- Specializations
INSERT IGNORE INTO specializations (name, description, is_active) VALUES
('General Medicine',   'Diagnosis and treatment of adult diseases', TRUE),
('Cardiology',         'Heart and cardiovascular system', TRUE),
('Orthopaedics',       'Bone, joint and musculoskeletal disorders', TRUE),
('Gynaecology',        'Female reproductive system and obstetrics', TRUE),
('Paediatrics',        'Medical care for infants and children', TRUE),
('Neurology',          'Disorders of the nervous system', TRUE),
('Dermatology',        'Skin, hair and nail conditions', TRUE),
('ENT',                'Ear, nose and throat disorders', TRUE),
('Ophthalmology',      'Eye disorders and vision care', TRUE),
('Psychiatry',         'Mental health and behavioural disorders', TRUE),
('Urology',            'Urinary tract disorders', TRUE),
('Gastroenterology',   'Digestive system disorders', TRUE),
('Pulmonology',        'Lung and respiratory disorders', TRUE),
('Endocrinology',      'Hormonal and metabolic disorders', TRUE),
('Oncology',           'Cancer diagnosis and treatment', TRUE),
('Emergency Medicine', '24x7 Emergency and Trauma care', TRUE);

-- Sample medicines
INSERT IGNORE INTO medicines (name, generic_name, category, manufacturer, unit_price, price, quantity_in_stock, min_stock_level, is_active) VALUES
('Paracetamol 500mg', 'Paracetamol',   'Analgesic',         'Sun Pharma',   5.00,  5.00,  500, 100, TRUE),
('Amoxicillin 500mg', 'Amoxicillin',   'Antibiotic',        'Cipla',        12.00, 12.00, 200,  50, TRUE),
('Metformin 500mg',   'Metformin HCl', 'Antidiabetic',      'USV Ltd',      8.00,  8.00,  300,  80, TRUE),
('Amlodipine 5mg',    'Amlodipine',    'Antihypertensive',  'Lupin',        6.00,  6.00,  250,  60, TRUE),
('Pantoprazole 40mg', 'Pantoprazole',  'PPI / Gastric',     'Alkem',        7.00,  7.00,  300,  80, TRUE),
('Cetirizine 10mg',   'Cetirizine',    'Antihistamine',     'Dr Reddys',    3.50,  3.50,  400, 100, TRUE),
('Azithromycin 500mg','Azithromycin',  'Antibiotic',        'Cipla',        45.00, 45.00, 150,  40, TRUE),
('Dolo 650',          'Paracetamol',   'Analgesic',         'Micro Labs',   8.00,  8.00,  600, 150, TRUE);

-- Sample lab tests
INSERT IGNORE INTO lab_tests (name, code, description, price, turnaround_hours, sample_type, is_active) VALUES
('Complete Blood Count',      'CBC',     'Full blood panel',            250.00,  4,  'Blood', TRUE),
('Blood Glucose (Fasting)',   'FBS',     'Fasting blood sugar',         80.00,   2,  'Blood', TRUE),
('Lipid Profile',             'LIPID',   'Cholesterol and triglycerides',400.00,  6,  'Blood', TRUE),
('Liver Function Test',       'LFT',     'ALT, AST, Bilirubin',         350.00,  6,  'Blood', TRUE),
('Kidney Function Test',      'KFT',     'Creatinine, BUN, uric acid',  400.00,  8,  'Blood', TRUE),
('Thyroid Profile',           'THYROID', 'TSH, T3, T4 levels',          600.00, 12,  'Blood', TRUE),
('Urine Routine',             'URE',     'Urine analysis',              100.00,  3,  'Urine', TRUE),
('ECG',                       'ECG',     'Electrocardiogram',           200.00,  1,  'N/A',   TRUE),
('Chest X-Ray',               'CXR',     'Chest radiograph',            300.00,  2,  'N/A',   TRUE),
('COVID-19 RT-PCR',           'RTPCR',   'SARS-CoV-2 test',             800.00, 24,  'NasalSwab', TRUE);

-- Sample ambulances
INSERT IGNORE INTO ambulances (vehicle_number, model, ambulance_type, status, branch_id, is_active)
VALUES
('AP09TH1234', 'TATA Winger',     'ALS', 'AVAILABLE', 1, TRUE),
('AP09TH5678', 'Force Traveller', 'BLS', 'AVAILABLE', 1, TRUE);

-- Default blood bank
INSERT IGNORE INTO blood_banks (id, name, branch_id, contact_phone, is_active)
VALUES (1, 'MediChain Blood Bank', 1, '+91-40-12345679', TRUE);

-- Blood inventory (all groups)
INSERT IGNORE INTO blood_inventory (bank_id, blood_group, units_available, minimum_threshold) VALUES
(1, 'A+',  25, 5), (1, 'A-',  10, 3),
(1, 'B+',  30, 5), (1, 'B-',  8,  3),
(1, 'AB+', 15, 3), (1, 'AB-', 5,  2),
(1, 'O+',  35, 8), (1, 'O-',  12, 4);
