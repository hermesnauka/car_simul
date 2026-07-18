package com.jsystems.carsimul.dto.car;

import jakarta.validation.constraints.NotNull;

public record CarControlUpdateRequest(@NotNull Boolean on) {
}
