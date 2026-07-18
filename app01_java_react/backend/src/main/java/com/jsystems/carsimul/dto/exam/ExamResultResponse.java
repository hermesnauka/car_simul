package com.jsystems.carsimul.dto.exam;

import java.time.Instant;
import java.util.List;

public record ExamResultResponse(
        Long sessionId,
        String status,
        int finalScore,
        int criticalInfractionCount,
        int totalInfractionCount,
        List<InfractionDto> infractions,
        Instant startedAt,
        Instant endedAt) {
}
