package com.jsystems.carsimul.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-0123456789abcdef-0123456789";

    @Test
    void issuedTokenRoundTrips() {
        JwtService service = new JwtService(SECRET, 60, Clock.systemUTC());
        String token = service.issueToken("alice", "STUDENT");
        assertThat(service.validateAndGetUsername(token)).contains("alice");
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService service = new JwtService(SECRET, 60, Clock.systemUTC());
        String token = service.issueToken("alice", "STUDENT");
        String tampered = token.substring(0, token.length() - 4) + "AAAA";
        assertThat(service.validateAndGetUsername(tampered)).isEmpty();
    }

    @Test
    void tokenSignedWithDifferentKeyIsRejected() {
        JwtService issuer = new JwtService(SECRET, 60, Clock.systemUTC());
        JwtService verifier = new JwtService("another-secret-0123456789abcdef-0123456789", 60, Clock.systemUTC());
        String token = issuer.issueToken("alice", "STUDENT");
        assertThat(verifier.validateAndGetUsername(token)).isEmpty();
    }

    @Test
    void expiredTokenIsRejected() {
        Instant issueTime = Instant.parse("2026-01-01T10:00:00Z");
        JwtService issuedInThePast = new JwtService(SECRET, 60,
                Clock.fixed(issueTime, ZoneOffset.UTC));
        String token = issuedInThePast.issueToken("alice", "STUDENT");

        JwtService verifierTwoHoursLater = new JwtService(SECRET, 60,
                Clock.fixed(issueTime.plus(Duration.ofHours(2)), ZoneOffset.UTC));
        assertThat(verifierTwoHoursLater.validateAndGetUsername(token)).isEmpty();
    }

    @Test
    void shortSecretIsRefusedAtStartup() {
        assertThatThrownBy(() -> new JwtService("too-short", 60, Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class);
    }
}
