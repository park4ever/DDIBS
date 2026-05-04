package io.github.park4ever.ddibs.settlement.controller;

import io.github.park4ever.ddibs.settlement.domain.SettlementStatus;
import io.github.park4ever.ddibs.settlement.dto.SettlementResponse;
import io.github.park4ever.ddibs.settlement.dto.SettlementSummaryResponse;
import io.github.park4ever.ddibs.settlement.dto.UpdateSettlementStatusRequest;
import io.github.park4ever.ddibs.settlement.dto.admin.AdminSettlementSearchRequest;
import io.github.park4ever.ddibs.settlement.dto.admin.AdminSettlementSummaryResponse;
import io.github.park4ever.ddibs.settlement.service.AdminSettlementQueryService;
import io.github.park4ever.ddibs.settlement.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/settlements")
public class AdminSettlementController {

    private final SettlementService settlementService;
    private final AdminSettlementQueryService adminSettlementQueryService;

    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponse> getSettlement(
            @PathVariable("settlementId") Long settlementId
    ) {
        SettlementResponse response = settlementService.getSettlement(settlementId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<AdminSettlementSummaryResponse>> getSettlements(
            @ModelAttribute AdminSettlementSearchRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<AdminSettlementSummaryResponse> response =
                adminSettlementQueryService.searchSettlements(request, pageable);

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
