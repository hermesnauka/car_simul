package com.jsystems.carsimul.dto.exam;

import com.jsystems.carsimul.domain.Direction;
import jakarta.validation.constraints.NotNull;

public class TurnSignalOnEventRequest extends ExamEventRequest {

    @NotNull
    private Direction direction;

    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
}
