# MediChain HMS — Backend API

Full-scale Hospital Chain Management System built with Spring Boot 3 REST API + JWT Authentication.

## Tech Stack
- Spring Boot 3.2.3
- Spring Security 6 (JWT)
- MySQL 8 + Flyway Migrations
- SpringDoc OpenAPI 2.3.0 (Swagger UI)
- Lombok, ModelMapper

## Quick Start

### Prerequisites: Java 17+, MySQL 8+, Maven 3.8+

```bash
git clone https://github.com/saikrish2000/medichain-hms-backend.git
cd medichain-hms-backend
```

Create DB:
```sql
CREATE DATABASE hospital_db;
CREATE USER 'hospital_user'@'localhost' IDENTIFIED BY 'yourpassword';
GRANT ALL PRIVILEGES ON hospital_db.* TO 'hospital_user'@'localhost';
```

Update `src/main/resources/application.properties`:
```
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_db
spring.datasource.username=hospital_user
spring.datasource.password=yourpassword
app.jwt.secret=your-256-bit-secret
```

Run:
```bash
mvn spring-boot:run
```

## Swagger UI
- http://localhost:8080/swagger-ui.html
- http://localhost:8080/v3/api-docs

**Auth in Swagger:** Call `POST /api/auth/login` → copy `token` → click Authorize → enter `Bearer <token>`

## Roles & Endpoints
| Role | Path |
|------|------|
| Admin | /api/admin/** |
| Doctor | /api/doctor/** |
| Nurse | /api/nurse/** |
| Patient | /api/patient/** |
| Receptionist | /api/receptionist/** |
| Pharmacist | /api/pharmacy/** |
| Lab Technician | /api/lab/** |
| Blood Bank | /api/blood-bank/** |
| Ambulance | /api/ambulance/** |
| Public | /api/auth/** |

## DB Migrations (Flyway)
V1 → V6 covering 54+ tables across all modules.

## Docker
```bash
docker-compose up -d
```

## Branches
`main` (prod) · `develop` (active dev) · `feature/*` · `hotfix/*`
