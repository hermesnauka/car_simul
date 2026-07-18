package com.jsystems.carsimul.controller;

import com.jsystems.carsimul.dto.car.CarControlUpdateRequest;
import com.jsystems.carsimul.dto.car.CarStateDto;
import com.jsystems.carsimul.service.CarStateService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/car")
public class CarStateController {

    private final CarStateService carStateService;

    public CarStateController(CarStateService carStateService) {
        this.carStateService = carStateService;
    }

    @GetMapping("/state")
    public CarStateDto getState(@AuthenticationPrincipal UserDetails principal) {
        return carStateService.getState(principal.getUsername());
    }

    @PostMapping("/controls/{control}")
    public CarStateDto setControl(@AuthenticationPrincipal UserDetails principal,
                                  @PathVariable String control,
                                  @Valid @RequestBody CarControlUpdateRequest request) {
        return carStateService.setControl(principal.getUsername(), control, request.on());
    }
}
