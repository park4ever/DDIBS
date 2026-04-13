package io.github.park4ever.ddibs.productvariant.controller;

import io.github.park4ever.ddibs.productvariant.dto.*;
import io.github.park4ever.ddibs.productvariant.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/product-variants")
public class AdminProductVariantController {

    private final ProductVariantService productVariantService;

    @PostMapping
    public ResponseEntity<CreateProductVariantResponse> createProductVariant(
            @Valid @RequestBody CreateProductVariantRequest request
    ) {
        CreateProductVariantResponse response = productVariantService.createProductVariant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{productVariantId}")
    public ResponseEntity<ProductVariantResponse> getProductVariant(
            @PathVariable("productVariantId") Long productVariantId
    ) {
        ProductVariantResponse response = productVariantService.getProductVariant(productVariantId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductVariantSummaryResponse>> getProductVariants() {
        List<ProductVariantSummaryResponse> response = productVariantService.getProductVariants();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productVariantId}/status")
    public ResponseEntity<ProductVariantResponse> updateProductVariantStatus(
            @PathVariable("productVariantId") Long productVariantId,
            @Valid @RequestBody UpdateProductVariantStatusRequest request
    ) {
        ProductVariantResponse response = productVariantService.updateProductVariantStatus(productVariantId, request);
        return ResponseEntity.ok(response);
    }
}
