# MediChain HMS — GitHub & Deployment Guide

---

## 1. Architecture Overview

This project is a **Monolithic Spring Boot application** with Thymeleaf server-side rendering.

```
medichain-hms/
├── backend + frontend (Thymeleaf SSR)  ← single Spring Boot app
├── database                             ← MySQL 8.0
└── reverse proxy                        ← Nginx (optional, for production)
```

> **Important:** There is no separate React/Vue frontend. The HTML templates live
> inside the Spring Boot app under `src/main/resources/templates/`. They are
> rendered on the server and sent as plain HTML to the browser.
> This means you deploy ONE artifact — the Spring Boot JAR — and it serves both
> the API and the UI.

---

## 2. Repository Structure (GitHub)

```
medichain-hms/                         ← repository root
├── .github/
│   └── workflows/
│       └── ci-cd.yml                  ← GitHub Actions pipeline
├── src/
│   └── main/
│       ├── java/com/hospital/         ← All Java (backend logic)
│       │   ├── config/
│       │   ├── controller/            ← HTTP endpoints (render HTML pages)
│       │   ├── entity/                ← JPA database models
│       │   ├── repository/            ← Spring Data JPA queries
│       │   ├── service/               ← Business logic
│       │   ├── security/              ← JWT + Spring Security
│       │   ├── dto/                   ← Data Transfer Objects
│       │   └── exception/             ← Error handling
│       └── resources/
│           ├── templates/             ← Thymeleaf HTML (the "frontend")
│           │   ├── layout/base.html   ← Master layout
│           │   ├── admin/             ← Admin dashboard pages
│           │   ├── doctor/            ← Doctor portal pages
│           │   ├── nurse/             ← Nurse station pages
│           │   ├── patient/           ← Patient portal pages
│           │   ├── auth/              ← Login/register pages
│           │   └── blood-bank/        ← Blood bank pages
│           ├── static/
│           │   └── css/
│           │       └── claymorphism.css  ← Custom UI styles
│           ├── db/migration/          ← Flyway SQL migrations (V1–V5)
│           ├── messages/              ← i18n strings (EN, HI, TE)
│           └── application.properties ← Config (uses env vars)
├── nginx/
│   └── nginx.conf                     ← Reverse proxy config
├── Dockerfile                         ← Container build
├── docker-compose.yml                 ← Local dev stack
├── pom.xml                            ← Maven dependencies
└── .gitignore                         ← Never commit secrets/build output
```

---

## 3. Push to GitHub

### Step 1 — Create repository
1. Go to https://github.com/new
2. Name it: `medichain-hms`
3. Visibility: **Private** (hospital data — always private)
4. Do NOT initialize with README (we have our own)
5. Click **Create repository**

### Step 2 — Initialize git locally
Open terminal in the `hospital-management-system/` folder:

```bash
cd hospital-management-system

# Initialize
git init
git branch -M main

# Add remote (replace YOUR_USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/medichain-hms.git

# Stage everything
git add .

# First commit
git commit -m "feat: initial commit — MediChain HMS full system"

# Push
git push -u origin main
```

### Step 3 — Set up GitHub Secrets
Go to: **Settings → Secrets and variables → Actions → New repository secret**

| Secret Name         | Value                              |
|---------------------|------------------------------------|
| `DOCKERHUB_USERNAME`| Your Docker Hub username           |
| `DOCKERHUB_TOKEN`   | Docker Hub access token            |
| `SERVER_HOST`       | Your server IP / hostname          |
| `SERVER_USER`       | SSH username (e.g. `ubuntu`)       |
| `SERVER_SSH_KEY`    | Private SSH key (paste full key)   |

---

## 4. Environment Variables Reference

The app reads ALL secrets from environment variables. **Never hardcode them.**

| Variable              | Description                          | Example                           |
|-----------------------|--------------------------------------|-----------------------------------|
| `DB_URL`              | Full JDBC URL                        | `jdbc:mysql://db:3306/hospital_db...` |
| `DB_USERNAME`         | DB username                          | `hospitaluser`                    |
| `DB_PASSWORD`         | DB password                          | `strongpassword`                  |
| `JWT_SECRET`          | 256-bit JWT signing key              | 64+ char random string            |
| `MAIL_USERNAME`       | Gmail address                        | `alerts@yourhospital.com`         |
| `MAIL_PASSWORD`       | Gmail App Password (not normal pwd)  | 16-char app password              |
| `TWILIO_ACCOUNT_SID`  | Twilio account SID                   | `ACxxxx...`                       |
| `TWILIO_AUTH_TOKEN`   | Twilio auth token                    | `xxxx...`                         |
| `TWILIO_PHONE`        | Twilio phone number                  | `+1XXXXXXXXXX`                    |
| `RAZORPAY_KEY_ID`     | Razorpay key ID                      | `rzp_live_xxxxx`                  |
| `RAZORPAY_KEY_SECRET` | Razorpay key secret                  | `xxxxxxxxxxxx`                    |
| `LOG_LEVEL`           | Logging verbosity                    | `INFO` (prod) / `DEBUG` (dev)     |
| `THYMELEAF_CACHE`     | Cache templates in prod              | `true`                            |

---

## 5. Deployment Options

### Option A — Render.com (Easiest, Free Tier Available)

1. Go to https://render.com → New → **Web Service**
2. Connect your GitHub repository
3. Settings:
   - **Runtime:** Docker
   - **Dockerfile path:** `Dockerfile`
   - **Health check path:** `/actuator/health`
4. Add all environment variables from the table above
5. Add a **PostgreSQL** or **MySQL** database from Render dashboard
6. Click Deploy — Render builds and deploys automatically on every push to `main`

**Free tier limits:** 512MB RAM, spins down after 15min inactivity. Fine for demo/testing.

---

### Option B — Railway.app (Recommended for Production-like free hosting)

1. Go to https://railway.app → New Project
2. Deploy from GitHub repo
3. Add a **MySQL** plugin to the project
4. Railway auto-injects `DATABASE_URL` — set your env vars
5. Builds via Dockerfile automatically

**Advantages:** Always-on, MySQL included, good free tier ($5/month credit).

---

### Option C — AWS EC2 + Docker Compose (Full Production)

```bash
# On your EC2 instance (Ubuntu 22.04)

# 1. Install Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker ubuntu

# 2. Clone your repo
git clone https://github.com/YOUR_USERNAME/medichain-hms.git /opt/medichain
cd /opt/medichain

# 3. Create environment file
cp .env.example .env
nano .env   # fill in your values

# 4. Start the full stack
docker-compose --profile production up -d

# 5. Check logs
docker-compose logs -f app
```

**Recommended EC2 instance:** t3.small (2GB RAM) minimum — t3.medium (4GB) for production load.

---

### Option D — Self-hosted VPS (DigitalOcean / Hetzner)

Same as EC2 approach above. Hetzner CX21 (€4.15/month, 4GB RAM) is excellent value.

---

## 6. MySQL Setup (local or server)

```sql
-- Run these commands in MySQL as root
CREATE DATABASE hospital_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'hospitaluser'@'%' IDENTIFIED BY 'YourStrongPassword123!';
GRANT ALL PRIVILEGES ON hospital_db.* TO 'hospitaluser'@'%';
FLUSH PRIVILEGES;
```

Flyway will run all migrations (V1 through V5) automatically on first startup.

---

## 7. Local Development Setup

```bash
# Prerequisites: Java 17+, Maven 3.9+, MySQL 8.0+

# 1. Clone
git clone https://github.com/YOUR_USERNAME/medichain-hms.git
cd medichain-hms

# 2. Create local MySQL database (see step 6 above)

# 3. Set environment variables (or copy and edit)
export DB_URL="jdbc:mysql://localhost:3306/hospital_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true"
export DB_USERNAME="hospitaluser"
export DB_PASSWORD="YourStrongPassword123!"
export JWT_SECRET="dev-secret-key-at-least-256-bits-long-for-jwt-signing"

# 4. Run
mvn spring-boot:run

# App starts at: http://localhost:8080
```

**Or use Docker (zero setup):**
```bash
docker-compose up -d
# App at http://localhost:8080
# phpMyAdmin at http://localhost:8081 (optional — add to docker-compose)
```

---

## 8. Branching Strategy

```
main          ← production-ready, protected branch
  └── develop ← integration branch (merge PRs here first)
        └── feature/blood-bank-module
        └── feature/billing-razorpay
        └── fix/appointment-slot-bug
```

**Rules:**
- Never push directly to `main`
- All features go through PRs into `develop`
- Only merge `develop → main` when release-ready
- GitHub Actions runs tests on every PR automatically

---

## 9. First Default Admin Account

After first startup, create the admin user via this SQL (run once):

```sql
-- Password is: Admin@123 (BCrypt encoded)
INSERT INTO users (username, email, password, role, first_name, last_name, is_active, is_verified)
VALUES (
  'admin',
  'admin@medichain.in',
  '$2a$12$LQv3c1yqBwEHFPXlKZ.MtOGO7FiBJL2Pp.u/PzxHOcV4XCv6./yOe',
  'ADMIN',
  'System',
  'Admin',
  TRUE,
  TRUE
);
```

Login at: `http://localhost:8080/login`

---

## 10. Monitoring & Health

| Endpoint               | Description              |
|------------------------|--------------------------|
| `/actuator/health`     | App + DB health status   |
| `/actuator/info`       | Build info               |

---

## Checklist Before Going Live

- [ ] Change `JWT_SECRET` to a strong random 64+ char string
- [ ] Set `THYMELEAF_CACHE=true` in production
- [ ] Enable HTTPS (SSL certificate via Let's Encrypt)
- [ ] Set up MySQL backups (automated daily)
- [ ] Configure Gmail App Password (not your main password)
- [ ] Set `LOG_LEVEL=WARN` in production
- [ ] Set up Razorpay live keys (replace test keys)
- [ ] Review and tighten SecurityConfig CORS origins
- [ ] Create first admin user via SQL above
