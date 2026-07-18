# Implementation Plan — app01 MVP (as built)

## Context

First working vertical slice of the Driving Simulator platform, built to
demonstrate the project's central security requirement: **the client can never
decide the exam outcome** (Abuser Story AS-1). Decisions made up front:
monorepo in `app01_java_react/` (`backend/` + `frontend/`), core vertical slice
only (no 3D, no physical hardware yet), simple JWT username/password auth
(full OAuth2/OIDC deferred — note this deviation from CLAUDE.md's
"OAuth2/JWT", which was accepted to keep the course setup friction-free).

## Milestones (all delivered)

| # | Scope | Proof |
|---|---|---|
| M0/M1 | Maven + Spring Boot skeleton, Flyway `V1`, `User`, BCrypt, `JwtService` (jjwt 0.12), `JwtAuthenticationFilter`, `SecurityConfig` (stateless, all `/api/**` protected except register/login), `AuthController` | `AuthControllerIT`, `JwtServiceTest` |
| M2 | Practice dashboard: `CarStateController`/`CarStateService` (in-memory per-user state — deliberately not persisted: nothing is graded there) | `AuthControllerIT` protected-endpoint test |
| M3 | Exam engine: `ExamSession`/`ExamEvent`/`Infraction` + Flyway `V2`, polymorphic event DTOs (`@JsonTypeInfo` on `type` + global `fail-on-unknown-properties`), `TurnSignalRule`, `SpeedingRule`, `TelemetryValidator`, `ExamEngineService`, `ExamController`, `AuditLogService` | 15 pure-unit rule tests + `ExamControllerIT` (10 tests incl. 5 tamper cases) |
| M4 | React frontend: `AuthContext` (JWT in sessionStorage), axios interceptors, Login / Dashboard / Exam / Results pages, SVG speedometer, 1 Hz telemetry stream, built-in "try to cheat" demo button | `npm run build` (strict TS) |
| M5 | E2E tamper script | `scripts/verify-anticheat.sh` — 10/10 against live server |

## Key design points

* **All exam rule state lives on the `exam_sessions` row** (signals, speeding
  episode, last client timestamp) + `@Version` optimistic locking — survives
  restarts, works with >1 instance, and is a deliberate contrast to the
  in-memory-map anti-pattern.
* **Rules are pure classes** (`TurnSignalRule`, `SpeedingRule`,
  `TelemetryValidator`) taking `(session, event, Instant now)` — unit-testable
  without Spring or mocks; the injected `Clock` keeps time controllable.
* **Server clock is authoritative.** `clientTimestamp` is only checked for
  monotonicity (cheap anti-replay) and stored for audit.
* **Rejected telemetry still costs the cheater**: `InvalidTelemetryException`
  is in `noRollbackFor`, so the 422 response coexists with a committed
  CRITICAL infraction.
* **Event contract has no result fields**; strict deserialization turns any
  smuggled `"status":"PASSED"` into a 400 (and `/finish` binds no body at all).

## Contract summary

```
POST /api/auth/register|login          -> {token, username, role}
GET  /api/car/state                    -> CarStateDto        (practice mode)
POST /api/car/controls/{control}       -> CarStateDto
POST /api/exam/start                   -> 201 ExamStateResponse
POST /api/exam/{id}/events             -> 200 ExamStateResponse
       body: {type: TURN_SIGNAL_ON|TURN_SIGNAL_OFF|TURN, direction, clientTimestamp}
           | {type: SPEED_UPDATE, speedKmh, clientTimestamp}
       errors: 400 malformed/extra fields | 403 not owner
             | 409 exam terminated | 422 implausible telemetry (+penalty)
GET  /api/exam/{id}/state              -> ExamStateResponse
POST /api/exam/{id}/finish  (no body)  -> ExamResultResponse (server verdict)
```

## Environment note (discovered during the build)

The dev VM's default JVM (`java-25-openjdk`) is JRE-only — no `javac` — which
makes Maven fail with a misleading "release version 17 not supported". Build
and run with `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64` (documented in
README prerequisites).
