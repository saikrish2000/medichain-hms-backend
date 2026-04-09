-- V4: Blood Bank Module

CREATE TABLE IF NOT EXISTS blood_banks (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id  BIGINT        NOT NULL,
    name       VARCHAR(100)  NOT NULL,
    address    VARCHAR(200),
    phone      VARCHAR(20),
    is_active  TINYINT(1)    DEFAULT 1,
    created_at DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bb_branch FOREIGN KEY (branch_id) REFERENCES hospital_branches(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS blood_inventory (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    blood_bank_id       BIGINT      NOT NULL,
    blood_group         VARCHAR(5)  NOT NULL,
    units_available     INT         DEFAULT 0,
    units_reserved      INT         DEFAULT 0,
    minimum_threshold   INT         DEFAULT 5,
    updated_at          DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_bank_group (blood_bank_id, blood_group),
    CONSTRAINT fk_inv_bank FOREIGN KEY (blood_bank_id) REFERENCES blood_banks(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS blood_requests (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_number    VARCHAR(30)   NOT NULL UNIQUE,
    blood_bank_id     BIGINT        NOT NULL,
    requested_by      BIGINT,
    blood_group       VARCHAR(5)    NOT NULL,
    units_requested   INT           NOT NULL,
    units_approved    INT,
    requester_type    VARCHAR(30)   DEFAULT 'INDIVIDUAL',
    status            VARCHAR(20)   DEFAULT 'PENDING',
    patient_name      VARCHAR(100),
    reason            TEXT,
    hospital_name     VARCHAR(200),
    contact_phone     VARCHAR(20),
    is_emergency      TINYINT(1)    DEFAULT 0,
    rejection_reason  VARCHAR(500),
    reviewed_by       BIGINT,
    reviewed_at       DATETIME,
    created_at        DATETIME      DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_req_bank     FOREIGN KEY (blood_bank_id) REFERENCES blood_banks(id),
    CONSTRAINT fk_req_user     FOREIGN KEY (requested_by) REFERENCES users(id),
    CONSTRAINT fk_req_reviewer FOREIGN KEY (reviewed_by)  REFERENCES users(id),
    INDEX idx_req_status (status),
    INDEX idx_req_bank   (blood_bank_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS blood_donations (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_id            BIGINT      NOT NULL,
    blood_bank_id       BIGINT      NOT NULL,
    blood_group         VARCHAR(5)  NOT NULL,
    units_donated       INT         DEFAULT 1,
    donation_date       DATE        NOT NULL,
    next_eligible_date  DATE,
    status              VARCHAR(20) DEFAULT 'PENDING',
    screened_by_user_id BIGINT,
    notes               TEXT,
    created_at          DATETIME    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_don_donor FOREIGN KEY (donor_id)      REFERENCES users(id),
    CONSTRAINT fk_don_bank  FOREIGN KEY (blood_bank_id) REFERENCES blood_banks(id),
    INDEX idx_don_donor (donor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed: one blood bank per branch (will auto-populate via admin setup)
