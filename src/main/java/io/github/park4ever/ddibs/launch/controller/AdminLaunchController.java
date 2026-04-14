package io.github.park4ever.ddibs.launch.controller;

import io.github.park4ever.ddibs.launch.dto.*;
import io.github.park4ever.ddibs.launch.service.LaunchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/launches")
public class AdminLaunchController {

    private final LaunchService launchService;

    @PostMapping
    public ResponseEntity<CreateLaunchResponse> createLaunch(
            @Valid @RequestBody CreateLaunchRequest request
    ) {
        CreateLaunchResponse response = launchService.createLaunch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{launchId}")
    public ResponseEntity<LaunchResponse> getLaunch(
            @PathVariable("launchId") Long launchId
    ) {
        LaunchResponse response = launchService.getLaunch(launchId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<LaunchSummaryResponse>> getLaunches() {
        List<LaunchSummaryResponse> response = launchService.getLaunches();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{launchId}/status")
    public ResponseEntity<LaunchResponse> updateLaunchStatus(
            @PathVariable("launchId") Long launchId,
            @Valid @RequestBody UpdateLaunchStatusRequest request
    ) {
        LaunchResponse response = launchService.updateLaunchStatus(launchId, request);
        return ResponseEntity.ok(response);
    }
}
