package com.jsystems.carsimul.dto.exam;

import com.jsystems.carsimul.domain.Infraction;

import java.time.Instant;

public record InfractionDto(String ruleCode, String severity, String message, Instant occurredAt) {

    public static InfractionDto from(Infraction i) {
        return new InfractionDto(i.getRuleCode(), i.getSeverity().name(), i.getMessage(), i.getOccurredAt());
    }
}
