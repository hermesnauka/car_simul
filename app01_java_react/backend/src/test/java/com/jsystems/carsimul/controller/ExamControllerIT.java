package com.jsystems.carsimul.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Exercises the server-authoritative exam flow, including the abuse cases from
 * USER_STORIES.md (AS-1): a hostile client cannot claim a PASS.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExamControllerIT {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private String token;
    private final AtomicLong clientClock = new AtomicLong(1_000_000);

    @BeforeEach
    void registerAndLogin() throws Exception {
        String body = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "student1", "password", "s3cretPass"))))
                .andReturn().getResponse().getContentAsString();
        token = json.readTree(body).get("token").asText();
    }

    private long startExam() throws Exception {
        String body = mvc.perform(post("/api/exam/start").header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.currentScore").value(100))
                .andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("sessionId").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions postEvent(long sessionId, String payload) throws Exception {
        return mvc.perform(post("/api/exam/" + sessionId + "/events")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));
    }

    private String turnEvent(String direction) {
        return """
                {"type":"TURN","direction":"%s","clientTimestamp":%d}"""
                .formatted(direction, clientClock.incrementAndGet());
    }

    private String signalOnEvent(String direction) {
        return """
                {"type":"TURN_SIGNAL_ON","direction":"%s","clientTimestamp":%d}"""
                .formatted(direction, clientClock.incrementAndGet());
    }

    @Test
    void turnWithSignalIsClean() throws Exception {
        long sessionId = startExam();
        postEvent(sessionId, signalOnEvent("LEFT")).andExpect(status().isOk());
        postEvent(sessionId, turnEvent("LEFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentScore").value(100))
                .andExpect(jsonPath("$.totalInfractionCount").value(0));
    }

    @Test
    void turnWithoutSignalCostsTwentyPoints() throws Exception {
        long sessionId = startExam();
        postEvent(sessionId, turnEvent("LEFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentScore").value(80))
                .andExpect(jsonPath("$.criticalInfractionCount").value(1))
                .andExpect(jsonPath("$.lastInfraction.ruleCode").value("MISSING_TURN_SIGNAL"));
    }

    @Test
    void threeCriticalInfractionsAutoFailAndLockTheSession() throws Exception {
        long sessionId = startExam();
        postEvent(sessionId, turnEvent("LEFT")).andExpect(status().isOk());
        postEvent(sessionId, turnEvent("RIGHT")).andExpect(status().isOk());
        postEvent(sessionId, turnEvent("LEFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
        // Any further event is rejected: the exam is over, no grinding it back.
        postEvent(sessionId, signalOnEvent("LEFT"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("EXAM_ALREADY_TERMINATED"));
    }

    @Test
    void cleanExamPasses() throws Exception {
        long sessionId = startExam();
        postEvent(sessionId, signalOnEvent("LEFT")).andExpect(status().isOk());
        postEvent(sessionId, turnEvent("LEFT")).andExpect(status().isOk());
        mvc.perform(post("/api/exam/" + sessionId + "/finish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PASSED"))
                .andExpect(jsonPath("$.finalScore").value(100));
    }

    // ------- Abuse cases (AS-1 and friends) -------

    @Test
    void tamperAttempt_injectedResultFieldsInFinishAreIgnored() throws Exception {
        long sessionId = startExam();
        postEvent(sessionId, turnEvent("LEFT")).andExpect(status().isOk());
        postEvent(sessionId, turnEvent("RIGHT")).andExpect(status().isOk());
        // Hostile client claims a pass in the finish body. There is no
        // request-body binding on /finish, so the claim is inert.
        mvc.perform(post("/api/exam/" + sessionId + "/finish")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PASSED\",\"finalScore\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.finalScore").value(60));
    }

    @Test
    void tamperAttempt_extraFieldSmuggledIntoEventIs400() throws Exception {
        long sessionId = startExam();
        postEvent(sessionId, """
                {"type":"TURN","direction":"LEFT","clientTimestamp":%d,"score":100}"""
                .formatted(clientClock.incrementAndGet()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("MALFORMED_REQUEST"));
    }

    @Test
    void tamperAttempt_implausibleSpeedJumpIs422AndCostsACritical() throws Exception {
        long sessionId = startExam();
        postEvent(sessionId, """
                {"type":"SPEED_UPDATE","speedKmh":10,"clientTimestamp":%d}"""
                .formatted(clientClock.incrementAndGet()))
                .andExpect(status().isOk());
        // 10 -> 180 km/h within milliseconds of server time: rejected AND penalized.
        postEvent(sessionId, """
                {"type":"SPEED_UPDATE","speedKmh":180,"clientTimestamp":%d}"""
                .formatted(clientClock.incrementAndGet()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("IMPLAUSIBLE_TELEMETRY"));
        mvc.perform(post("/api/exam/" + sessionId + "/finish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criticalInfractionCount").value(1))
                .andExpect(jsonPath("$.infractions[0].ruleCode").value("INVALID_TELEMETRY"));
    }

    @Test
    void tamperAttempt_anotherUsersSessionIs403() throws Exception {
        long sessionId = startExam();
        String otherBody = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("username", "intruder", "password", "s3cretPass"))))
                .andReturn().getResponse().getContentAsString();
        String otherToken = json.readTree(otherBody).get("token").asText();

        mvc.perform(post("/api/exam/" + sessionId + "/events")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(turnEvent("LEFT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void tamperAttempt_nonMonotonicClientTimestampIs400() throws Exception {
        long sessionId = startExam();
        postEvent(sessionId, signalOnEvent("LEFT")).andExpect(status().isOk());
        postEvent(sessionId, """
                {"type":"TURN","direction":"LEFT","clientTimestamp":1}""")
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownSessionIs404() throws Exception {
        mvc.perform(post("/api/exam/999999/finish").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
