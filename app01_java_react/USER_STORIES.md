# 📖 Epic & User Stories (with Security Criteria)

## Epic 1: Hardware & Dashboard Integration
**Description:** Enable users to control the virtual vehicle using physical hardware and on-screen dashboard buttons.

*   **US-1.1:** As a student, I want to connect my physical steering wheel so that I can steer the virtual car.
    *   *Acceptance Criteria:* Steering input maps linearly to the virtual wheels.
    *   *Security/Abuse Criteria:* System gracefully handles hardware disconnection without crashing or exposing debug data.
*   **US-1.2:** As a student, I want to click virtual buttons on the dashboard (e.g., hazard lights, AC) so I can learn car internal controls.
    *   *Acceptance Criteria:* Clicking buttons updates the car's state and plays an audio cue.

## Epic 2: Driving Exam Module
**Description:** A strict, evaluated mode where driving rules are enforced and graded.

*   **US-2.1:** As an examiner system, I want to record driving infractions (e.g., missing turn signals) so that I can fail the student if they exceed the mistake limit.
    *   *Acceptance Criteria:* Exam terminates automatically if critical mistakes are made.
    *   *Security/Abuse Criteria:* Exam state (score, mistakes) must be managed on the backend. Frontend only displays the state. A malicious user modifying local React state must not be able to pass the exam.

## 🛡️ Abuser Stories (Threat Modeling Output)
*   **AS-1:** As a malicious user, I want to intercept the API call submitting my final exam score and change "FAIL" to "PASS".
    *   *Mitigation:* The frontend does not send "PASS/FAIL". The frontend sends a stream of encrypted/validated events. The Spring Boot backend calculates the final score.
*   **AS-2:** As a cheater, I want to inject a script that automatically triggers the "turn signal" event right before I turn the wheel.
    *   *Mitigation:* Implement CAPTCHA or behavioral analysis on inputs if anomalies (perfect machine-timing) are detected, though physical separation is preferred.
