-- ================================================================
-- V7 — Payment Gateway, GPS Tracking, Organ Donor Registry
-- ================================================================

-- ── 1. INVOICES — add razorpay columns ──────────────────────────
ALTER TABLE invoices
    ADD COLUMN IF NOT EXISTS razorpay_order_id  VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS paid_at            DATETIME     NULL,
    ADD COLUMN IF NOT EXISTS notes              TEXT         NULL;

-- Index for fast lookup by transaction_id (razorpay order/payment id)
CREATE INDEX IF NOT EXISTS idx_invoices_transaction_id ON invoices(transaction_id);

-- ── 2. AMBULANCES — GPS columns ──────────────────────────────────
ALTER TABLE ambulances
    ADD COLUMN IF NOT EXISTS last_latitude      DECIMAL(10,7) NULL,
    ADD COLUMN IF NOT EXISTS last_longitude     DECIMAL(10,7) NULL,
    ADD COLUMN IF NOT EXISTS last_gps_update    DATETIME      NULL,
    ADD COLUMN IF NOT EXISTS current_speed      DECIMAL(5,2)  NULL,
    ADD COLUMN IF NOT EXISTS heading            VARCHAR(10)   NULL;

-- ── 3. ORGAN_DONORS — extend for full registry ───────────────────
ALTER TABLE organ_donors
    ADD COLUMN IF NOT EXISTS organs_for_donation     TEXT    NULL,
    ADD COLUMN IF NOT EXISTS consent_given           BOOLEAN DEFAULT FALSE NOT NULL,
    ADD COLUMN IF NOT EXISTS registration_date       DATETIME NULL,
    ADD COLUMN IF NOT EXISTS emergency_contact_name  VARCHAR(100) NULL,
    ADD COLUMN IF NOT EXISTS emergency_contact_phone VARCHAR(20)  NULL,
    ADD COLUMN IF NOT EXISTS medical_notes           TEXT    NULL;

-- ── 4. ORGAN_REQUESTS — extend for full request flow ─────────────
ALTER TABLE organ_requests
    ADD COLUMN IF NOT EXISTS patient_name       VARCHAR(200) NULL,
    ADD COLUMN IF NOT EXISTS blood_group        VARCHAR(10)  NULL,
    ADD COLUMN IF NOT EXISTS hospital_name      VARCHAR(200) NULL,
    ADD COLUMN IF NOT EXISTS contact_phone      VARCHAR(20)  NULL,
    ADD COLUMN IF NOT EXISTS notes              TEXT         NULL,
    ADD COLUMN IF NOT EXISTS requested_at       DATETIME     NULL,
    ADD COLUMN IF NOT EXISTS fulfilled_at       DATETIME     NULL;

-- ── 5. APPOINTMENTS — add payment_status for Razorpay ────────────
ALTER TABLE appointments
    ADD COLUMN IF NOT EXISTS payment_status     VARCHAR(20) DEFAULT 'UNPAID' NULL,
    ADD COLUMN IF NOT EXISTS razorpay_order_id  VARCHAR(100) NULL;

-- ── 6. USERS — add preferred_language if missing ─────────────────
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS preferred_language VARCHAR(10) DEFAULT 'en' NULL;
