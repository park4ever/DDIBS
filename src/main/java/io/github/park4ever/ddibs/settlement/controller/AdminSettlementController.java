package io.github.park4ever.ddibs.settlement.controller;

import io.github.park4ever.ddibs.settlement.domain.SettlementStatus;
import io.github.park4ever.ddibs.settlement.dto.SettlementResponse;
import io.github.park4ever.ddibs.settlement.dto.SettlementSummaryResponse;
import io.github.park4ever.ddibs.settlement.dto.UpdateSettlementStatusRequest;
import io.github.park4ever.ddibs.settlement.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/settlements")
public class AdminSettlementController {

    private final SettlementService settlementService;

    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponse> getSettlement(
            @PathVariable("settlementId") Long settlementId
    ) {
        SettlementResponse response = settlementService.getSettlement(settlementId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SettlementSummaryResponse>> getSettlements(
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) SettlementStatus status
    ) {
        List<SettlementSummaryResponse> response;

        if (sellerId != null) {
            response = settlementService.getSettlementsBySeller(sellerId);
        } else if (status != null) {
            response = settlementService.getSettlementsByStatus(status);
        } else {
            response = settlementService.getSettlements();
        }

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{settlementId}/status")
    public ResponseEntity<SettlementResponse> updateSettlementStatus(
            @PathVariable("settlementId") Long settlementId,
            @Valid @RequestBody UpdateSettlementStatusRequest request
    ) {
        SettlementResponse response = settlementService.updateSettlementStatus(settlementId, request);
        return ResponseEntity.ok(response);
    }
}
