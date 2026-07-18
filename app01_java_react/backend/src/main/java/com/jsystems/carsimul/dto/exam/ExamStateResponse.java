package com.jsystems.carsimul.dto.exam;

import java.time.Instant;

public record ExamStateResponse(
        Long sessionId,
        String status,
        int currentScore,
        int criticalInfractionCount,
        int totalInfractionCount,
        int speedLimitKmh,
        Instant startedAt,
        InfractionDto lastInfraction) {
}
