# 📅 Secure SDLC Project Plan: Driving Simulator Platform

All the photos of car are in directory "./inside".

## Phase 1: Planning & Risk Assessment
*   **Objectives:** Define project scope, identify regulatory requirements (driving exam standards), and perform initial risk assessment.
*   **Security Focus:** Data Privacy Impact Assessment (DPIA) for student data, identification of critical assets (exam results, user credentials).
*   **Milestone:** Approved Requirements & Risk Profile.

## Phase 2: Secure Design & Architecture
*   **Objectives:** System architecture design mapping React frontend to Spring Boot backend.
*   **Security Focus:** Threat Modeling (STRIDE methodology). Defining security controls (e.g., rate limiting, anti-tampering for exam payloads, TLS 1.3).
*   **Milestone:** Threat Model Document, Architecture Diagrams.

## Phase 3: Secure Development
*   **Objectives:** Sprint-based implementation of User Stories. Hardware integration via Gamepad API.
*   **Security Focus:** 
    *   Peer code reviews.
    *   Static Application Security Testing (SAST) in CI/CD pipeline (e.g., SonarQube).
    *   Software Composition Analysis (SCA) to avoid vulnerable npm/Maven dependencies.
*   **Milestone:** Feature-complete MVP with zero critical SAST findings.

## Phase 4: Security Testing & QA
*   **Objectives:** Functional testing of the simulator, driving physics, and exam flow.
*   **Security Focus:**
    *   Dynamic Application Security Testing (DAST).
    *   Manual Penetration Testing (focusing on cheating the exam system).
    *   Abuse case execution.
*   **Milestone:** Penetration Test Sign-off, QA Sign-off.

## Phase 5: Deployment & Operations
*   **Objectives:** Launch the platform to the production environment.
*   **Security Focus:** Infrastructure as Code (IaC) security scanning, WAF configuration, continuous monitoring, and Incident Response Plan activation.
*   **Milestone:** Go-Live.
