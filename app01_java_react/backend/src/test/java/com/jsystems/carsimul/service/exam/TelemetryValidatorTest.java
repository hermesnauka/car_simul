package com.jsystems.carsimul.service.exam;

import com.jsystems.carsimul.config.ExamRulesProperties;
import com.jsystems.carsimul.domain.ExamSession;
import com.jsystems.carsimul.exception.InvalidTelemetryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelemetryValidatorTest {

    private static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");

    private TelemetryValidator validator;
    private ExamSession session;

    @BeforeEach
    void setUp() {
        ExamRulesProperties props = new ExamRulesProperties();
        props.setMaxAccelerationKmhPerSecond(15.0);
        validator = new TelemetryValidator(props);
        session = new ExamSession();
    }

    @Test
    void plausibleAccelerationPasses() {
        session.setCurrentSpeedKmh(50);
        session.setLastSpeedUpdateAt(T0);
        // +10 km/h over 1s = 10 km/h/s, under the 15 limit.
        assertThatCode(() -> validator.validateSpeedChange(session, 60, T0.plusSeconds(1)))
                .doesNotThrowAnyException();
    }

    @Test
    void implausibleJumpIsRejected() {
        session.setCurrentSpeedKmh(0);
        session.setLastSpeedUpdateAt(T0);
        // 0 -> 180 in 100ms: the classic scripted-client signature.
        assertThatThrownBy(() -> validator.validateSpeedChange(session, 180, T0.plusMillis(100)))
                .isInstanceOf(InvalidTelemetryException.class);
    }

    @Test
    void implausibleFirstReadingIsRejected() {
        assertThatThrownBy(() -> validator.validateSpeedChange(session, 120, T0))
                .isInstanceOf(InvalidTelemetryException.class);
    }

    @Test
    void nonMonotonicClientTimestampIsRejected() {
        validator.validateMonotonicTimestamp(session, 1000L);
        assertThatThrownBy(() -> validator.validateMonotonicTimestamp(session, 500L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
