#!/bin/bash
# ──────────────────────────────────────────────
# MediChain HMS — Database Setup Script
# Run this ONCE before starting the backend
# ──────────────────────────────────────────────

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'

echo -e "${YELLOW}MediChain HMS — Database Setup${NC}"
echo ""

# Read credentials
read -p "MySQL root username [root]: " MYSQL_USER
MYSQL_USER=${MYSQL_USER:-root}
read -s -p "MySQL root password: " MYSQL_PASS
echo ""
read -p "Database name [hospital_db]: " DB_NAME
DB_NAME=${DB_NAME:-hospital_db}
read -p "App DB username [medichain]: " APP_USER
APP_USER=${APP_USER:-medichain}
read -s -p "App DB password [Medichain@123]: " APP_PASS
APP_PASS=${APP_PASS:-Medichain@123}
echo ""

echo -e "\n${YELLOW}Creating database and user...${NC}"

mysql -u"$MYSQL_USER" -p"$MYSQL_PASS" << SQL
CREATE DATABASE IF NOT EXISTS \`$DB_NAME\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$APP_USER'@'localhost' IDENTIFIED BY '$APP_PASS';
GRANT ALL PRIVILEGES ON \`$DB_NAME\`.* TO '$APP_USER'@'localhost';
FLUSH PRIVILEGES;
SQL

if [ $? -eq 0 ]; then
  echo -e "${GREEN}✅ Database '$DB_NAME' ready${NC}"
  echo -e "${GREEN}✅ User '$APP_USER' created${NC}"
  echo ""
  echo -e "${YELLOW}Update application.properties with:${NC}"
  echo "  spring.datasource.username=$APP_USER"
  echo "  spring.datasource.password=$APP_PASS"
  echo "  (or set env vars DB_USERNAME and DB_PASSWORD)"
else
  echo -e "${RED}❌ Database setup failed. Check your MySQL root credentials.${NC}"
  exit 1
fi
