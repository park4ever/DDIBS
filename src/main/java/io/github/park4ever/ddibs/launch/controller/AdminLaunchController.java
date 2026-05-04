package io.github.park4ever.ddibs.launch.controller;

import io.github.park4ever.ddibs.launch.dto.*;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchDetailResponse;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchSearchRequest;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchSummaryResponse;
import io.github.park4ever.ddibs.launch.service.AdminLaunchQueryService;
import io.github.park4ever.ddibs.launch.service.LaunchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/launches")
public class AdminLaunchController {

    private final LaunchService launchService;
    private final AdminLaunchQueryService adminLaunchQueryService;

    @PostMapping
    public ResponseEntity<CreateLaunchResponse> createLaunch(
            @Valid @RequestBody CreateLaunchRequest request
    ) {
        CreateLaunchResponse response = launchService.createLaunch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{launchId}")
    public ResponseEntity<AdminLaunchDetailResponse> getLaunch(
            @PathVariable("launchId") Long launchId
    ) {
        AdminLaunchDetailResponse response = adminLaunchQueryService.getLaunchDetail(launchId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<AdminLaunchSummaryResponse>> getLaunches(
            @ModelAttribute AdminLaunchSearchRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<AdminLaunchSummaryResponse> response =
                adminLaunchQueryService.searchLaunches(request, pageable);

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
