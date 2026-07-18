package com.jsystems.carsimul.dto.auth;

public record AuthResponse(String token, String username, String role) {
}
