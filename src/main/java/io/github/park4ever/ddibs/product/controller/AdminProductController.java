package io.github.park4ever.ddibs.product.controller;

import io.github.park4ever.ddibs.product.dto.*;
import io.github.park4ever.ddibs.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<CreateProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request
    ) {
        CreateProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable("productId") Long productId
    ) {
        ProductResponse response = productService.getProduct(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProductSummaryResponse>> getProducts() {
        List<ProductSummaryResponse> response = productService.getProducts();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/status")
    public ResponseEntity<ProductResponse> updateProductStatus(
            @PathVariable("productId") Long productId,
            @Valid @RequestBody UpdateProductStatusRequest request
    ) {
        ProductResponse response = productService.updateProductStatus(productId, request);
        return ResponseEntity.ok(response);
    }
}
