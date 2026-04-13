package io.github.park4ever.ddibs.productvariant.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.product.domain.Product;
import io.github.park4ever.ddibs.product.repository.ProductRepository;
import io.github.park4ever.ddibs.productvariant.domain.ProductVariant;
import io.github.park4ever.ddibs.productvariant.dto.*;
import io.github.park4ever.ddibs.productvariant.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductVariantService {

    private static final String PRODUCT_VARIANT_CODE_PREFIX = "VRT-";
    private static final String PRODUCT_VARIANT_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int PRODUCT_VARIANT_CODE_LENGTH = 8;
    private static final int MAX_PRODUCT_VARIANT_CODE_RETRY_COUNT = 10;

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public CreateProductVariantResponse createProductVariant(CreateProductVariantRequest request) {
        Product product = findProductById(request.productId());
        validateProductIsActive(product);
        validateDuplicateVariantName(product.getId(), request.name());

        for (int attempt = 0; attempt < MAX_PRODUCT_VARIANT_CODE_RETRY_COUNT; attempt++) {
            String variantCode = generateProductVariantCode();

            if (productVariantRepository.existsByVariantCode(variantCode)) {
                continue;
            }

            ProductVariant productVariant = ProductVariant.create(product, variantCode, request.name());

            try {
                ProductVariant savedProductVariant = productVariantRepository.saveAndFlush(productVariant);
                return CreateProductVariantResponse.from(savedProductVariant);
            } catch (DataIntegrityViolationException exception) {
                if (productVariantRepository.existsByProductIdAndName(product.getId(), request.name())) {
                    throw new BusinessException(ErrorCode.DUPLICATE_PRODUCT_VARIANT_NAME);
                }

                if (attempt == MAX_PRODUCT_VARIANT_CODE_RETRY_COUNT - 1) {
                    throw new BusinessException(ErrorCode.PRODUCT_VARIANT_CODE_GENERATION_FAILED);
                }
            }
        }

        throw new BusinessException(ErrorCode.PRODUCT_VARIANT_CODE_GENERATION_FAILED);
    }

    public ProductVariantResponse getProductVariant(Long productVariantId) {
        ProductVariant productVariant = findProductVariantById(productVariantId);
        return ProductVariantResponse.from(productVariant);
    }

    public List<ProductVariantSummaryResponse> getProductVariants() {
        return productVariantRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(ProductVariantSummaryResponse::from)
                .toList();
    }

    @Transactional
    public ProductVariantResponse updateProductVariantStatus(
            Long productVariantId,
            UpdateProductVariantStatusRequest request
    ) {
        ProductVariant productVariant = findProductVariantById(productVariantId);

        switch (request.status()) {
            case ACTIVE -> productVariant.activate();
            case INACTIVE -> productVariant.inactivate();
        }

        return ProductVariantResponse.from(productVariant);
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private ProductVariant findProductVariantById(Long productVariantId) {
        return productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
    }

    private void validateProductIsActive(Product product) {
        if (!product.isActive()) {
            throw new BusinessException(ErrorCode.PRODUCT_INACTIVE);
        }
    }

    private void validateDuplicateVariantName(Long productId, String name) {
        if (productVariantRepository.existsByProductIdAndName(productId, name)) {
            throw new BusinessException(ErrorCode.DUPLICATE_PRODUCT_VARIANT_NAME);
        }
    }

    private String generateProductVariantCode() {
        StringBuilder builder = new StringBuilder(PRODUCT_VARIANT_CODE_PREFIX);

        for (int idx = 0; idx < PRODUCT_VARIANT_CODE_LENGTH; idx++) {
            int randomIndex = secureRandom.nextInt(PRODUCT_VARIANT_CODE_CHARACTERS.length());
            builder.append(PRODUCT_VARIANT_CODE_CHARACTERS.charAt(randomIndex));
        }

        return builder.toString();
    }
}
