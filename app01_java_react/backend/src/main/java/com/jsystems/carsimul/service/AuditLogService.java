package com.jsystems.carsimul.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Non-repudiation audit trail (REQ-SEC-04). Writes to a dedicated "AUDIT"
 * logger so operations can route it to a separate, append-only sink.
 */
@Service
public class AuditLogService {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    public void userRegistered(String username) {
        AUDIT.info("USER_REGISTERED user={}", username);
    }

    public void examStarted(String username, Long sessionId) {
        AUDIT.info("EXAM_STARTED user={} session={}", username, sessionId);
    }

    public void infractionRecorded(String username, Long sessionId, String ruleCode, String severity) {
        AUDIT.warn("INFRACTION user={} session={} rule={} severity={}", username, sessionId, ruleCode, severity);
    }

    public void examTerminated(String username, Long sessionId, String status, int finalScore) {
        AUDIT.info("EXAM_TERMINATED user={} session={} status={} score={}", username, sessionId, status, finalScore);
    }

    public void suspiciousPayload(String username, String detail) {
        AUDIT.warn("SUSPICIOUS_PAYLOAD user={} detail={}", username, detail);
    }

    public void implausibleTelemetry(String username, Long sessionId, String detail) {
        AUDIT.warn("IMPLAUSIBLE_TELEMETRY user={} session={} detail={}", username, sessionId, detail);
    }
}
