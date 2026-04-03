# Project Structure

```
hospital-management-system/
│
├── frontend/                          # React + Vite + Tailwind CSS
│   ├── src/
│   │   ├── api/                       # Axios client + API modules
│   │   ├── components/                # Reusable UI components
│   │   │   ├── layout/                # Header, Sidebar
│   │   │   └── ui/                    # Button, Card, Modal, Table etc.
│   │   ├── layouts/                   # AppLayout wrapper
│   │   ├── pages/                     # Role-based page components
│   │   │   ├── admin/                 # Admin dashboard, users, depts, reports
│   │   │   ├── doctor/                # Doctor appointments, patients, prescriptions
│   │   │   ├── nurse/                 # Nurse station, eMAR, handover, vitals
│   │   │   ├── patient/               # Patient portal, booking, records
│   │   │   ├── pharmacy/              # Pharmacy dashboard, medicines
│   │   │   ├── lab/                   # Lab orders, tests
│   │   │   ├── blood-bank/            # Blood inventory, donations
│   │   │   ├── ambulance/             # Dispatch, fleet management
│   │   │   ├── billing/               # Invoices, my bills
│   │   │   └── auth/                  # Login, Register
│   │   ├── store/                     # Zustand auth store
│   │   └── utils/                     # Helper functions
│   └── package.json
│
├── src/main/java/com/hospital/
│   ├── config/                        # SecurityConfig, WebConfig, ModelMapperConfig
│   ├── controller/                    # REST controllers (@RestController)
│   │   ├── AuthController             # POST /api/auth/login, /register
│   │   ├── AdminController            # GET/POST /api/admin/**
│   │   ├── DoctorController           # /api/doctor/**
│   │   ├── NurseController            # /api/nurse/**
│   │   ├── PatientController          # /api/patient/**
│   │   ├── AppointmentController      # /api/appointments/**
│   │   ├── BillingController          # /api/billing/**
│   │   ├── BloodBankController        # /api/blood-bank/**
│   │   ├── LabController              # /api/lab/**
│   │   ├── PharmacyController         # /api/pharmacy/**
│   │   ├── AmbulanceController        # /api/ambulance/**
│   │   └── ReceptionistController     # /api/receptionist/**
│   ├── dto/                           # DTOs (LoginRequest, RegisterRequest, AuthResponse)
│   ├── entity/                        # JPA Entities (60+ tables)
│   ├── enums/                         # Role, BloodGroup
│   ├── exception/                     # GlobalExceptionHandler (REST JSON errors)
│   ├── repository/                    # Spring Data JPA repositories
│   ├── security/                      # JWT filter, UserPrincipal, JwtTokenProvider
│   └── service/                       # Business logic services
│
├── src/main/resources/
│   ├── application.properties         # All config (env-var driven)
│   └── db/migration/                  # Flyway SQL migrations V1–V6
│       ├── V1__init_schema.sql        # Core tables (branches, users, doctors, patients)
│       ├── V2__new_modules_schema.sql # External vendors (shop, diagnostics, nurses)
│       ├── V3__slots_appointments.sql # Doctor slots + appointments
│       ├── V4__blood_bank.sql         # Blood bank module
│       ├── V5__schema_fixes.sql       # Fixes + seed data
│       └── V6__complete_alignment.sql # Full entity↔DB sync + 60+ tables
│
├── nginx/nginx.conf                   # Nginx reverse proxy config
├── Dockerfile                         # Multi-stage Docker build
├── docker-compose.yml                 # Full stack (backend + MySQL + nginx)
└── .github/workflows/ci-cd.yml        # GitHub Actions CI/CD
```

## Key Design Decisions

- **No Thymeleaf** — Pure REST API backend, React SPA frontend
- **String-based status fields** — Avoids enum migration issues in MySQL
- **Flyway migrations** — Every schema change versioned, reproducible
- **JWT stateless auth** — No sessions, mobile-friendly
- **Role-based access** — Spring Security method + URL level
- **Responsive UI** — Tailwind CSS mobile-first, works on all devices
