# Driving Simulator — app01 (Java + React)

MVP vertical slice of the Driving Simulator Educational Platform: JWT-secured
Spring Boot backend with a **server-authoritative exam engine**, and a React 18
frontend (practice dashboard + exam mode + results). See
[IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the design and
[USER_STORIES.md](USER_STORIES.md) for the stories/abuse cases it implements.

## Layout

```
backend/    Spring Boot 3 (Java 17+), Maven, H2 (dev) / PostgreSQL (profile: postgres)
frontend/   Vite + React 18 + TypeScript
scripts/    verify-anticheat.sh — end-to-end tamper-resistance check
```

## Prerequisites

* A full **JDK 17+** (`javac` required, a JRE is not enough). If your default
  JVM is a JRE-only install, point `JAVA_HOME` at a JDK, e.g.
  `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`.
* Node 20+.

## Run (dev)

```bash
# Terminal 1 — backend on :8080 (H2 in-memory, schema via Flyway)
cd backend
./mvnw spring-boot:run
# port taken? add: -Dspring-boot.run.arguments=--server.port=8081

# Terminal 2 — frontend on :5173 (proxies /api to :8080)
cd frontend
npm install
npm run dev
```

Open http://localhost:5173, register a user, try the Practice Dashboard, then
take an exam. The 🏴‍☠️ button on the exam page demonstrates the server-side
telemetry plausibility check rejecting a cheat attempt.

## Build & test

```bash
cd backend && ./mvnw clean install    # compiles + runs all 35 unit/integration tests
cd frontend && npm run build          # strict TS typecheck + production bundle
```

## Verify the anti-cheat guarantees end-to-end

With the backend running:

```bash
./scripts/verify-anticheat.sh                       # default http://localhost:8080
./scripts/verify-anticheat.sh http://localhost:8081 # custom port
```

It proves (against a live server) that: unauthenticated requests are rejected;
smuggled result fields are rejected (400); implausible speed jumps are rejected
*and penalized* (422 + critical infraction); another user's token cannot touch
your session (403); a claimed "PASSED" in the finish body is ignored; and a
terminated exam session is locked (409).

## Security design in one paragraph

The frontend never computes or sends a score, infraction, or pass/fail — it
streams raw telemetry events (`TURN_SIGNAL_ON/OFF`, `TURN`, `SPEED_UPDATE`) and
renders whatever state the backend returns. The backend persists all rule state
on the exam session row, validates telemetry physically (max acceleration,
monotonic client timestamps), applies strict JSON deserialization (unknown
fields → 400), enforces session ownership, auto-fails after 3 critical
infractions, and writes an AUDIT log line for every start / infraction /
termination (REQ-SEC-01..04, Abuser Story AS-1).

## Exam rules (tunable in `backend/src/main/resources/application.yml`)

| Rule | Default |
|---|---|
| Turn requires matching signal within lookback | 10 s |
| Speeding counts after sustained | 3 s |
| Speeding severity threshold (overage) | >20 km/h ⇒ CRITICAL, else MINOR |
| Max plausible acceleration | 15 km/h/s |
| Critical / minor penalty | −20 / −5 points |
| Auto-fail after critical infractions | 3 |
| Pass threshold | score ≥ 70 |
| Speed limit (MVP route) | 50 km/h |

## Out of scope for this MVP (planned next)

3D cockpit (Three.js/R3F), physical wheel input (Gamepad/WebHID), OAuth2/OIDC
against a real IdP, telemetry event signing (REQ-SEC-02 full version), rate
limiting, exam history view.
