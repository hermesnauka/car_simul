package com.jsystems.carsimul.domain;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * Server-authoritative exam state. Every field the scoring rules depend on is
 * persisted on this row — never in a parallel in-memory map (which would lose
 * state on restart and break with more than one backend instance) and never
 * trusted from the client (AS-1 mitigation).
 */
@Entity
@Table(name = "exam_sessions")
public class ExamSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExamStatus status = ExamStatus.IN_PROGRESS;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "speed_limit_kmh", nullable = false)
    private int speedLimitKmh;

    @Column(nullable = false)
    private int score = 100;

    @Column(name = "critical_infraction_count", nullable = false)
    private int criticalInfractionCount;

    @Column(name = "total_infraction_count", nullable = false)
    private int totalInfractionCount;

    // --- rule state: speed ---
    @Column(name = "current_speed_kmh", nullable = false)
    private double currentSpeedKmh;

    @Column(name = "last_speed_update_at")
    private Instant lastSpeedUpdateAt;

    @Column(name = "speeding_since")
    private Instant speedingSince;

    @Column(name = "speeding_episode_flagged", nullable = false)
    private boolean speedingEpisodeFlagged;

    // --- rule state: turn signals ---
    @Column(name = "left_signal_on", nullable = false)
    private boolean leftSignalOn;

    @Column(name = "right_signal_on", nullable = false)
    private boolean rightSignalOn;

    @Column(name = "last_left_signal_on_at")
    private Instant lastLeftSignalOnAt;

    @Column(name = "last_right_signal_on_at")
    private Instant lastRightSignalOnAt;

    /** Millis; used only for a cheap monotonicity/anti-replay check, never for scoring. */
    @Column(name = "last_client_timestamp")
    private Long lastClientTimestamp;

    @Version
    @Column(nullable = false)
    private long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public ExamStatus getStatus() { return status; }
    public void setStatus(ExamStatus status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }
    public int getSpeedLimitKmh() { return speedLimitKmh; }
    public void setSpeedLimitKmh(int speedLimitKmh) { this.speedLimitKmh = speedLimitKmh; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public int getCriticalInfractionCount() { return criticalInfractionCount; }
    public void setCriticalInfractionCount(int v) { this.criticalInfractionCount = v; }
    public int getTotalInfractionCount() { return totalInfractionCount; }
    public void setTotalInfractionCount(int v) { this.totalInfractionCount = v; }
    public double getCurrentSpeedKmh() { return currentSpeedKmh; }
    public void setCurrentSpeedKmh(double v) { this.currentSpeedKmh = v; }
    public Instant getLastSpeedUpdateAt() { return lastSpeedUpdateAt; }
    public void setLastSpeedUpdateAt(Instant v) { this.lastSpeedUpdateAt = v; }
    public Instant getSpeedingSince() { return speedingSince; }
    public void setSpeedingSince(Instant v) { this.speedingSince = v; }
    public boolean isSpeedingEpisodeFlagged() { return speedingEpisodeFlagged; }
    public void setSpeedingEpisodeFlagged(boolean v) { this.speedingEpisodeFlagged = v; }
    public boolean isLeftSignalOn() { return leftSignalOn; }
    public void setLeftSignalOn(boolean v) { this.leftSignalOn = v; }
    public boolean isRightSignalOn() { return rightSignalOn; }
    public void setRightSignalOn(boolean v) { this.rightSignalOn = v; }
    public Instant getLastLeftSignalOnAt() { return lastLeftSignalOnAt; }
    public void setLastLeftSignalOnAt(Instant v) { this.lastLeftSignalOnAt = v; }
    public Instant getLastRightSignalOnAt() { return lastRightSignalOnAt; }
    public void setLastRightSignalOnAt(Instant v) { this.lastRightSignalOnAt = v; }
    public Long getLastClientTimestamp() { return lastClientTimestamp; }
    public void setLastClientTimestamp(Long v) { this.lastClientTimestamp = v; }
    public long getVersion() { return version; }

    public boolean isSignalOn(Direction direction) {
        return direction == Direction.LEFT ? leftSignalOn : rightSignalOn;
    }

    public Instant lastSignalOnAt(Direction direction) {
        return direction == Direction.LEFT ? lastLeftSignalOnAt : lastRightSignalOnAt;
    }
}
