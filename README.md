# 🏥 MediChain HMS

**Full-stack Hospital Management System**  
Spring Boot 3 REST API + React + MySQL

---

## ⚡ Quick Start (2 options)

### Option A — Docker (recommended, zero setup)

```bash
# 1. Clone both repos side by side
git clone https://github.com/saikrish2000/medichain-hms-backend.git
git clone https://github.com/saikrish2000/medichain-hms-frontend.git

# 2. Go into backend folder
cd medichain-hms-backend

# 3. Start everything (MySQL + Backend + Frontend)
docker compose up --build
```

That's it! Open:
- 🌐 **App** → http://localhost:3000
- 🔧 **API** → http://localhost:8080
- 📖 **Swagger** → http://localhost:8080/swagger-ui.html

---

### Option B — Run locally (without Docker)

**Prerequisites:** Java 17, Maven, MySQL 8, Node 20

**Step 1 — Create the database**
```bash
mysql -u root -p
```
```sql
CREATE DATABASE hospital_db CHARACTER SET utf8mb4;
CREATE USER 'medichain'@'localhost' IDENTIFIED BY 'Medichain@123';
GRANT ALL ON hospital_db.* TO 'medichain'@'localhost';
FLUSH PRIVILEGES;
exit;
```

**Step 2 — Configure backend**

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=medichain
spring.datasource.password=Medichain@123
```
Or just use env vars (no file edit needed):
```bash
export DB_USERNAME=medichain
export DB_PASSWORD=Medichain@123
```

**Step 3 — Start backend**
```bash
cd medichain-hms-backend
mvn spring-boot:run
```
✅ Tables auto-created by Flyway on first run

**Step 4 — Start frontend**
```bash
cd medichain-hms-frontend
cp .env.example .env.local   # VITE_API_BASE_URL=http://localhost:8080
npm install
npm run dev
```

✅ Open http://localhost:5173

---

## 🔑 Default Credentials

| Role | Register at | Notes |
|------|------------|-------|
| Admin | `/auth/register` with role `ADMIN` | First admin auto-approved |
| Doctor | Register with role `DOCTOR` | Needs admin approval |
| Patient | Register with role `PATIENT` | Auto-approved |

---

## 🔌 Optional Integrations

All optional — app works without them (uses log output instead):

| Service | Env Vars | How to get |
|---------|----------|-----------|
| Gmail SMTP | `MAIL_USERNAME`, `MAIL_PASSWORD` | [App passwords](https://myaccount.google.com/apppasswords) |
| Razorpay | `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET` | [Dashboard](https://dashboard.razorpay.com/app/keys) |
| Twilio SMS | `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_PHONE_NUMBER` | [Console](https://console.twilio.com) |

Set via `.env` file or export in terminal before running.

---

## 📋 Features

| Module | Roles | Endpoints |
|--------|-------|-----------|
| Auth | All | `/api/auth/**` |
| Admin Dashboard | ADMIN | `/api/admin/**` |
| Doctor | DOCTOR | `/api/doctor/**` |
| Nurse | NURSE | `/api/nurse/**` |
| Patient | PATIENT | `/api/patient/**` |
| Billing + PDF | ADMIN, RECEPTIONIST | `/api/billing/**` |
| Razorpay Payments | Authenticated | `/api/payment/**` |
| Blood Bank | BLOOD_BANK_MANAGER | `/api/blood-bank/**` |
| Lab | LAB_TECHNICIAN | `/api/lab/**` |
| Pharmacy | PHARMACIST | `/api/pharmacy/**` |
| Ambulance + GPS | AMBULANCE_OPERATOR | `/api/ambulance/**` |
| Organ Donor | Authenticated | `/api/organ-donor/**` |
| Appointments | All | `/api/appointments/**` |

---

## 🛠️ Troubleshooting

| Problem | Fix |
|---------|-----|
| `Access denied for user` | Check `DB_USERNAME` and `DB_PASSWORD` |
| `Flyway migration failed` | Drop and recreate `hospital_db`, re-run |
| Port 8080 in use | Set `SERVER_PORT=8081` env var |
| `Email not sent` | Set `MAIL_USERNAME` + `MAIL_PASSWORD` (optional) |
| CORS error in browser | Backend not running or wrong `VITE_API_BASE_URL` |

---

## 🗂️ Tech Stack

- **Backend:** Spring Boot 3.2, Spring Security JWT, Flyway, JPA/Hibernate
- **Database:** MySQL 8
- **Frontend:** React 18, Vite, Tailwind CSS
- **Payments:** Razorpay
- **SMS:** Twilio
- **PDF:** iTextPDF
- **Real-time:** WebSocket (STOMP/SockJS)
- **API Docs:** Swagger UI (`/swagger-ui.html`)
