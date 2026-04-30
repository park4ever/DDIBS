package io.github.park4ever.ddibs.order.controller;

import io.github.park4ever.ddibs.order.dto.admin.AdminOrderSearchRequest;
import io.github.park4ever.ddibs.order.dto.admin.AdminOrderSummaryResponse;
import io.github.park4ever.ddibs.order.service.AdminOrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderQueryService adminOrderQueryService;

    @GetMapping
    public ResponseEntity<Page<AdminOrderSummaryResponse>> getOrders(
            @ModelAttribute AdminOrderSearchRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<AdminOrderSummaryResponse> response = adminOrderQueryService.getOrders(request, pageable);

        return ResponseEntity.ok(response);
    }
}
