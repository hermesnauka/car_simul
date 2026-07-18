package com.jsystems.carsimul.dto.exam;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;

/**
 * Telemetry event contract. Note what is deliberately ABSENT here: there is no
 * score, no pass/fail, no infraction field. The client reports what happened;
 * the server alone decides what it means (AS-1 mitigation). Strict
 * deserialization (fail-on-unknown-properties) rejects any smuggled extras.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TurnSignalOnEventRequest.class, name = "TURN_SIGNAL_ON"),
        @JsonSubTypes.Type(value = TurnSignalOffEventRequest.class, name = "TURN_SIGNAL_OFF"),
        @JsonSubTypes.Type(value = TurnEventRequest.class, name = "TURN"),
        @JsonSubTypes.Type(value = SpeedUpdateEventRequest.class, name = "SPEED_UPDATE")
})
public abstract class ExamEventRequest {

    @NotNull
    private String type;

    /** Client clock, epoch millis. Informational + monotonicity check only. */
    @NotNull
    private Long clientTimestamp;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Long getClientTimestamp() { return clientTimestamp; }
    public void setClientTimestamp(Long clientTimestamp) { this.clientTimestamp = clientTimestamp; }
}
