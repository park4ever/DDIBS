package io.github.park4ever.ddibs.product.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.product.domain.Product;
import io.github.park4ever.ddibs.product.dto.*;
import io.github.park4ever.ddibs.product.repository.ProductRepository;
import io.github.park4ever.ddibs.seller.domain.Seller;
import io.github.park4ever.ddibs.seller.repository.SellerRepository;
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
public class ProductService {

    private static final String PRODUCT_CODE_PREFIX = "PRD-";
    private static final String PRODUCT_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int PRODUCT_CODE_LENGTH = 8;
    private static final int MAX_PRODUCT_CODE_RETRY_COUNT = 10;

    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public CreateProductResponse createProduct(CreateProductRequest request) {
        Seller seller = findSellerById(request.sellerId());

        for (int attempt = 0; attempt < MAX_PRODUCT_CODE_RETRY_COUNT; attempt++) {
            String productCode = generateProductCode();

            if (productRepository.existsByProductCode(productCode)) {
                continue;
            }

            Product product = Product.create(seller, productCode, request.name());

            try {
                Product savedProduct = productRepository.saveAndFlush(product);
                return CreateProductResponse.from(savedProduct);
            } catch (DataIntegrityViolationException exception) {
                if (attempt == MAX_PRODUCT_CODE_RETRY_COUNT - 1) {
                    throw new BusinessException(ErrorCode.PRODUCT_CODE_GENERATION_FAILED);
                }
            }
        }

        throw new BusinessException(ErrorCode.PRODUCT_CODE_GENERATION_FAILED);
    }

    public ProductResponse getProduct(Long productId) {
        Product product = findProductById(productId);
        return ProductResponse.from(product);
    }

    public List<ProductSummaryResponse> getProducts() {
        return productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(ProductSummaryResponse::from)
                .toList();
    }

    @Transactional
    public ProductResponse updateProductStatus(Long productId, UpdateProductStatusRequest request) {
        Product product = findProductById(productId);

        switch (request.status()) {
            case ACTIVE -> product.activate();
            case INACTIVE -> product.inactivate();
        }

        return ProductResponse.from(product);
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Seller findSellerById(Long sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_NOT_FOUND));
    }

    private String generateProductCode() {
        StringBuilder builder = new StringBuilder(PRODUCT_CODE_PREFIX);

        for (int idx = 0; idx < PRODUCT_CODE_LENGTH; idx++) {
            int randomIdx = secureRandom.nextInt(PRODUCT_CODE_CHARACTERS.length());
            builder.append(PRODUCT_CODE_CHARACTERS.charAt(randomIdx));
        }

        return builder.toString();
    }
}
