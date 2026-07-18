package com.jsystems.carsimul.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Tunable exam rule thresholds. Instructors can adjust these in application.yml
 * without recompiling.
 */
@ConfigurationProperties(prefix = "examsim.rules")
public class ExamRulesProperties {

    /** A TURN is valid only if the matching signal was switched on within this window. */
    private long signalLookbackSeconds = 10;

    /** Speeding must be sustained this long before it counts as an infraction. */
    private long speedingDurationSeconds = 3;

    /** Overage above the limit (km/h) at which speeding becomes CRITICAL instead of MINOR. */
    private int criticalSpeedingOverageKmh = 20;

    /** Maximum physically plausible acceleration, km/h per second (REQ-SEC-03). */
    private double maxAccelerationKmhPerSecond = 15.0;

    /** Exam auto-fails once this many CRITICAL infractions are recorded. */
    private int maxCriticalInfractions = 3;

    /** Minimum final score required to pass. */
    private int passThreshold = 70;

    /** Score penalty per critical infraction. */
    private int criticalPenalty = 20;

    /** Score penalty per minor infraction. */
    private int minorPenalty = 5;

    /** Posted speed limit for the MVP's single exam route. */
    private int speedLimitKmh = 50;

    public long getSignalLookbackSeconds() { return signalLookbackSeconds; }
    public void setSignalLookbackSeconds(long v) { this.signalLookbackSeconds = v; }
    public long getSpeedingDurationSeconds() { return speedingDurationSeconds; }
    public void setSpeedingDurationSeconds(long v) { this.speedingDurationSeconds = v; }
    public int getCriticalSpeedingOverageKmh() { return criticalSpeedingOverageKmh; }
    public void setCriticalSpeedingOverageKmh(int v) { this.criticalSpeedingOverageKmh = v; }
    public double getMaxAccelerationKmhPerSecond() { return maxAccelerationKmhPerSecond; }
    public void setMaxAccelerationKmhPerSecond(double v) { this.maxAccelerationKmhPerSecond = v; }
    public int getMaxCriticalInfractions() { return maxCriticalInfractions; }
    public void setMaxCriticalInfractions(int v) { this.maxCriticalInfractions = v; }
    public int getPassThreshold() { return passThreshold; }
    public void setPassThreshold(int v) { this.passThreshold = v; }
    public int getCriticalPenalty() { return criticalPenalty; }
    public void setCriticalPenalty(int v) { this.criticalPenalty = v; }
    public int getMinorPenalty() { return minorPenalty; }
    public void setMinorPenalty(int v) { this.minorPenalty = v; }
    public int getSpeedLimitKmh() { return speedLimitKmh; }
    public void setSpeedLimitKmh(int v) { this.speedLimitKmh = v; }
}
