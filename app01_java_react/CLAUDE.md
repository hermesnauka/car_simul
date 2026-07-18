# Driving Simulator Educational Platform - AI Assistant Guide

Welcome to the Secure SDLC repository for the Driving Simulator Educational Platform. 
All the photos of car are in directory "./inside".
This platform helps users prepare for driving exams using physical steering wheels, dashboard buttons, and virtual environments.

## 🔗 Related Context
**[Read the AI Agents Personas Configuration Here](AGENTS.md)**

## 🛠️ Technology Stack
*   **Backend:** Java 17+, Spring Boot 3, Spring Security (OAuth2/JWT)
*   **Frontend:** React 18, Three.js / React Three Fiber (for 3D rendering), WebHID / Gamepad API (for hardware input)
*   **Database:** PostgreSQL (encrypted at rest)

## 🔒 Secure SDLC (SSDLC) Guidelines for AI
When writing code or designing architecture for this project, always adhere to the following:
1.  **Input Validation:** Sanitize all inputs from the frontend, especially exam actions and hardware telemetry.
2.  **Authentication & Authorization:** Assume zero trust. All API endpoints must be protected.
3.  **Dependency Management:** Suggest regular audits (e.g., OWASP Dependency-Check).
4.  **Anti-Cheating Mechanisms:** Ensure exam state is calculated and verified server-side to prevent client-side manipulation.

## 🚀 Common Commands
*   **Backend Build:** `./mvnw clean install`
*   **Frontend Build:** `npm run build`
*   **SAST Scan (Local):** `./mvnw sonar:sonar`
