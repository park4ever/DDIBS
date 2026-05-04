package io.github.park4ever.ddibs.settlement.repository;

import io.github.park4ever.ddibs.settlement.dto.admin.AdminSettlementSearchRequest;
import io.github.park4ever.ddibs.settlement.dto.admin.AdminSettlementSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SettlementRepositoryCustom {

    Page<AdminSettlementSummaryResponse> searchAdminSettlements(
            AdminSettlementSearchRequest condition,
            Pageable pageable
    );
}
