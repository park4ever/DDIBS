package io.github.park4ever.ddibs.order.repository;

import io.github.park4ever.ddibs.order.dto.admin.AdminOrderSearchRequest;
import io.github.park4ever.ddibs.order.dto.admin.AdminOrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryCustom {

    Page<AdminOrderSummaryResponse> searchAdminOrders(
            AdminOrderSearchRequest condition,
            Pageable pageable
    );
}
