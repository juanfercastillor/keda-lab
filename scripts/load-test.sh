#!/usr/bin/env bash
#
# Generates load against the KEDA lab by submitting many jobs, so the queue
# backlog (keda_lab_pending_jobs) grows and KEDA scales the Deployment out.
#
# Usage:
#   ./scripts/load-test.sh [BASE_URL] [COUNT]
#
# Examples:
#   ./scripts/load-test.sh                         # localhost:8080, 200 jobs
#   ./scripts/load-test.sh http://localhost:8080 500
#
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
COUNT="${2:-200}"

echo "Submitting ${COUNT} jobs to ${BASE_URL}/api/jobs ..."
for i in $(seq 1 "${COUNT}"); do
  curl -s -o /dev/null -X POST "${BASE_URL}/api/jobs" \
    -H 'Content-Type: application/json' \
    -d "{\"payload\":\"job-${i}\"}" &
  # Cap concurrency so we don't exhaust local sockets.
  if (( i % 25 == 0 )); then
    wait
  fi
done
wait

echo "Done. Current queue stats:"
curl -s "${BASE_URL}/api/jobs/stats"
echo
