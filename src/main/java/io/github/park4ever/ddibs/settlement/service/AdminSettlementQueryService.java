package io.github.park4ever.ddibs.settlement.service;

import io.github.park4ever.ddibs.settlement.dto.admin.AdminSettlementSearchRequest;
import io.github.park4ever.ddibs.settlement.dto.admin.AdminSettlementSummaryResponse;
import io.github.park4ever.ddibs.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminSettlementQueryService {

    private final SettlementRepository settlementRepository;

    public Page<AdminSettlementSummaryResponse> searchSettlements(
            AdminSettlementSearchRequest condition,
            Pageable pageable
    ) {
        return settlementRepository.searchAdminSettlements(condition, pageable);
    }
}
