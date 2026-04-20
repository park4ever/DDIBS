package io.github.park4ever.ddibs.payment.controller;

import io.github.park4ever.ddibs.auth.security.MemberPrincipal;
import io.github.park4ever.ddibs.payment.dto.PaymentResponse;
import io.github.park4ever.ddibs.payment.dto.RequestPaymentRequest;
import io.github.park4ever.ddibs.payment.dto.RequestPaymentResponse;
import io.github.park4ever.ddibs.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<RequestPaymentResponse> requestPayment(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @RequestBody RequestPaymentRequest request
    ) {
        RequestPaymentResponse response = paymentService.requestPayment(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentResponse> getMyPayment(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable("orderId") Long orderId
    ) {
        PaymentResponse response = paymentService.getMyPayment(principal.getId(), orderId);
        return ResponseEntity.ok(response);
    }
}
