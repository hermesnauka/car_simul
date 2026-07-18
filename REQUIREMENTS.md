# 📋 Requirements Specification (Functional & Security)

## 1. Functional Requirements
*   **REQ-F-01:** The web application (React) shall connect to physical steering wheels and pedals via the browser's Gamepad API or WebHID API.
*   **REQ-F-02:** The UI shall simulate an interactive car dashboard, allowing the user to click or use hardware knobs for radio, AC, lights, and turn signals.
*   **REQ-F-03:** The platform shall include an "Exam Mode" that tracks mistakes (e.g., failing to use a turn signal, speeding) and calculates a final score.
*   **REQ-F-04:** The Spring Boot backend shall store user profiles, exam history, and telemetry data for review.
*   **REQ-F-05:** Real-time feedback must be provided visually in the 3D environment with < 50ms latency.

## 2. Non-Functional Requirements
*   **REQ-NF-01 (Performance):** The frontend simulator must run at a stable 60 FPS on standard modern hardware.
*   **REQ-NF-02 (Availability):** The backend API must have 99.9% uptime.
*   **REQ-NF-03 (Compatibility):** Must support major browsers (Chrome, Edge) that allow advanced hardware APIs.

## 3. Security Requirements (SSDLC Focused)
*   **REQ-SEC-01 (Authentication):** All users must authenticate via OAuth2/OIDC. Exam results can only be submitted by the authenticated session owner.
*   **REQ-SEC-02 (Data Integrity):** Telemetry and exam events sent to the server must be cryptographically signed or validated continuously server-side to prevent API spoofing.
*   **REQ-SEC-03 (Input Validation):** The backend must strictly validate all incoming data models (e.g., speed cannot instantly jump from 0 to 100).
*   **REQ-SEC-04 (Auditing):** All exam starts, completions, and state changes must be logged securely for non-repudiation.
