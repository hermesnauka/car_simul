package com.jsystems.carsimul.controller;

import com.jsystems.carsimul.dto.exam.ExamEventRequest;
import com.jsystems.carsimul.dto.exam.ExamResultResponse;
import com.jsystems.carsimul.dto.exam.ExamStateResponse;
import com.jsystems.carsimul.service.exam.ExamEngineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exam")
public class ExamController {

    private final ExamEngineService examEngine;

    public ExamController(ExamEngineService examEngine) {
        this.examEngine = examEngine;
    }

    @PostMapping("/start")
    public ResponseEntity<ExamStateResponse> start(@AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(examEngine.startExam(principal.getUsername()));
    }

    @PostMapping("/{sessionId}/events")
    public ExamStateResponse postEvent(@AuthenticationPrincipal UserDetails principal,
                                       @PathVariable Long sessionId,
                                       @Valid @RequestBody ExamEventRequest event) {
        return examEngine.processEvent(principal.getUsername(), sessionId, event);
    }

    @GetMapping("/{sessionId}/state")
    public ExamStateResponse getState(@AuthenticationPrincipal UserDetails principal,
                                      @PathVariable Long sessionId) {
        return examEngine.getState(principal.getUsername(), sessionId);
    }

    /**
     * Note: the request body is deliberately EMPTY. The client cannot claim an
     * outcome — the backend computes it (AS-1 mitigation).
     */
    @PostMapping("/{sessionId}/finish")
    public ExamResultResponse finish(@AuthenticationPrincipal UserDetails principal,
                                     @PathVariable Long sessionId) {
        return examEngine.finishExam(principal.getUsername(), sessionId);
    }
}
