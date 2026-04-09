-- ==========================================
-- HOSPITAL MANAGEMENT SYSTEM - FULL DB SCHEMA
-- Version: V1 - Initial Schema
-- ==========================================

-- ==========================================
-- 1. HOSPITAL BRANCHES
-- ==========================================
CREATE TABLE hospital_branches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    is_active BOOLEAN DEFAULT TRUE,
    established_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ==========================================
-- 2. DEPARTMENTS
-- ==========================================
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL,
    description TEXT,
    floor_number VARCHAR(10),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id)
);

-- ==========================================
-- 3. SPECIALIZATIONS
-- ==========================================
CREATE TABLE specializations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    department_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

-- ==========================================
-- 4. USERS (Base table for all roles)
-- ==========================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','DOCTOR','NURSE','PATIENT','BLOOD_BANK_MANAGER',
              'ORGAN_DONOR','BLOOD_DONOR','AMBULANCE_OPERATOR','LAB_TECHNICIAN',
              'PHARMACIST','RECEPTIONIST') NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    date_of_birth DATE,
    gender ENUM('MALE','FEMALE','OTHER'),
    blood_group ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-'),
    profile_photo_url VARCHAR(500),
    branch_id BIGINT,
    preferred_language VARCHAR(10) DEFAULT 'en',
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    last_login TIMESTAMP,
    password_reset_token VARCHAR(255),
    password_reset_expiry TIMESTAMP,
    email_verification_token VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id)
);

-- ==========================================
-- 5. DOCTORS
-- ==========================================
CREATE TABLE doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    license_number VARCHAR(100) UNIQUE NOT NULL,
    license_document_url VARCHAR(500),
    specialization_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    qualification VARCHAR(255) NOT NULL,
    experience_years INT DEFAULT 0,
    consultation_fee DECIMAL(10,2) NOT NULL,
    bio TEXT,
    approval_status ENUM('PENDING','APPROVED','REJECTED','SUSPENDED') DEFAULT 'PENDING',
    approved_by BIGINT,
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    background_check_status ENUM('PENDING','PASSED','FAILED') DEFAULT 'PENDING',
    background_check_notes TEXT,
    rating DECIMAL(3,2) DEFAULT 0.00,
    total_reviews INT DEFAULT 0,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (specialization_id) REFERENCES specializations(id),
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- ==========================================
-- 6. NURSES
-- ==========================================
CREATE TABLE nurses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    license_number VARCHAR(100) UNIQUE NOT NULL,
    license_document_url VARCHAR(500),
    department_id BIGINT NOT NULL,
    assigned_doctor_id BIGINT,
    ward VARCHAR(50),
    shift ENUM('MORNING','AFTERNOON','NIGHT','ROTATING'),
    qualification VARCHAR(255),
    experience_years INT DEFAULT 0,
    approval_status ENUM('PENDING','APPROVED','REJECTED','SUSPENDED') DEFAULT 'PENDING',
    approved_by BIGINT,
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (assigned_doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- ==========================================
-- 7. PATIENTS
-- ==========================================
CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    patient_id_number VARCHAR(50) UNIQUE NOT NULL,
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    emergency_contact_relation VARCHAR(50),
    insurance_provider VARCHAR(100),
    insurance_policy_number VARCHAR(100),
    insurance_document_url VARCHAR(500),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    pincode VARCHAR(10),
    allergies TEXT,
    chronic_conditions TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==========================================
-- 8. DOCTOR SLOTS
-- ==========================================
CREATE TABLE doctor_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    max_patients INT DEFAULT 1,
    booked_count INT DEFAULT 0,
    slot_type ENUM('REGULAR','EMERGENCY','FOLLOW_UP') DEFAULT 'REGULAR',
    status ENUM('AVAILABLE','BLOCKED','FULLY_BOOKED','CANCELLED') DEFAULT 'AVAILABLE',
    blocked_reason TEXT,
    branch_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id),
    UNIQUE KEY unique_slot (doctor_id, slot_date, start_time)
);

-- ==========================================
-- 9. DOCTOR SCHEDULE TEMPLATE (Weekly recurring)
-- ==========================================
CREATE TABLE doctor_schedule_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    day_of_week ENUM('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INT DEFAULT 30,
    max_patients_per_slot INT DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

-- ==========================================
-- 10. APPOINTMENTS
-- ==========================================
CREATE TABLE appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_number VARCHAR(50) UNIQUE NOT NULL,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    slot_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    appointment_type ENUM('CONSULTATION','FOLLOW_UP','EMERGENCY','ROUTINE_CHECKUP') DEFAULT 'CONSULTATION',
    status ENUM('PENDING','CONFIRMED','REJECTED','CANCELLED','COMPLETED','NO_SHOW') DEFAULT 'PENDING',
    reason_for_visit TEXT,
    doctor_notes TEXT,
    rejection_reason TEXT,
    cancellation_reason TEXT,
    cancelled_by BIGINT,
    symptoms TEXT,
    is_first_visit BOOLEAN DEFAULT TRUE,
    consultation_fee DECIMAL(10,2),
    payment_status ENUM('PENDING','PAID','REFUNDED','WAIVED') DEFAULT 'PENDING',
    reminder_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (slot_id) REFERENCES doctor_slots(id),
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id),
    FOREIGN KEY (cancelled_by) REFERENCES users(id)
);

-- ==========================================
-- 11. PATIENT MEDICAL RECORDS
-- ==========================================
CREATE TABLE medical_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    appointment_id BIGINT,
    doctor_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    chief_complaint TEXT,
    diagnosis TEXT,
    treatment_plan TEXT,
    notes TEXT,
    follow_up_date DATE,
    blood_pressure VARCHAR(20),
    temperature DECIMAL(4,1),
    pulse_rate INT,
    weight DECIMAL(5,2),
    height DECIMAL(5,2),
    oxygen_saturation DECIMAL(4,1),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

-- ==========================================
-- 12. PRESCRIPTIONS
-- ==========================================
CREATE TABLE prescriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_number VARCHAR(50) UNIQUE NOT NULL,
    medical_record_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    prescription_date DATE NOT NULL,
    valid_until DATE,
    notes TEXT,
    status ENUM('ACTIVE','DISPENSED','EXPIRED','CANCELLED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (medical_record_id) REFERENCES medical_records(id),
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id)
);

-- ==========================================
-- 13. PRESCRIPTION ITEMS
-- ==========================================
CREATE TABLE prescription_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    medicine_name VARCHAR(200) NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    frequency VARCHAR(100) NOT NULL,
    duration VARCHAR(100) NOT NULL,
    instructions TEXT,
    quantity INT,
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(id)
);

-- ==========================================
-- 14. BLOOD BANK INVENTORY
-- ==========================================
CREATE TABLE blood_bank_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    blood_group ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-') NOT NULL,
    units_available INT DEFAULT 0,
    units_reserved INT DEFAULT 0,
    expiry_date DATE,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id),
    UNIQUE KEY unique_blood_branch (branch_id, blood_group)
);

-- ==========================================
-- 15. BLOOD DONORS
-- ==========================================
CREATE TABLE blood_donors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    donor_id_number VARCHAR(50) UNIQUE NOT NULL,
    blood_group ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-') NOT NULL,
    weight DECIMAL(5,2),
    last_donation_date DATE,
    total_donations INT DEFAULT 0,
    is_eligible BOOLEAN DEFAULT TRUE,
    ineligibility_reason TEXT,
    medical_conditions TEXT,
    registration_date DATE,
    preferred_branch_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (preferred_branch_id) REFERENCES hospital_branches(id)
);

-- ==========================================
-- 16. BLOOD REQUESTS
-- ==========================================
CREATE TABLE blood_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_number VARCHAR(50) UNIQUE NOT NULL,
    requester_type ENUM('HOSPITAL','INDIVIDUAL') NOT NULL,
    requester_user_id BIGINT,
    branch_id BIGINT NOT NULL,
    patient_name VARCHAR(100),
    blood_group ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-') NOT NULL,
    units_required INT NOT NULL,
    units_approved INT DEFAULT 0,
    urgency ENUM('NORMAL','URGENT','CRITICAL') DEFAULT 'NORMAL',
    reason TEXT,
    required_by_date DATE,
    status ENUM('PENDING','APPROVED','PARTIALLY_APPROVED','REJECTED','FULFILLED','CANCELLED') DEFAULT 'PENDING',
    approved_by BIGINT,
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (requester_user_id) REFERENCES users(id),
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- ==========================================
-- 17. ORGAN DONORS
-- ==========================================
CREATE TABLE organ_donors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    donor_id_number VARCHAR(50) UNIQUE NOT NULL,
    is_deceased BOOLEAN DEFAULT FALSE,
    registration_date DATE NOT NULL,
    consent_form_url VARCHAR(500),
    medical_report_url VARCHAR(500),
    notes TEXT,
    status ENUM('ACTIVE','INACTIVE','DONATED','DECEASED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==========================================
-- 18. ORGAN DONOR ORGANS
-- ==========================================
CREATE TABLE organ_donor_organs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id BIGINT NOT NULL,
    organ_type ENUM('HEART','LIVER','KIDNEY_LEFT','KIDNEY_RIGHT','LUNGS',
                    'PANCREAS','INTESTINE','CORNEA','SKIN','BONE_MARROW','OTHER') NOT NULL,
    status ENUM('AVAILABLE','MATCHED','DONATED','UNAVAILABLE') DEFAULT 'AVAILABLE',
    notes TEXT,
    FOREIGN KEY (donor_id) REFERENCES organ_donors(id)
);

-- ==========================================
-- 19. ORGAN REQUESTS
-- ==========================================
CREATE TABLE organ_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_number VARCHAR(50) UNIQUE NOT NULL,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    organ_type ENUM('HEART','LIVER','KIDNEY_LEFT','KIDNEY_RIGHT','LUNGS',
                    'PANCREAS','INTESTINE','CORNEA','SKIN','BONE_MARROW','OTHER') NOT NULL,
    urgency ENUM('NORMAL','URGENT','CRITICAL') DEFAULT 'NORMAL',
    medical_justification TEXT,
    status ENUM('PENDING','MATCHED','APPROVED','REJECTED','COMPLETED','CANCELLED') DEFAULT 'PENDING',
    matched_donor_id BIGINT,
    medical_documents_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id),
    FOREIGN KEY (matched_donor_id) REFERENCES organ_donors(id)
);

-- ==========================================
-- 20. AMBULANCES
-- ==========================================
CREATE TABLE ambulances (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    vehicle_number VARCHAR(30) UNIQUE NOT NULL,
    vehicle_type ENUM('BASIC','ADVANCED','NEONATAL','CARDIAC','ICU') NOT NULL,
    driver_name VARCHAR(100),
    driver_phone VARCHAR(20),
    operator_id BIGINT,
    current_latitude DECIMAL(10, 8),
    current_longitude DECIMAL(11, 8),
    status ENUM('AVAILABLE','DISPATCHED','EN_ROUTE','AT_SCENE','RETURNING','MAINTENANCE') DEFAULT 'AVAILABLE',
    last_location_update TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id),
    FOREIGN KEY (operator_id) REFERENCES users(id)
);

-- ==========================================
-- 21. AMBULANCE REQUESTS
-- ==========================================
CREATE TABLE ambulance_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_number VARCHAR(50) UNIQUE NOT NULL,
    requested_by BIGINT NOT NULL,
    ambulance_id BIGINT,
    branch_id BIGINT NOT NULL,
    patient_name VARCHAR(100),
    patient_phone VARCHAR(20),
    pickup_address TEXT NOT NULL,
    pickup_latitude DECIMAL(10, 8),
    pickup_longitude DECIMAL(11, 8),
    destination_address TEXT,
    emergency_type ENUM('ACCIDENT','CARDIAC','MATERNITY','GENERAL','OTHER') NOT NULL,
    status ENUM('PENDING','ASSIGNED','DISPATCHED','EN_ROUTE','AT_SCENE','COMPLETED','CANCELLED') DEFAULT 'PENDING',
    priority ENUM('LOW','MEDIUM','HIGH','CRITICAL') DEFAULT 'MEDIUM',
    notes TEXT,
    assigned_at TIMESTAMP,
    dispatched_at TIMESTAMP,
    arrived_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (requested_by) REFERENCES users(id),
    FOREIGN KEY (ambulance_id) REFERENCES ambulances(id),
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id)
);

-- ==========================================
-- 22. PHARMACY / MEDICINES
-- ==========================================
CREATE TABLE medicines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    generic_name VARCHAR(200),
    manufacturer VARCHAR(200),
    category VARCHAR(100),
    unit VARCHAR(50),
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT DEFAULT 0,
    reorder_level INT DEFAULT 10,
    expiry_date DATE,
    batch_number VARCHAR(100),
    is_prescription_required BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id)
);

-- ==========================================
-- 23. PHARMACY DISPENSE
-- ==========================================
CREATE TABLE pharmacy_dispenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    pharmacist_id BIGINT NOT NULL,
    dispensed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2),
    status ENUM('PENDING','DISPENSED','PARTIALLY_DISPENSED','CANCELLED') DEFAULT 'PENDING',
    notes TEXT,
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(id),
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (pharmacist_id) REFERENCES users(id)
);

-- ==========================================
-- 24. LAB TESTS
-- ==========================================
CREATE TABLE lab_tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    category VARCHAR(100),
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    turnaround_hours INT DEFAULT 24,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 25. LAB ORDERS
-- ==========================================
CREATE TABLE lab_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    technician_id BIGINT,
    medical_record_id BIGINT,
    order_date DATE NOT NULL,
    status ENUM('ORDERED','SAMPLE_COLLECTED','IN_PROGRESS','COMPLETED','CANCELLED') DEFAULT 'ORDERED',
    priority ENUM('ROUTINE','URGENT','STAT') DEFAULT 'ROUTINE',
    notes TEXT,
    total_amount DECIMAL(10,2),
    payment_status ENUM('PENDING','PAID') DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id),
    FOREIGN KEY (technician_id) REFERENCES users(id),
    FOREIGN KEY (medical_record_id) REFERENCES medical_records(id)
);

-- ==========================================
-- 26. LAB ORDER ITEMS
-- ==========================================
CREATE TABLE lab_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    test_id BIGINT NOT NULL,
    result_value TEXT,
    result_unit VARCHAR(50),
    normal_range VARCHAR(100),
    is_abnormal BOOLEAN DEFAULT FALSE,
    result_document_url VARCHAR(500),
    result_notes TEXT,
    completed_at TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES lab_orders(id),
    FOREIGN KEY (test_id) REFERENCES lab_tests(id)
);

-- ==========================================
-- 27. DOCUMENTS (Generic document store)
-- ==========================================
CREATE TABLE documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    document_type ENUM('LICENSE','INSURANCE','LAB_REPORT','PRESCRIPTION','CONSENT_FORM',
                        'MEDICAL_CERTIFICATE','ID_PROOF','OTHER') NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    ocr_text TEXT,
    is_verified BOOLEAN DEFAULT FALSE,
    verified_by BIGINT,
    verified_at TIMESTAMP,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_user_id) REFERENCES users(id),
    FOREIGN KEY (uploaded_by) REFERENCES users(id),
    FOREIGN KEY (verified_by) REFERENCES users(id)
);

-- ==========================================
-- 28. BILLING
-- ==========================================
CREATE TABLE bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_number VARCHAR(50) UNIQUE NOT NULL,
    patient_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    appointment_id BIGINT,
    lab_order_id BIGINT,
    bill_date DATE NOT NULL,
    due_date DATE,
    subtotal DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    paid_amount DECIMAL(10,2) DEFAULT 0.00,
    status ENUM('DRAFT','PENDING','PARTIALLY_PAID','PAID','OVERDUE','CANCELLED','REFUNDED') DEFAULT 'PENDING',
    notes TEXT,
    generated_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id),
    FOREIGN KEY (lab_order_id) REFERENCES lab_orders(id),
    FOREIGN KEY (generated_by) REFERENCES users(id)
);

-- ==========================================
-- 29. BILL ITEMS
-- ==========================================
CREATE TABLE bill_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bill_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    category ENUM('CONSULTATION','LAB','PHARMACY','AMBULANCE','PROCEDURE','OTHER') NOT NULL,
    quantity INT DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES bills(id)
);

-- ==========================================
-- 30. PAYMENTS
-- ==========================================
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_number VARCHAR(50) UNIQUE NOT NULL,
    bill_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CASH','CARD','UPI','NET_BANKING','INSURANCE','RAZORPAY') NOT NULL,
    razorpay_order_id VARCHAR(255),
    razorpay_payment_id VARCHAR(255),
    razorpay_signature VARCHAR(500),
    status ENUM('PENDING','SUCCESS','FAILED','REFUNDED') DEFAULT 'PENDING',
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    FOREIGN KEY (bill_id) REFERENCES bills(id),
    FOREIGN KEY (patient_id) REFERENCES patients(id)
);

-- ==========================================
-- 31. NOTIFICATIONS
-- ==========================================
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('APPOINTMENT','BLOOD_REQUEST','ORGAN_REQUEST','AMBULANCE',
              'PAYMENT','LAB_RESULT','PRESCRIPTION','APPROVAL','SYSTEM') NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    email_sent BOOLEAN DEFAULT FALSE,
    sms_sent BOOLEAN DEFAULT FALSE,
    reference_id BIGINT,
    reference_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==========================================
-- 32. AUDIT LOGS
-- ==========================================
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id BIGINT,
    old_values JSON,
    new_values JSON,
    ip_address VARCHAR(50),
    user_agent TEXT,
    branch_id BIGINT,
    status ENUM('SUCCESS','FAILURE') DEFAULT 'SUCCESS',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==========================================
-- 33. DOCTOR REVIEWS
-- ==========================================
CREATE TABLE doctor_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    appointment_id BIGINT UNIQUE NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    is_anonymous BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (patient_id) REFERENCES patients(id),
    FOREIGN KEY (appointment_id) REFERENCES appointments(id)
);

-- ==========================================
-- 34. USER SESSIONS (JWT refresh tokens)
-- ==========================================
CREATE TABLE user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    refresh_token VARCHAR(500) UNIQUE NOT NULL,
    device_info TEXT,
    ip_address VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ==========================================
-- INDEXES FOR PERFORMANCE
-- ==========================================
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_appointments_date ON appointments(appointment_date);
CREATE INDEX idx_appointments_patient ON appointments(patient_id);
CREATE INDEX idx_appointments_doctor ON appointments(doctor_id);
CREATE INDEX idx_doctor_slots_date ON doctor_slots(slot_date);
CREATE INDEX idx_doctor_slots_doctor ON doctor_slots(doctor_id);
CREATE INDEX idx_blood_requests_blood_group ON blood_requests(blood_group);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_date ON audit_logs(created_at);
CREATE INDEX idx_notifications_user ON notifications(user_id, is_read);
CREATE INDEX idx_payments_bill ON payments(bill_id);
