package com.jsystems.carsimul.service.exam;

import com.jsystems.carsimul.config.ExamRulesProperties;
import com.jsystems.carsimul.domain.ExamSession;
import com.jsystems.carsimul.domain.InfractionSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SpeedingRuleTest {

    private static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");

    private SpeedingRule rule;
    private ExamSession session;

    @BeforeEach
    void setUp() {
        ExamRulesProperties props = new ExamRulesProperties();
        props.setSpeedingDurationSeconds(3);
        props.setCriticalSpeedingOverageKmh(20);
        rule = new SpeedingRule(props);
        session = new ExamSession();
        session.setSpeedLimitKmh(50);
    }

    @Test
    void speedUnderLimitIsClean() {
        assertThat(rule.onSpeedUpdate(session, 45, T0)).isEmpty();
    }

    @Test
    void briefSpikeUnderDurationThresholdIsClean() {
        assertThat(rule.onSpeedUpdate(session, 60, T0)).isEmpty();
        assertThat(rule.onSpeedUpdate(session, 60, T0.plusSeconds(1))).isEmpty();
        // Back under the limit before the 3s threshold — no infraction.
        assertThat(rule.onSpeedUpdate(session, 48, T0.plusSeconds(2))).isEmpty();
    }

    @Test
    void sustainedMinorSpeedingYieldsExactlyOneMinorInfraction() {
        assertThat(rule.onSpeedUpdate(session, 60, T0)).isEmpty();
        Optional<RuleViolation> v = rule.onSpeedUpdate(session, 60, T0.plusSeconds(4));
        assertThat(v).isPresent();
        assertThat(v.get().severity()).isEqualTo(InfractionSeverity.MINOR);
        // Continuing the same episode must NOT produce further infractions.
        assertThat(rule.onSpeedUpdate(session, 62, T0.plusSeconds(6))).isEmpty();
        assertThat(rule.onSpeedUpdate(session, 65, T0.plusSeconds(8))).isEmpty();
    }

    @Test
    void largeOverageIsCritical() {
        rule.onSpeedUpdate(session, 80, T0);
        Optional<RuleViolation> v = rule.onSpeedUpdate(session, 80, T0.plusSeconds(4));
        assertThat(v).isPresent();
        assertThat(v.get().severity()).isEqualTo(InfractionSeverity.CRITICAL);
    }

    @Test
    void newEpisodeAfterSlowingDownIsFlaggedAgain() {
        rule.onSpeedUpdate(session, 60, T0);
        assertThat(rule.onSpeedUpdate(session, 60, T0.plusSeconds(4))).isPresent();
        // Slow down — episode resets.
        rule.onSpeedUpdate(session, 40, T0.plusSeconds(6));
        // Speed up again — a second, separate episode.
        rule.onSpeedUpdate(session, 61, T0.plusSeconds(8));
        assertThat(rule.onSpeedUpdate(session, 61, T0.plusSeconds(12))).isPresent();
    }
}
