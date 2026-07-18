package com.jsystems.carsimul.domain;

import jakarta.persistence.*;

import java.time.Instant;

/** Raw telemetry event as received — kept for the audit trail (REQ-SEC-04). */
@Entity
@Table(name = "exam_events")
public class ExamEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Direction direction;

    @Column(name = "speed_kmh")
    private Double speedKmh;

    @Column(name = "client_timestamp")
    private Long clientTimestamp;

    /** Server clock — authoritative for all scoring. */
    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    public Long getId() { return id; }
    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public Double getSpeedKmh() { return speedKmh; }
    public void setSpeedKmh(Double speedKmh) { this.speedKmh = speedKmh; }
    public Long getClientTimestamp() { return clientTimestamp; }
    public void setClientTimestamp(Long clientTimestamp) { this.clientTimestamp = clientTimestamp; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
}
