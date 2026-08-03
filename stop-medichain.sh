#!/bin/bash

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${RED}Stopping MediChain HMS...${NC}"

# Kill by PID files
if [ -f /tmp/medichain-backend.pid ]; then
  kill $(cat /tmp/medichain-backend.pid) 2>/dev/null && echo -e "${GREEN}✓ Backend stopped${NC}"
  rm /tmp/medichain-backend.pid
fi

if [ -f /tmp/medichain-frontend.pid ]; then
  kill $(cat /tmp/medichain-frontend.pid) 2>/dev/null && echo -e "${GREEN}✓ Frontend stopped${NC}"
  rm /tmp/medichain-frontend.pid
fi

# Also kill by port just in case
lsof -ti:8080 | xargs kill -9 2>/dev/null
lsof -ti:5173 | xargs kill -9 2>/dev/null

echo -e "${GREEN}All stopped!${NC}"
