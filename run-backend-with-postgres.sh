#!/usr/bin/env bash
set -euo pipefail
IFS=$'\n\t'

PROJECT_DIR="${1:-/workspace/agentfactory}"
PROPERTIES_FILE="$PROJECT_DIR/src/main/resources/application.properties"

DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="backend_practice"
DB_USER="postgres"
DB_PASS="postgres"

PG_BIN="/usr/lib/postgresql/15/bin"
PGDATA="$PROJECT_DIR/.postgres-data"
PGLOG="$PGDATA/postgres.log"

function fail() {
  echo "ERROR: $*" >&2
  exit 1
}

function info() {
  echo "INFO: $*"
}

function has_cmd() {
  command -v "$1" >/dev/null 2>&1
}

function parse_properties() {
  if [[ ! -f "$PROPERTIES_FILE" ]]; then
    info "No application.properties found at $PROPERTIES_FILE, using defaults."
    return
  fi

  local raw_url
  raw_url=$(grep -E '^spring\.datasource\.url=' "$PROPERTIES_FILE" | head -n 1 | cut -d'=' -f2- || true)
  if [[ -n "$raw_url" && "$raw_url" =~ jdbc:postgresql://([^:/]+)(:([0-9]+))?/([^?]+) ]]; then
    DB_HOST="${BASH_REMATCH[1]}"
    DB_PORT="${BASH_REMATCH[3]:-$DB_PORT}"
    DB_NAME="${BASH_REMATCH[4]}"
  fi

  local parsed_user parsed_pass
  parsed_user=$(grep -E '^spring\.datasource\.username=' "$PROPERTIES_FILE" | head -n 1 | cut -d'=' -f2- || true)
  parsed_pass=$(grep -E '^spring\.datasource\.password=' "$PROPERTIES_FILE" | head -n 1 | cut -d'=' -f2- || true)

  if [[ -n "$parsed_user" ]]; then DB_USER="$parsed_user"; fi
  if [[ -n "$parsed_pass" ]]; then DB_PASS="$parsed_pass"; fi
}

function ensure_postgres_ready() {
  # If already listening, nothing to do.
  if "$PG_BIN/pg_isready" -h "$DB_HOST" -p "$DB_PORT" >/dev/null 2>&1; then
    info "PostgreSQL is already running and accepting connections."
  else
    # Initialize a user-space cluster if this is the first run.
    if [[ ! -d "$PGDATA" ]]; then
      info "Initializing new PostgreSQL cluster at $PGDATA ..."
      "$PG_BIN/initdb" -D "$PGDATA" -U postgres \
        --auth=trust --encoding=UTF8 --no-locale \
        >/dev/null
    fi

    info "Starting PostgreSQL cluster ..."
    "$PG_BIN/pg_ctl" -D "$PGDATA" -l "$PGLOG" start -o "-p $DB_PORT -k /tmp" -w >/dev/null

    if ! "$PG_BIN/pg_isready" -h "$DB_HOST" -p "$DB_PORT" >/dev/null 2>&1; then
      fail "PostgreSQL did not come up on $DB_HOST:$DB_PORT. Check $PGLOG for details."
    fi
    info "PostgreSQL is ready on $DB_HOST:$DB_PORT."
  fi

  # Create the application database if it doesn't exist.
  local exists
  exists=$("$PG_BIN/psql" -h "$DB_HOST" -p "$DB_PORT" -U postgres \
    -tAc "SELECT 1 FROM pg_database WHERE datname='${DB_NAME}'" 2>/dev/null || true)

  if [[ "$exists" == "1" ]]; then
    info "Database '$DB_NAME' already exists."
  else
    info "Creating database '$DB_NAME' ..."
    "$PG_BIN/createdb" -h "$DB_HOST" -p "$DB_PORT" -U postgres "$DB_NAME"
  fi
}

function run_backend() {
  if [[ ! -d "$PROJECT_DIR" ]]; then
    fail "Project directory '$PROJECT_DIR' does not exist."
  fi

  pushd "$PROJECT_DIR" >/dev/null
  info "Running ./mvnw spring-boot:run in $PROJECT_DIR"
  ./mvnw spring-boot:run
  popd >/dev/null
}

function main() {
  parse_properties
  ensure_postgres_ready
  run_backend
}

main "$@"
