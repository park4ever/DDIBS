package io.github.park4ever.ddibs.seller.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.seller.domain.Seller;
import io.github.park4ever.ddibs.seller.dto.*;
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
public class SellerService {

    private static final String SELLER_CODE_PREFIX = "SEL-";
    private static final String SELLER_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SELLER_CODE_LENGTH = 8;
    private static final int MAX_SELLER_CODE_RETRY_COUNT = 10;

    private final SellerRepository sellerRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public CreateSellerResponse createSeller(CreateSellerRequest request) {
        for (int attempt = 0; attempt < MAX_SELLER_CODE_RETRY_COUNT; attempt++) {
            String sellerCode = generateSellerCode();

            if (sellerRepository.existsBySellerCode(sellerCode)) {
                continue;
            }

            Seller seller = Seller.create(sellerCode, request.name());

            try {
                Seller savedSeller = sellerRepository.saveAndFlush(seller);
                return CreateSellerResponse.from(savedSeller);
            } catch (DataIntegrityViolationException exception) {
                //seller_code unique 충돌 가능성에 대한 방어
                if (attempt == MAX_SELLER_CODE_RETRY_COUNT - 1) {
                    throw new BusinessException(ErrorCode.SELLER_CODE_GENERATION_FAILED);
                }
            }
        }

        throw new BusinessException(ErrorCode.SELLER_CODE_GENERATION_FAILED);
    }

    public SellerResponse getSeller(Long sellerId) {
        Seller seller = findSellerById(sellerId);
        return SellerResponse.from(seller);
    }

    public List<SellerSummaryResponse> getSellers() {
        return sellerRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(SellerSummaryResponse::from)
                .toList();
    }

    @Transactional
    public SellerResponse updateSellerStatus(Long sellerId, UpdateSellerStatusRequest request) {
        Seller seller = findSellerById(sellerId);

        switch (request.status()) {
            case ACTIVE -> seller.activate();
            case INACTIVE -> seller.inactivate();
            case SUSPENDED -> seller.suspend();
        }

        return SellerResponse.from(seller);
    }

    private Seller findSellerById(Long sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SELLER_NOT_FOUND));
    }

    private String generateSellerCode() {
        StringBuilder builder = new StringBuilder(SELLER_CODE_PREFIX);

        for (int index = 0; index < SELLER_CODE_LENGTH; index++) {
            int randomIndex = secureRandom.nextInt(SELLER_CODE_CHARACTERS.length());
            builder.append(SELLER_CODE_CHARACTERS.charAt(randomIndex));
        }

        return builder.toString();
    }
}
