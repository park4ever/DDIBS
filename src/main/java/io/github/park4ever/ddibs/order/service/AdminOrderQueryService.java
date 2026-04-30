package io.github.park4ever.ddibs.order.service;

import io.github.park4ever.ddibs.order.dto.admin.AdminOrderSearchRequest;
import io.github.park4ever.ddibs.order.dto.admin.AdminOrderSummaryResponse;
import io.github.park4ever.ddibs.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminOrderQueryService {

    private final OrderRepository orderRepository;

    public Page<AdminOrderSummaryResponse> getOrders(
            AdminOrderSearchRequest condition,
            Pageable pageable
    ) {
        return orderRepository.searchAdminOrders(condition, pageable);
    }
}
