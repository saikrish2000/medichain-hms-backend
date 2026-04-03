# 🏥 MediChain HMS — Hospital Management System

A full-scale, production-grade Hospital Management System built with a modern decoupled architecture.

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   CLIENT LAYER                          │
│   React + Vite + Tailwind CSS (frontend/)               │
│   Responsive — works on all devices (mobile/tablet/PC)  │
└──────────────────────┬──────────────────────────────────┘
                       │ REST API (JSON)
                       ↓
┌─────────────────────────────────────────────────────────┐
│                  BACKEND LAYER                          │
│   Spring Boot 3.2.3 — Pure REST API (@RestController)  │
│   Spring Security + JWT Authentication                  │
│   Flyway DB Migrations (V1 → V6)                       │
└──────────────────────┬──────────────────────────────────┘
                       │ JPA / Hibernate
                       ↓
┌─────────────────────────────────────────────────────────┐
│                  DATABASE LAYER                         │
│   MySQL 8.x — 60+ tables, fully normalised             │
└─────────────────────────────────────────────────────────┘
```

> **No Thymeleaf. No server-side rendering. Pure REST + React.**

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Node.js 20+
- MySQL 8.x
- Maven 3.8+

### Backend
```bash
# Set environment variables (or edit application.properties)
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=medichain_hms
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
export JWT_SECRET=your-super-secret-key

cd hospital-management-system
./mvnw spring-boot:run
```

Backend runs on http://localhost:8080

### Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend runs on http://localhost:5173

## 🧑‍💼 Roles & Access

| Role | Access |
|------|--------|
| ADMIN | Full system access |
| DOCTOR | Patient records, appointments, prescriptions, lab orders |
| NURSE | Patient care, vitals, eMAR, handovers |
| PATIENT | Self-service booking, records, bills |
| PHARMACIST | Prescriptions, medicine inventory |
| LAB_TECHNICIAN | Lab orders, test results |
| BLOOD_BANK_MANAGER | Inventory, donations, requests |
| AMBULANCE_OPERATOR | Dispatch, GPS tracking |
| RECEPTIONIST | Appointments, check-in |

## 📦 Modules

1. **Authentication** — JWT, role-based, refresh tokens
2. **Admin Dashboard** — Users, doctors, departments, reports, audit logs
3. **Doctor Portal** — Appointments, prescriptions, lab orders, slots
4. **Nurse Station** — Patient care, vitals, eMAR, shift handover
5. **Patient Portal** — Booking, medical records, bills, vitals
6. **Blood Bank** — Inventory, donations, requests
7. **Pharmacy** — Medicines, prescriptions dispensing
8. **Laboratory** — Orders, test results, reports
9. **Ambulance** — Real-time dispatch, GPS WebSocket
10. **Billing** — Invoices, Razorpay integration
11. **Organ Donor Registry**
12. **Multi-branch Support**

## 🗄️ Database Migrations

| Version | Description |
|---------|-------------|
| V1 | Core schema — branches, users, doctors, patients, appointments |
| V2 | External modules — medical shops, diagnostics, independent nurses |
| V3 | Slots, appointments, medical records |
| V4 | Blood bank module |
| V5 | Schema fixes + seed data |
| V6 | Complete alignment — 60+ tables, eMAR, handover, lab junction |

## 🐳 Docker

```bash
docker-compose up -d
```

## 🔗 API Base URL

All endpoints: `/api/*`
Health check: `GET /api/health`
Auth: `POST /api/auth/login`, `POST /api/auth/register`

## 📱 Frontend Routes

All pages are fully responsive (mobile-first Tailwind CSS):
- `/login`, `/register`
- `/admin/dashboard`, `/admin/users`, `/admin/departments`, etc.
- `/doctor/dashboard`, `/doctor/appointments`, etc.
- `/nurse/dashboard`, `/nurse/patients`, `/nurse/emar`, etc.
- `/patient/dashboard`, `/patient/appointments`, etc.
- `/pharmacy/dashboard`, `/lab/dashboard`, `/blood-bank/dashboard`, etc.
