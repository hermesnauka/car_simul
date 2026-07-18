package com.jsystems.carsimul.service.exam;

import com.jsystems.carsimul.config.ExamRulesProperties;
import com.jsystems.carsimul.domain.ExamSession;
import com.jsystems.carsimul.exception.InvalidTelemetryException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * REQ-SEC-03: physical-plausibility checks on incoming telemetry. A speed that
 * jumps 0→180 in 100ms is not a car, it's a script.
 */
@Component
public class TelemetryValidator {

    private final ExamRulesProperties rules;

    public TelemetryValidator(ExamRulesProperties rules) {
        this.rules = rules;
    }

    /**
     * @throws InvalidTelemetryException if the speed change is physically
     *         implausible given the elapsed server-side time.
     */
    public void validateSpeedChange(ExamSession session, double newSpeedKmh, Instant now) {
        Instant lastUpdate = session.getLastSpeedUpdateAt();
        if (lastUpdate == null) {
            // First reading: only plausible if starting near standstill.
            if (newSpeedKmh > 30) {
                throw new InvalidTelemetryException(
                        "Initial speed reading of " + newSpeedKmh + " km/h is implausible");
            }
            return;
        }
        double elapsedSeconds = Math.max(Duration.between(lastUpdate, now).toMillis() / 1000.0, 0.001);
        double deltaV = Math.abs(newSpeedKmh - session.getCurrentSpeedKmh());
        double accel = deltaV / elapsedSeconds;
        if (accel > rules.getMaxAccelerationKmhPerSecond()) {
            throw new InvalidTelemetryException(String.format(
                    "Speed change of %.1f km/h in %.2fs (%.1f km/h/s) exceeds plausible acceleration",
                    deltaV, elapsedSeconds, accel));
        }
    }

    /** Cheap anti-replay: client timestamps must be monotonically increasing. */
    public void validateMonotonicTimestamp(ExamSession session, long clientTimestamp) {
        Long last = session.getLastClientTimestamp();
        if (last != null && clientTimestamp < last) {
            throw new IllegalArgumentException(
                    "Event clientTimestamp is older than a previously received event");
        }
        session.setLastClientTimestamp(clientTimestamp);
    }
}
