package io.github.park4ever.ddibs.launchvariant.controller;

import io.github.park4ever.ddibs.launchvariant.dto.CreateLaunchVariantRequest;
import io.github.park4ever.ddibs.launchvariant.dto.CreateLaunchVariantResponse;
import io.github.park4ever.ddibs.launchvariant.dto.LaunchVariantResponse;
import io.github.park4ever.ddibs.launchvariant.dto.LaunchVariantSummaryResponse;
import io.github.park4ever.ddibs.launchvariant.service.LaunchVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/launch-variants")
public class AdminLaunchVariantController {

    private final LaunchVariantService launchVariantService;

    @PostMapping
    public ResponseEntity<CreateLaunchVariantResponse> createLaunchVariant(
            @Valid @RequestBody CreateLaunchVariantRequest request
    ) {
        CreateLaunchVariantResponse response = launchVariantService.createLaunchVariant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{launchVariantId}")
    public ResponseEntity<LaunchVariantResponse> getLaunchVariant(
            @PathVariable("launchVariantId") Long launchVariantId
    ) {
        LaunchVariantResponse response = launchVariantService.getLaunchVariant(launchVariantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<LaunchVariantSummaryResponse>> getLaunchVariants() {
        List<LaunchVariantSummaryResponse> response = launchVariantService.getLaunchVariants();
        return ResponseEntity.ok(response);
    }


}
