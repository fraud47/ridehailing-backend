#!/bin/sh
set -eu

APP_DIR="${APP_DIR:-$HOME/apps/ride-hailing-backend}"

mkdir -p "$APP_DIR"
cd "$APP_DIR"

if [ ! -f .env ]; then
  echo ".env file is missing in $APP_DIR" >&2
  exit 1
fi

docker compose -f docker-compose.prod.yml build --pull app
docker compose -f docker-compose.prod.yml up -d
docker image prune -f
