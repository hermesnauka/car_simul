package com.jsystems.carsimul.service.exam;

import com.jsystems.carsimul.config.ExamRulesProperties;
import com.jsystems.carsimul.domain.Direction;
import com.jsystems.carsimul.domain.ExamSession;
import com.jsystems.carsimul.domain.InfractionSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Pure unit tests — no Spring context, no mocks. */
class TurnSignalRuleTest {

    private static final Instant T0 = Instant.parse("2026-01-01T10:00:00Z");

    private TurnSignalRule rule;
    private ExamSession session;

    @BeforeEach
    void setUp() {
        ExamRulesProperties props = new ExamRulesProperties();
        props.setSignalLookbackSeconds(10);
        rule = new TurnSignalRule(props);
        session = new ExamSession();
    }

    @Test
    void turnWithRecentSignalIsClean() {
        rule.onSignalOn(session, Direction.LEFT, T0);
        Optional<RuleViolation> v = rule.onTurn(session, Direction.LEFT, T0.plusSeconds(3));
        assertThat(v).isEmpty();
    }

    @Test
    void turnWithoutSignalIsCritical() {
        Optional<RuleViolation> v = rule.onTurn(session, Direction.LEFT, T0);
        assertThat(v).isPresent();
        assertThat(v.get().ruleCode()).isEqualTo(RuleViolation.MISSING_TURN_SIGNAL);
        assertThat(v.get().severity()).isEqualTo(InfractionSeverity.CRITICAL);
    }

    @Test
    void turnWithWrongDirectionSignalIsCritical() {
        rule.onSignalOn(session, Direction.RIGHT, T0);
        Optional<RuleViolation> v = rule.onTurn(session, Direction.LEFT, T0.plusSeconds(1));
        assertThat(v).isPresent();
    }

    @Test
    void turnAfterSignalSwitchedOffIsCritical() {
        rule.onSignalOn(session, Direction.LEFT, T0);
        rule.onSignalOff(session, Direction.LEFT);
        Optional<RuleViolation> v = rule.onTurn(session, Direction.LEFT, T0.plusSeconds(2));
        assertThat(v).isPresent();
    }

    @Test
    void turnWithStaleSignalBeyondLookbackIsCritical() {
        rule.onSignalOn(session, Direction.LEFT, T0);
        Optional<RuleViolation> v = rule.onTurn(session, Direction.LEFT, T0.plusSeconds(11));
        assertThat(v).isPresent();
        assertThat(v.get().message()).contains("too long before");
    }

    @Test
    void switchingSignalDirectionTurnsOffTheOther() {
        rule.onSignalOn(session, Direction.LEFT, T0);
        rule.onSignalOn(session, Direction.RIGHT, T0.plusSeconds(1));
        assertThat(session.isLeftSignalOn()).isFalse();
        assertThat(session.isRightSignalOn()).isTrue();
    }
}
