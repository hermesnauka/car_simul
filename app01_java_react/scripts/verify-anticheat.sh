#!/usr/bin/env bash
# End-to-end anti-cheat verification (Abuser Story AS-1 and REQ-SEC-01..04).
# Proves that a hostile client cannot fake a passing exam result.
#
# Usage: ./verify-anticheat.sh [BASE_URL]     (default: http://localhost:8080)
set -u

BASE_URL="${1:-http://localhost:8080}"
PASS_COUNT=0
FAIL_COUNT=0
RUN_ID="$RANDOM$RANDOM"

check() { # check <description> <expected> <actual>
  if [ "$2" = "$3" ]; then
    echo "  ✅ $1 (got $3)"
    PASS_COUNT=$((PASS_COUNT + 1))
  else
    echo "  ❌ $1 — expected $2, got $3"
    FAIL_COUNT=$((FAIL_COUNT + 1))
  fi
}

json_field() { # json_field <json> <field>
  echo "$1" | sed -n "s/.*\"$2\":\"\{0,1\}\([^\",}]*\)\"\{0,1\}.*/\1/p" | head -1
}

# Portable epoch-millis: %N is a GNU extension and width modifiers are not
# honored everywhere, so derive millis from whole seconds. Equal timestamps
# within one second are fine — the server check only rejects going backwards.
now_ms() { echo $(( $(date +%s) * 1000 )); }

echo "=== 1. Zero trust: protected endpoint without a token ==="
code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/car/state")
check "GET /api/car/state without JWT is rejected" 401 "$code"

echo "=== 2. Register + login ==="
USER="student$RUN_ID"
body=$(curl -s -X POST "$BASE_URL/api/auth/register" -H 'Content-Type: application/json' \
  -d "{\"username\":\"$USER\",\"password\":\"s3cretPass\"}")
TOKEN=$(json_field "$body" token)
[ -n "$TOKEN" ] && echo "  ✅ registered $USER, got JWT" || { echo "  ❌ registration failed: $body"; exit 1; }
AUTH="Authorization: Bearer $TOKEN"

echo "=== 3. Start exam ==="
body=$(curl -s -X POST "$BASE_URL/api/exam/start" -H "$AUTH")
SESSION=$(json_field "$body" sessionId)
check "exam starts IN_PROGRESS" "IN_PROGRESS" "$(json_field "$body" status)"

echo "=== 4. Honest infraction: TURN without signal ==="
body=$(curl -s -X POST "$BASE_URL/api/exam/$SESSION/events" -H "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"type\":\"TURN\",\"direction\":\"LEFT\",\"clientTimestamp\":$(now_ms)}")
check "score drops to 80" 80 "$(json_field "$body" currentScore)"
check "infraction is MISSING_TURN_SIGNAL" "MISSING_TURN_SIGNAL" "$(json_field "$body" ruleCode)"

echo "=== 5. TAMPER: smuggle a score field into an event ==="
code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/exam/$SESSION/events" -H "$AUTH" \
  -H 'Content-Type: application/json' \
  -d "{\"type\":\"TURN\",\"direction\":\"LEFT\",\"clientTimestamp\":$(now_ms),\"score\":100}")
check "extra field rejected with 400" 400 "$code"

echo "=== 6. TAMPER: implausible speed jump (10 -> 180 km/h) ==="
curl -s -o /dev/null -X POST "$BASE_URL/api/exam/$SESSION/events" -H "$AUTH" -H 'Content-Type: application/json' \
  -d "{\"type\":\"SPEED_UPDATE\",\"speedKmh\":10,\"clientTimestamp\":$(now_ms)}"
code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/exam/$SESSION/events" -H "$AUTH" \
  -H 'Content-Type: application/json' \
  -d "{\"type\":\"SPEED_UPDATE\",\"speedKmh\":180,\"clientTimestamp\":$(now_ms)}")
check "teleport rejected with 422" 422 "$code"

echo "=== 7. TAMPER: another user probes our session ==="
body=$(curl -s -X POST "$BASE_URL/api/auth/register" -H 'Content-Type: application/json' \
  -d "{\"username\":\"intruder$RUN_ID\",\"password\":\"s3cretPass\"}")
INTRUDER_TOKEN=$(json_field "$body" token)
code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/exam/$SESSION/events" \
  -H "Authorization: Bearer $INTRUDER_TOKEN" -H 'Content-Type: application/json' \
  -d "{\"type\":\"TURN\",\"direction\":\"LEFT\",\"clientTimestamp\":$(now_ms)}")
check "cross-user access rejected with 403" 403 "$code"

echo "=== 8. TAMPER: claim a PASS in the finish request body ==="
body=$(curl -s -X POST "$BASE_URL/api/exam/$SESSION/finish" -H "$AUTH" -H 'Content-Type: application/json' \
  -d '{"status":"PASSED","finalScore":100}')
status=$(json_field "$body" status)
score=$(json_field "$body" finalScore)
check "server ignores the claimed PASS (score 80 - 20 telemetry penalty = 60 < 70 threshold)" "FAILED" "$status"
check "final score is the server's computation" 60 "$score"

echo "=== 9. Terminated session is locked ==="
code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/api/exam/$SESSION/events" -H "$AUTH" \
  -H 'Content-Type: application/json' \
  -d "{\"type\":\"TURN_SIGNAL_ON\",\"direction\":\"LEFT\",\"clientTimestamp\":$(now_ms)}")
check "event after finish rejected with 409" 409 "$code"

echo
echo "RESULT: $PASS_COUNT passed, $FAIL_COUNT failed"
[ "$FAIL_COUNT" -eq 0 ]
