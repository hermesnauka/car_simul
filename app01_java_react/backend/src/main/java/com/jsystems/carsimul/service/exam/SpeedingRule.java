package com.jsystems.carsimul.service.exam;

import com.jsystems.carsimul.config.ExamRulesProperties;
import com.jsystems.carsimul.domain.ExamSession;
import com.jsystems.carsimul.domain.InfractionSeverity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Speeding must be sustained (not a momentary blip) to count, and one
 * continuous speeding episode yields exactly one infraction, regardless of how
 * many SPEED_UPDATE ticks arrive while it lasts.
 */
@Component
public class SpeedingRule {

    private final ExamRulesProperties rules;

    public SpeedingRule(ExamRulesProperties rules) {
        this.rules = rules;
    }

    /** Call with an already-validated speed value (see TelemetryValidator). */
    public Optional<RuleViolation> onSpeedUpdate(ExamSession session, double speedKmh, Instant now) {
        session.setCurrentSpeedKmh(speedKmh);
        session.setLastSpeedUpdateAt(now);

        int limit = session.getSpeedLimitKmh();
        if (speedKmh <= limit) {
            // Episode over — reset so the next violation can be flagged again.
            session.setSpeedingSince(null);
            session.setSpeedingEpisodeFlagged(false);
            return Optional.empty();
        }

        if (session.getSpeedingSince() == null) {
            session.setSpeedingSince(now);
            return Optional.empty();
        }

        boolean sustained = Duration.between(session.getSpeedingSince(), now).getSeconds()
                >= rules.getSpeedingDurationSeconds();
        if (!sustained || session.isSpeedingEpisodeFlagged()) {
            return Optional.empty();
        }

        session.setSpeedingEpisodeFlagged(true);
        double overage = speedKmh - limit;
        InfractionSeverity severity = overage > rules.getCriticalSpeedingOverageKmh()
                ? InfractionSeverity.CRITICAL
                : InfractionSeverity.MINOR;
        return Optional.of(new RuleViolation(
                RuleViolation.SPEEDING, severity,
                String.format("Sustained speeding: %.0f km/h in a %d km/h zone", speedKmh, limit)));
    }
}
