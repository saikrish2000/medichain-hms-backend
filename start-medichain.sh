#!/bin/bash

# ─────────────────────────────────────────
#  MediChain HMS — Start Script
#  Starts both backend and frontend together
# ─────────────────────────────────────────

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}"
echo "  __  __          _ _ ____ _           _       "
echo " |  \/  | ___  __| (_) ___| |__   __ _(_)_ __  "
echo " | |\/| |/ _ \/ _\` | | |  | '_ \ / _\` | | '_ \ "
echo " | |  | |  __/ (_| | | |__| | | | (_| | | | | |"
echo " |_|  |_|\___|\__,_|_|\____|_| |_|\__,_|_|_| |_|"
echo -e "${NC}"
echo -e "${YELLOW}  Hospital Management System — Local Dev Starter${NC}"
echo ""

# ── Check prerequisites ──
check_cmd() {
  if ! command -v $1 &> /dev/null; then
    echo -e "${RED}✗ $1 not found. Please install it first.${NC}"
    exit 1
  else
    echo -e "${GREEN}✓ $1 found${NC}"
  fi
}

echo "Checking prerequisites..."
check_cmd java
check_cmd mvn
check_cmd node
check_cmd npm
echo ""

# ── Paths ──
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/medichain-hms-backend"
FRONTEND_DIR="$SCRIPT_DIR/medichain-hms-frontend"

if [ ! -d "$BACKEND_DIR" ]; then
  echo -e "${RED}✗ Backend folder not found at: $BACKEND_DIR${NC}"
  echo "  Make sure both repos are cloned in the same parent folder as this script."
  exit 1
fi

if [ ! -d "$FRONTEND_DIR" ]; then
  echo -e "${RED}✗ Frontend folder not found at: $FRONTEND_DIR${NC}"
  echo "  Make sure both repos are cloned in the same parent folder as this script."
  exit 1
fi

# ── Install frontend deps if needed ──
if [ ! -d "$FRONTEND_DIR/node_modules" ]; then
  echo -e "${YELLOW}Installing frontend dependencies...${NC}"
  cd "$FRONTEND_DIR" && npm install
  echo ""
fi

# ── Create .env.local if missing ──
if [ ! -f "$FRONTEND_DIR/.env.local" ]; then
  echo "VITE_API_BASE_URL=http://localhost:8080" > "$FRONTEND_DIR/.env.local"
  echo -e "${GREEN}✓ Created frontend .env.local${NC}"
fi

# ── Start backend in background ──
echo -e "${YELLOW}Starting backend (Spring Boot)...${NC}"
cd "$BACKEND_DIR"
mvn spring-boot:run > /tmp/medichain-backend.log 2>&1 &
BACKEND_PID=$!
echo -e "${GREEN}✓ Backend starting (PID: $BACKEND_PID)${NC}"
echo "  Logs: tail -f /tmp/medichain-backend.log"

# ── Wait for backend to be ready ──
echo -e "${YELLOW}Waiting for backend on port 8080...${NC}"
for i in {1..30}; do
  if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1 || \
     curl -s http://localhost:8080/v3/api-docs > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Backend is up!${NC}"
    break
  fi
  printf "."
  sleep 3
done
echo ""

# ── Start frontend ──
echo -e "${YELLOW}Starting frontend (React + Vite)...${NC}"
cd "$FRONTEND_DIR"
npm run dev > /tmp/medichain-frontend.log 2>&1 &
FRONTEND_PID=$!
echo -e "${GREEN}✓ Frontend starting (PID: $FRONTEND_PID)${NC}"
echo "  Logs: tail -f /tmp/medichain-frontend.log"
echo ""

sleep 3

echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}  ✅ MediChain HMS is running!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "  🌐 Frontend  →  ${YELLOW}http://localhost:5173${NC}"
echo -e "  🔧 Backend   →  ${YELLOW}http://localhost:8080${NC}"
echo -e "  📖 Swagger   →  ${YELLOW}http://localhost:8080/swagger-ui.html${NC}"
echo ""
echo -e "  Backend PID:  $BACKEND_PID"
echo -e "  Frontend PID: $FRONTEND_PID"
echo ""
echo -e "${YELLOW}  Press Ctrl+C to stop both servers${NC}"
echo ""

# ── Save PIDs for stop script ──
echo "$BACKEND_PID" > /tmp/medichain-backend.pid
echo "$FRONTEND_PID" > /tmp/medichain-frontend.pid

# ── Wait and handle Ctrl+C ──
trap "echo ''; echo -e '${RED}Stopping both servers...${NC}'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; echo -e '${GREEN}Done!${NC}'; exit 0" SIGINT SIGTERM

wait
