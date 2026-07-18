package com.jsystems.carsimul.service.exam;

import com.jsystems.carsimul.domain.InfractionSeverity;

/** Result of a rule evaluation before it is persisted as an Infraction. */
public record RuleViolation(String ruleCode, InfractionSeverity severity, String message) {

    public static final String MISSING_TURN_SIGNAL = "MISSING_TURN_SIGNAL";
    public static final String SPEEDING = "SPEEDING";
    public static final String INVALID_TELEMETRY = "INVALID_TELEMETRY";
}
