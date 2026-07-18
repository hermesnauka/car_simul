package com.jsystems.carsimul.service.exam;

import com.jsystems.carsimul.config.ExamRulesProperties;
import com.jsystems.carsimul.domain.Direction;
import com.jsystems.carsimul.domain.ExamSession;
import com.jsystems.carsimul.domain.InfractionSeverity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * US-2.1: a TURN is only valid if the matching signal is currently on AND was
 * switched on recently (within the lookback window). All times are the server
 * clock — client timestamps are never used for scoring.
 */
@Component
public class TurnSignalRule {

    private final ExamRulesProperties rules;

    public TurnSignalRule(ExamRulesProperties rules) {
        this.rules = rules;
    }

    public void onSignalOn(ExamSession session, Direction direction, Instant now) {
        if (direction == Direction.LEFT) {
            session.setLeftSignalOn(true);
            session.setLastLeftSignalOnAt(now);
            session.setRightSignalOn(false);
        } else {
            session.setRightSignalOn(true);
            session.setLastRightSignalOnAt(now);
            session.setLeftSignalOn(false);
        }
    }

    public void onSignalOff(ExamSession session, Direction direction) {
        if (direction == Direction.LEFT) {
            session.setLeftSignalOn(false);
        } else {
            session.setRightSignalOn(false);
        }
    }

    public Optional<RuleViolation> onTurn(ExamSession session, Direction direction, Instant now) {
        boolean signalOn = session.isSignalOn(direction);
        Instant onAt = session.lastSignalOnAt(direction);
        boolean recentEnough = onAt != null
                && Duration.between(onAt, now).getSeconds() <= rules.getSignalLookbackSeconds();
        if (signalOn && recentEnough) {
            return Optional.empty();
        }
        String reason = !signalOn
                ? "Turned " + direction.name().toLowerCase() + " without an active turn signal"
                : "Turn signal was switched on too long before the turn";
        return Optional.of(new RuleViolation(
                RuleViolation.MISSING_TURN_SIGNAL, InfractionSeverity.CRITICAL, reason));
    }
}
