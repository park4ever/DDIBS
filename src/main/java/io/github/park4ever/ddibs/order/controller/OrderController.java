package io.github.park4ever.ddibs.order.controller;

import io.github.park4ever.ddibs.auth.security.MemberPrincipal;
import io.github.park4ever.ddibs.order.dto.CreateOrderRequest;
import io.github.park4ever.ddibs.order.dto.CreateOrderResponse;
import io.github.park4ever.ddibs.order.dto.OrderResponse;
import io.github.park4ever.ddibs.order.dto.OrderSummaryResponse;
import io.github.park4ever.ddibs.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> createOrder(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        CreateOrderResponse response = orderService.createOrder(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getMyOrder(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable("orderId") Long orderId
    ) {
        OrderResponse response = orderService.getMyOrder(principal.getId(), orderId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderSummaryResponse>> getMyOrders(
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        List<OrderSummaryResponse> response = orderService.getMyOrders(principal.getId());
        return ResponseEntity.ok(response);
    }
}
