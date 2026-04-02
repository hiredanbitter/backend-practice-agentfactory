#!/usr/bin/env bash
# Runs AgentFactory on backlog tickets one at a time, in order.
# After each agent completes, runs the full test suite and halts on failure.
# Usage: ./run-backlog-sequentially.sh [STARTING_ISSUE]
# Example: ./run-backlog-sequentially.sh BAC-10   (resumes from BAC-10)
set -euo pipefail

ISSUES=(
  BAC-8
  BAC-9
  # BAC-10
  # BAC-11
  # BAC-12
  # BAC-13
  # BAC-14
  # BAC-15
  # BAC-16
  # BAC-17
  # BAC-18
  # BAC-19
  # BAC-20
  # BAC-21
  # BAC-22
  # BAC-23
  # BAC-24
)

START_FROM="${1:-}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PG_BIN="/usr/lib/postgresql/15/bin"
PGDATA="$SCRIPT_DIR/.postgres-data"

skipping=false
if [[ -n "$START_FROM" ]]; then
  skipping=true
fi

for issue in "${ISSUES[@]}"; do
  if $skipping; then
    if [[ "$issue" == "$START_FROM" ]]; then
      skipping=false
    else
      echo "SKIP: $issue (before $START_FROM)"
      continue
    fi
  fi

  echo ""
  echo "========================================"
  echo "Processing: $issue"
  echo "========================================"
  "$SCRIPT_DIR/rerun-agent.sh" "$issue"

  echo ""
  echo "--- Running tests for $issue ---"

  # Ensure postgres is up before running tests.
  if ! "$PG_BIN/pg_isready" -h localhost -p 5432 >/dev/null 2>&1; then
    echo "INFO: Starting PostgreSQL cluster for tests..."
    "$PG_BIN/pg_ctl" -D "$PGDATA" -l "$PGDATA/postgres.log" \
      start -o "-p 5432 -k /tmp" -w >/dev/null
  fi

  TEST_LOG=$(mktemp)
  if ./mvnw -f "$SCRIPT_DIR/pom.xml" test >"$TEST_LOG" 2>&1; then
    echo "PASS: $issue — all tests passed"
    rm -f "$TEST_LOG"
  else
    echo ""
    echo "FAIL: $issue — tests failed."
    echo "----------------------------------------"
    grep -E "(\[ERROR\]|Tests run:.*FAILURE|Tests run:.*ERROR|BUILD FAILURE)" "$TEST_LOG" \
      || cat "$TEST_LOG"
    echo "----------------------------------------"
    echo "Full output saved to: $TEST_LOG"
    exit 1
  fi
done

echo ""
echo "All issues processed and tests passed."
