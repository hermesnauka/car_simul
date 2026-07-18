package com.jsystems.carsimul.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "infractions")
public class Infraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "rule_code", nullable = false, length = 40)
    private String ruleCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private InfractionSeverity severity;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    public Long getId() { return id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public InfractionSeverity getSeverity() { return severity; }
    public void setSeverity(InfractionSeverity severity) { this.severity = severity; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }
}
