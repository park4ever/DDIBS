package io.github.park4ever.ddibs.seller.controller;

import io.github.park4ever.ddibs.seller.dto.*;
import io.github.park4ever.ddibs.seller.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/sellers")
public class AdminSellerController {

    private final SellerService sellerService;

    @PostMapping
    public ResponseEntity<CreateSellerResponse> createSeller(
            @Valid @RequestBody CreateSellerRequest request
    ) {
        CreateSellerResponse response = sellerService.createSeller(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{sellerId}")
    public ResponseEntity<SellerResponse> getSeller(
            @PathVariable("sellerId") Long sellerId
    ) {
        SellerResponse response = sellerService.getSeller(sellerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SellerSummaryResponse>> getSellers() {
        List<SellerSummaryResponse> response = sellerService.getSellers();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{sellerId}/status")
    public ResponseEntity<SellerResponse> updateSellerStatus(
            @PathVariable("sellerId") Long sellerId,
            @Valid @RequestBody UpdateSellerStatusRequest request
    ) {
        SellerResponse response = sellerService.updateSellerStatus(sellerId, request);
        return ResponseEntity.ok(response);
    }
}
