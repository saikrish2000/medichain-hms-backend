-- ==========================================
-- V2 - NEW MODULES SCHEMA
-- Medical Shop, Diagnostics, Nurse Service, Home Nurse Booking
-- ==========================================

-- ==========================================
-- 35. MEDICAL SHOPS
-- ==========================================
CREATE TABLE medical_shops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    shop_name VARCHAR(200) NOT NULL,
    shop_code VARCHAR(30) UNIQUE NOT NULL,
    license_number VARCHAR(100) UNIQUE NOT NULL,
    license_document_url VARCHAR(500),
    gst_number VARCHAR(50),
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    opening_time TIME,
    closing_time TIME,
    is_open_24hrs BOOLEAN DEFAULT FALSE,
    delivery_available BOOLEAN DEFAULT TRUE,
    pickup_available BOOLEAN DEFAULT TRUE,
    delivery_radius_km DECIMAL(5,2) DEFAULT 5.00,
    delivery_charge DECIMAL(10,2) DEFAULT 0.00,
    min_order_amount DECIMAL(10,2) DEFAULT 0.00,
    approval_status ENUM('PENDING','APPROVED','REJECTED','SUSPENDED') DEFAULT 'PENDING',
    approved_by BIGINT,
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    rating DECIMAL(3,2) DEFAULT 0.00,
    total_reviews INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_user_id) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- ==========================================
-- 36. MEDICAL SHOP INVENTORY
-- ==========================================
CREATE TABLE shop_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shop_id BIGINT NOT NULL,
    medicine_name VARCHAR(200) NOT NULL,
    generic_name VARCHAR(200),
    manufacturer VARCHAR(200),
    category VARCHAR(100),
    unit VARCHAR(50),
    price DECIMAL(10,2) NOT NULL,
    mrp DECIMAL(10,2),
    stock_quantity INT DEFAULT 0,
    reorder_level INT DEFAULT 10,
    expiry_date DATE,
    batch_number VARCHAR(100),
    is_prescription_required BOOLEAN DEFAULT FALSE,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (shop_id) REFERENCES medical_shops(id)
);

-- ==========================================
-- 37. MEDICINE ORDERS (from medical shop)
-- ==========================================
CREATE TABLE medicine_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    patient_user_id BIGINT NOT NULL,
    shop_id BIGINT NOT NULL,
    prescription_document_url VARCHAR(500),
    prescription_verified BOOLEAN DEFAULT FALSE,
    delivery_type ENUM('DELIVERY','PICKUP') NOT NULL,
    delivery_address TEXT,
    delivery_latitude DECIMAL(10,8),
    delivery_longitude DECIMAL(11,8),
    subtotal DECIMAL(10,2) NOT NULL,
    delivery_charge DECIMAL(10,2) DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING','CONFIRMED','PROCESSING','OUT_FOR_DELIVERY',
                'READY_FOR_PICKUP','DELIVERED','PICKED_UP','CANCELLED','REJECTED') DEFAULT 'PENDING',
    payment_status ENUM('PENDING','PAID','REFUNDED') DEFAULT 'PENDING',
    payment_method ENUM('CASH','UPI','CARD','RAZORPAY'),
    razorpay_order_id VARCHAR(255),
    razorpay_payment_id VARCHAR(255),
    rejection_reason TEXT,
    estimated_delivery_time TIMESTAMP,
    delivered_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_user_id) REFERENCES users(id),
    FOREIGN KEY (shop_id) REFERENCES medical_shops(id)
);

-- ==========================================
-- 38. MEDICINE ORDER ITEMS
-- ==========================================
CREATE TABLE medicine_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    shop_inventory_id BIGINT NOT NULL,
    medicine_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES medicine_orders(id),
    FOREIGN KEY (shop_inventory_id) REFERENCES shop_inventory(id)
);

-- ==========================================
-- 39. DIAGNOSTIC CENTERS
-- ==========================================
CREATE TABLE diagnostic_centers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_user_id BIGINT NOT NULL,
    center_name VARCHAR(200) NOT NULL,
    center_code VARCHAR(30) UNIQUE NOT NULL,
    license_number VARCHAR(100) UNIQUE NOT NULL,
    license_document_url VARCHAR(500),
    nabl_accredited BOOLEAN DEFAULT FALSE,
    nabl_certificate_url VARCHAR(500),
    address TEXT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    opening_time TIME,
    closing_time TIME,
    home_collection_available BOOLEAN DEFAULT TRUE,
    home_collection_charge DECIMAL(10,2) DEFAULT 0.00,
    home_collection_radius_km DECIMAL(5,2) DEFAULT 10.00,
    approval_status ENUM('PENDING','APPROVED','REJECTED','SUSPENDED') DEFAULT 'PENDING',
    approved_by BIGINT,
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    rating DECIMAL(3,2) DEFAULT 0.00,
    total_reviews INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_user_id) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- ==========================================
-- 40. DIAGNOSTIC CENTER TESTS (catalog)
-- ==========================================
CREATE TABLE diagnostic_center_tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    center_id BIGINT NOT NULL,
    test_name VARCHAR(200) NOT NULL,
    test_code VARCHAR(50),
    category VARCHAR(100),
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    home_collection_available BOOLEAN DEFAULT TRUE,
    turnaround_hours INT DEFAULT 24,
    fasting_required BOOLEAN DEFAULT FALSE,
    preparation_notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (center_id) REFERENCES diagnostic_centers(id)
);

-- ==========================================
-- 41. DIAGNOSTIC BOOKINGS
-- ==========================================
CREATE TABLE diagnostic_bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_number VARCHAR(50) UNIQUE NOT NULL,
    patient_user_id BIGINT NOT NULL,
    center_id BIGINT NOT NULL,
    booking_type ENUM('WALK_IN','HOME_COLLECTION') NOT NULL,
    booking_date DATE NOT NULL,
    booking_time TIME NOT NULL,
    collection_address TEXT,
    collection_latitude DECIMAL(10,8),
    collection_longitude DECIMAL(11,8),
    phlebotomist_id BIGINT,
    phlebotomist_latitude DECIMAL(10,8),
    phlebotomist_longitude DECIMAL(11,8),
    phlebotomist_last_location_update TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL,
    home_collection_charge DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING','CONFIRMED','SAMPLE_COLLECTED','IN_PROGRESS',
                'REPORTS_READY','COMPLETED','CANCELLED') DEFAULT 'PENDING',
    payment_status ENUM('PENDING','PAID','REFUNDED') DEFAULT 'PENDING',
    payment_method ENUM('CASH','UPI','CARD','RAZORPAY'),
    razorpay_order_id VARCHAR(255),
    razorpay_payment_id VARCHAR(255),
    report_document_url VARCHAR(500),
    report_uploaded_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_user_id) REFERENCES users(id),
    FOREIGN KEY (center_id) REFERENCES diagnostic_centers(id),
    FOREIGN KEY (phlebotomist_id) REFERENCES users(id)
);

-- ==========================================
-- 42. DIAGNOSTIC BOOKING ITEMS
-- ==========================================
CREATE TABLE diagnostic_booking_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    test_id BIGINT NOT NULL,
    test_name VARCHAR(200) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    result_value TEXT,
    result_document_url VARCHAR(500),
    is_abnormal BOOLEAN DEFAULT FALSE,
    result_notes TEXT,
    completed_at TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES diagnostic_bookings(id),
    FOREIGN KEY (test_id) REFERENCES diagnostic_center_tests(id)
);

-- ==========================================
-- 43. INDEPENDENT NURSES
-- ==========================================
CREATE TABLE independent_nurses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    license_number VARCHAR(100) UNIQUE NOT NULL,
    license_document_url VARCHAR(500),
    id_proof_document_url VARCHAR(500),
    qualification VARCHAR(255) NOT NULL,
    experience_years INT DEFAULT 0,
    bio TEXT,
    profile_photo_url VARCHAR(500),
    rate_per_visit DECIMAL(10,2) NOT NULL,
    rate_per_hour DECIMAL(10,2),
    available_for_emergency BOOLEAN DEFAULT FALSE,
    current_latitude DECIMAL(10,8),
    current_longitude DECIMAL(11,8),
    last_location_update TIMESTAMP,
    approval_status ENUM('PENDING','APPROVED','REJECTED','SUSPENDED') DEFAULT 'PENDING',
    approved_by BIGINT,
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    background_check_status ENUM('PENDING','PASSED','FAILED') DEFAULT 'PENDING',
    rating DECIMAL(3,2) DEFAULT 0.00,
    total_reviews INT DEFAULT 0,
    total_services INT DEFAULT 0,
    is_available BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- ==========================================
-- 44. NURSE SERVICE TYPES (skills/services offered)
-- ==========================================
CREATE TABLE nurse_service_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE
);

-- Insert default service types
INSERT INTO nurse_service_types (name, description) VALUES
('POST_SURGERY_CARE', 'Post-operative care and wound management'),
('ELDERLY_CARE', 'Daily assistance and monitoring for elderly patients'),
('IV_DRIP_ADMINISTRATION', 'Intravenous drip setup and monitoring'),
('WOUND_DRESSING', 'Wound cleaning and dressing'),
('INJECTION_ADMINISTRATION', 'Administering prescribed injections'),
('GENERAL_MONITORING', 'Vital signs monitoring and health tracking'),
('PHYSIOTHERAPY_ASSIST', 'Assistance with physiotherapy exercises'),
('PALLIATIVE_CARE', 'End-of-life comfort care'),
('MATERNITY_CARE', 'Post-delivery mother and newborn care'),
('DIABETIC_CARE', 'Blood sugar monitoring and diabetic management');

-- ==========================================
-- 45. NURSE OFFERED SERVICES (mapping)
-- ==========================================
CREATE TABLE nurse_offered_services (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nurse_id BIGINT NOT NULL,
    service_type_id BIGINT NOT NULL,
    price_override DECIMAL(10,2),
    FOREIGN KEY (nurse_id) REFERENCES independent_nurses(id),
    FOREIGN KEY (service_type_id) REFERENCES nurse_service_types(id),
    UNIQUE KEY unique_nurse_service (nurse_id, service_type_id)
);

-- ==========================================
-- 46. NURSE SERVICE REGIONS
-- ==========================================
CREATE TABLE nurse_service_regions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nurse_id BIGINT NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10),
    approval_status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    approved_by BIGINT,
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (nurse_id) REFERENCES independent_nurses(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- ==========================================
-- 47. NURSE AVAILABILITY SCHEDULE
-- ==========================================
CREATE TABLE nurse_availability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nurse_id BIGINT NOT NULL,
    day_of_week ENUM('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available_for_emergency BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (nurse_id) REFERENCES independent_nurses(id),
    UNIQUE KEY unique_nurse_day (nurse_id, day_of_week)
);

-- ==========================================
-- 48. HOME NURSE BOOKINGS
-- ==========================================
CREATE TABLE home_nurse_bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_number VARCHAR(50) UNIQUE NOT NULL,
    patient_user_id BIGINT NOT NULL,
    nurse_id BIGINT NOT NULL,
    service_type_id BIGINT NOT NULL,
    booking_type ENUM('EMERGENCY','SCHEDULED') NOT NULL,
    booking_date DATE,
    booking_time TIME,
    service_address TEXT NOT NULL,
    service_latitude DECIMAL(10,8),
    service_longitude DECIMAL(11,8),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(10) NOT NULL,
    patient_condition TEXT,
    special_requirements TEXT,
    estimated_duration_hours DECIMAL(4,2) DEFAULT 1.00,
    visit_fee DECIMAL(10,2) NOT NULL,
    additional_charges DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING','ACCEPTED','REJECTED','EN_ROUTE','IN_SERVICE',
                'COMPLETED','CANCELLED','NO_SHOW') DEFAULT 'PENDING',
    payment_status ENUM('PENDING','PAID','REFUNDED') DEFAULT 'PENDING',
    payment_method ENUM('CASH','UPI','CARD','RAZORPAY'),
    razorpay_order_id VARCHAR(255),
    razorpay_payment_id VARCHAR(255),
    nurse_rejection_reason TEXT,
    cancellation_reason TEXT,
    cancelled_by BIGINT,
    nurse_arrived_at TIMESTAMP,
    service_started_at TIMESTAMP,
    service_completed_at TIMESTAMP,
    service_report TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_user_id) REFERENCES users(id),
    FOREIGN KEY (nurse_id) REFERENCES independent_nurses(id),
    FOREIGN KEY (service_type_id) REFERENCES nurse_service_types(id),
    FOREIGN KEY (cancelled_by) REFERENCES users(id)
);

-- ==========================================
-- 49. HOME NURSE REVIEWS
-- ==========================================
CREATE TABLE home_nurse_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT UNIQUE NOT NULL,
    nurse_id BIGINT NOT NULL,
    patient_user_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    is_anonymous BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES home_nurse_bookings(id),
    FOREIGN KEY (nurse_id) REFERENCES independent_nurses(id),
    FOREIGN KEY (patient_user_id) REFERENCES users(id)
);

-- ==========================================
-- 50. HOSPITAL AFFILIATION REQUESTS
-- (Medical shops, diagnostic centers, nurses requesting to affiliate)
-- ==========================================
CREATE TABLE hospital_affiliation_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_type ENUM('MEDICAL_SHOP','DIAGNOSTIC_CENTER','INDEPENDENT_NURSE') NOT NULL,
    requester_id BIGINT NOT NULL,
    branch_id BIGINT NOT NULL,
    request_message TEXT,
    status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    rejection_reason TEXT,
    affiliation_terms TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id),
    FOREIGN KEY (reviewed_by) REFERENCES users(id)
);

-- ==========================================
-- 51. ACTIVE AFFILIATIONS
-- ==========================================
CREATE TABLE hospital_affiliations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    affiliate_type ENUM('MEDICAL_SHOP','DIAGNOSTIC_CENTER','INDEPENDENT_NURSE') NOT NULL,
    affiliate_id BIGINT NOT NULL,
    affiliation_start_date DATE NOT NULL,
    affiliation_end_date DATE,
    commission_percentage DECIMAL(5,2) DEFAULT 0.00,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (branch_id) REFERENCES hospital_branches(id)
);

-- ==========================================
-- ROLES UPDATE — Add new roles to users table
-- ==========================================
ALTER TABLE users MODIFY COLUMN role ENUM(
    'ADMIN',
    'DOCTOR',
    'NURSE',
    'PATIENT',
    'BLOOD_BANK_MANAGER',
    'ORGAN_DONOR',
    'BLOOD_DONOR',
    'AMBULANCE_OPERATOR',
    'LAB_TECHNICIAN',
    'PHARMACIST',
    'RECEPTIONIST',
    'MEDICAL_SHOP_OWNER',
    'DIAGNOSTIC_CENTER_OWNER',
    'INDEPENDENT_NURSE',
    'PHLEBOTOMIST'
) NOT NULL;

-- ==========================================
-- INDEXES FOR NEW TABLES
-- ==========================================
CREATE INDEX idx_medical_shops_city ON medical_shops(city, state);
CREATE INDEX idx_medical_shops_approval ON medical_shops(approval_status);
CREATE INDEX idx_diagnostic_centers_city ON diagnostic_centers(city, state);
CREATE INDEX idx_independent_nurses_city ON nurse_service_regions(city, state);
CREATE INDEX idx_home_nurse_bookings_patient ON home_nurse_bookings(patient_user_id);
CREATE INDEX idx_home_nurse_bookings_nurse ON home_nurse_bookings(nurse_id);
CREATE INDEX idx_home_nurse_bookings_status ON home_nurse_bookings(status);
CREATE INDEX idx_medicine_orders_patient ON medicine_orders(patient_user_id);
CREATE INDEX idx_medicine_orders_shop ON medicine_orders(shop_id);
CREATE INDEX idx_diagnostic_bookings_patient ON diagnostic_bookings(patient_user_id);
CREATE INDEX idx_diagnostic_bookings_center ON diagnostic_bookings(center_id);
CREATE INDEX idx_affiliation_requests_status ON hospital_affiliation_requests(status);
