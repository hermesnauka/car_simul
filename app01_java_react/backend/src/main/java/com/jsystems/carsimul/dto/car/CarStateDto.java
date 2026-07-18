package com.jsystems.carsimul.dto.car;

public record CarStateDto(
        boolean hazardLights,
        boolean acOn,
        boolean headlights,
        boolean leftSignal,
        boolean rightSignal) {
}
