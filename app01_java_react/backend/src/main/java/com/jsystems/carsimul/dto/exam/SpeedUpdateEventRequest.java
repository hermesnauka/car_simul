package com.jsystems.carsimul.dto.exam;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class SpeedUpdateEventRequest extends ExamEventRequest {

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("220.0")
    private Double speedKmh;

    public Double getSpeedKmh() { return speedKmh; }
    public void setSpeedKmh(Double speedKmh) { this.speedKmh = speedKmh; }
}
