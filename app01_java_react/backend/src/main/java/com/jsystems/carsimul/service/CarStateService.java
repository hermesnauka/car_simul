package com.jsystems.carsimul.service;

import com.jsystems.carsimul.dto.car.CarStateDto;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Practice-mode dashboard state (US-1.2). Deliberately in-memory and per-user:
 * this state is not security-relevant (nothing is graded here), so persistence
 * would be over-engineering for the MVP. Exam state, by contrast, IS persisted
 * and server-authoritative — see the exam package.
 */
@Service
public class CarStateService {

    public static final Set<String> CONTROLS =
            Set.of("hazard", "ac", "headlights", "leftSignal", "rightSignal");

    private final Map<String, Map<String, Boolean>> stateByUser = new ConcurrentHashMap<>();

    public CarStateDto getState(String username) {
        return toDto(userState(username));
    }

    public CarStateDto setControl(String username, String control, boolean on) {
        if (!CONTROLS.contains(control)) {
            throw new IllegalArgumentException("Unknown control: " + control);
        }
        Map<String, Boolean> state = userState(username);
        // Turn signals are mutually exclusive, like a real stalk switch.
        if (on && control.equals("leftSignal")) {
            state.put("rightSignal", false);
        } else if (on && control.equals("rightSignal")) {
            state.put("leftSignal", false);
        }
        state.put(control, on);
        return toDto(state);
    }

    private Map<String, Boolean> userState(String username) {
        return stateByUser.computeIfAbsent(username, u -> {
            Map<String, Boolean> m = new ConcurrentHashMap<>();
            CONTROLS.forEach(c -> m.put(c, false));
            return m;
        });
    }

    private CarStateDto toDto(Map<String, Boolean> state) {
        return new CarStateDto(
                state.getOrDefault("hazard", false),
                state.getOrDefault("ac", false),
                state.getOrDefault("headlights", false),
                state.getOrDefault("leftSignal", false),
                state.getOrDefault("rightSignal", false));
    }
}
