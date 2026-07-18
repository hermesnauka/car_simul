package com.jsystems.carsimul.service.exam;

import com.jsystems.carsimul.config.ExamRulesProperties;
import com.jsystems.carsimul.domain.*;
import com.jsystems.carsimul.dto.exam.*;
import com.jsystems.carsimul.exception.ExamNotActiveException;
import com.jsystems.carsimul.exception.ForbiddenSessionAccessException;
import com.jsystems.carsimul.exception.InvalidTelemetryException;
import com.jsystems.carsimul.exception.ResourceNotFoundException;
import com.jsystems.carsimul.repository.ExamEventRepository;
import com.jsystems.carsimul.repository.ExamSessionRepository;
import com.jsystems.carsimul.repository.InfractionRepository;
import com.jsystems.carsimul.repository.UserRepository;
import com.jsystems.carsimul.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * The server-authoritative exam brain. The client streams raw events; this
 * service alone interprets them, records infractions, and decides PASS/FAIL.
 */
@Service
public class ExamEngineService {

    private final ExamSessionRepository sessionRepository;
    private final ExamEventRepository eventRepository;
    private final InfractionRepository infractionRepository;
    private final UserRepository userRepository;
    private final TurnSignalRule turnSignalRule;
    private final SpeedingRule speedingRule;
    private final TelemetryValidator telemetryValidator;
    private final ExamRulesProperties rules;
    private final AuditLogService audit;
    private final Clock clock;

    public ExamEngineService(ExamSessionRepository sessionRepository,
                             ExamEventRepository eventRepository,
                             InfractionRepository infractionRepository,
                             UserRepository userRepository,
                             TurnSignalRule turnSignalRule,
                             SpeedingRule speedingRule,
                             TelemetryValidator telemetryValidator,
                             ExamRulesProperties rules,
                             AuditLogService audit,
                             Clock clock) {
        this.sessionRepository = sessionRepository;
        this.eventRepository = eventRepository;
        this.infractionRepository = infractionRepository;
        this.userRepository = userRepository;
        this.turnSignalRule = turnSignalRule;
        this.speedingRule = speedingRule;
        this.telemetryValidator = telemetryValidator;
        this.rules = rules;
        this.audit = audit;
        this.clock = clock;
    }

    @Transactional
    public ExamStateResponse startExam(String username) {
        User user = requireUser(username);
        ExamSession session = new ExamSession();
        session.setUserId(user.getId());
        session.setStartedAt(clock.instant());
        session.setSpeedLimitKmh(rules.getSpeedLimitKmh());
        sessionRepository.save(session);
        audit.examStarted(username, session.getId());
        return toStateResponse(session, null);
    }

    // noRollbackFor: rejected telemetry must still COMMIT its penalty — a
    // cheating attempt that rolls back its own infraction would be free.
    @Transactional(noRollbackFor = InvalidTelemetryException.class)
    public ExamStateResponse processEvent(String username, Long sessionId, ExamEventRequest event) {
        ExamSession session = requireOwnedSession(username, sessionId);
        requireActive(session);
        Instant now = clock.instant();

        telemetryValidator.validateMonotonicTimestamp(session, event.getClientTimestamp());
        recordRawEvent(session, event, now);

        Optional<RuleViolation> violation;
        try {
            violation = evaluate(session, event, now);
        } catch (InvalidTelemetryException e) {
            // Implausible telemetry is itself a critical infraction: cheating
            // attempts must cost the cheater, not just be silently dropped.
            audit.implausibleTelemetry(username, sessionId, e.getMessage());
            applyViolation(session, new RuleViolation(
                    RuleViolation.INVALID_TELEMETRY, InfractionSeverity.CRITICAL, e.getMessage()), now, username);
            sessionRepository.save(session);
            throw e;
        }

        violation.ifPresent(v -> applyViolation(session, v, now, username));
        sessionRepository.save(session);
        return toStateResponse(session, violation.isPresent() ? lastInfraction(session) : null);
    }

    @Transactional(readOnly = true)
    public ExamStateResponse getState(String username, Long sessionId) {
        ExamSession session = requireOwnedSession(username, sessionId);
        return toStateResponse(session, null);
    }

    @Transactional
    public ExamResultResponse finishExam(String username, Long sessionId) {
        ExamSession session = requireOwnedSession(username, sessionId);
        if (session.getStatus() == ExamStatus.IN_PROGRESS) {
            boolean failed = session.getScore() < rules.getPassThreshold()
                    || session.getCriticalInfractionCount() >= rules.getMaxCriticalInfractions();
            session.setStatus(failed ? ExamStatus.FAILED : ExamStatus.PASSED);
            session.setEndedAt(clock.instant());
            sessionRepository.save(session);
            audit.examTerminated(username, sessionId, session.getStatus().name(), session.getScore());
        }
        List<InfractionDto> infractions = infractionRepository
                .findBySessionIdOrderByOccurredAtAsc(sessionId).stream()
                .map(InfractionDto::from)
                .toList();
        return new ExamResultResponse(
                session.getId(), session.getStatus().name(), session.getScore(),
                session.getCriticalInfractionCount(), session.getTotalInfractionCount(),
                infractions, session.getStartedAt(), session.getEndedAt());
    }

    // --- internals ---

    private Optional<RuleViolation> evaluate(ExamSession session, ExamEventRequest event, Instant now) {
        if (event instanceof TurnSignalOnEventRequest e) {
            turnSignalRule.onSignalOn(session, e.getDirection(), now);
            return Optional.empty();
        }
        if (event instanceof TurnSignalOffEventRequest e) {
            turnSignalRule.onSignalOff(session, e.getDirection());
            return Optional.empty();
        }
        if (event instanceof TurnEventRequest e) {
            return turnSignalRule.onTurn(session, e.getDirection(), now);
        }
        if (event instanceof SpeedUpdateEventRequest e) {
            telemetryValidator.validateSpeedChange(session, e.getSpeedKmh(), now);
            return speedingRule.onSpeedUpdate(session, e.getSpeedKmh(), now);
        }
        throw new IllegalArgumentException("Unsupported event type: " + event.getType());
    }

    private void applyViolation(ExamSession session, RuleViolation violation, Instant now, String username) {
        Infraction infraction = new Infraction();
        infraction.setSessionId(session.getId());
        infraction.setRuleCode(violation.ruleCode());
        infraction.setSeverity(violation.severity());
        infraction.setMessage(violation.message());
        infraction.setOccurredAt(now);
        infractionRepository.save(infraction);

        int penalty = violation.severity() == InfractionSeverity.CRITICAL
                ? rules.getCriticalPenalty() : rules.getMinorPenalty();
        session.setScore(Math.max(0, session.getScore() - penalty));
        session.setTotalInfractionCount(session.getTotalInfractionCount() + 1);
        if (violation.severity() == InfractionSeverity.CRITICAL) {
            session.setCriticalInfractionCount(session.getCriticalInfractionCount() + 1);
        }
        audit.infractionRecorded(username, session.getId(), violation.ruleCode(), violation.severity().name());

        if (session.getCriticalInfractionCount() >= rules.getMaxCriticalInfractions()
                && session.getStatus() == ExamStatus.IN_PROGRESS) {
            session.setStatus(ExamStatus.FAILED);
            session.setEndedAt(now);
            audit.examTerminated(username, session.getId(), ExamStatus.FAILED.name(), session.getScore());
        }
    }

    private void recordRawEvent(ExamSession session, ExamEventRequest event, Instant now) {
        ExamEvent raw = new ExamEvent();
        raw.setSessionId(session.getId());
        raw.setEventType(event.getType());
        raw.setClientTimestamp(event.getClientTimestamp());
        raw.setReceivedAt(now);
        if (event instanceof TurnSignalOnEventRequest e) raw.setDirection(e.getDirection());
        if (event instanceof TurnSignalOffEventRequest e) raw.setDirection(e.getDirection());
        if (event instanceof TurnEventRequest e) raw.setDirection(e.getDirection());
        if (event instanceof SpeedUpdateEventRequest e) raw.setSpeedKmh(e.getSpeedKmh());
        eventRepository.save(raw);
    }

    private User requireUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ExamSession requireOwnedSession(String username, Long sessionId) {
        ExamSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam session not found: " + sessionId));
        User user = requireUser(username);
        if (!session.getUserId().equals(user.getId())) {
            throw new ForbiddenSessionAccessException(sessionId);
        }
        return session;
    }

    private void requireActive(ExamSession session) {
        if (session.getStatus() != ExamStatus.IN_PROGRESS) {
            throw new ExamNotActiveException(session.getId(), session.getStatus().name());
        }
    }

    private InfractionDto lastInfraction(ExamSession session) {
        List<Infraction> all = infractionRepository.findBySessionIdOrderByOccurredAtAsc(session.getId());
        return all.isEmpty() ? null : InfractionDto.from(all.get(all.size() - 1));
    }

    private ExamStateResponse toStateResponse(ExamSession session, InfractionDto lastInfraction) {
        return new ExamStateResponse(
                session.getId(), session.getStatus().name(), session.getScore(),
                session.getCriticalInfractionCount(), session.getTotalInfractionCount(),
                session.getSpeedLimitKmh(), session.getStartedAt(), lastInfraction);
    }
}
