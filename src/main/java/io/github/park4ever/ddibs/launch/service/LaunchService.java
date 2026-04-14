package io.github.park4ever.ddibs.launch.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.launch.domain.Launch;
import io.github.park4ever.ddibs.launch.dto.*;
import io.github.park4ever.ddibs.launch.repository.LaunchRepository;
import io.github.park4ever.ddibs.product.domain.Product;
import io.github.park4ever.ddibs.product.repository.ProductRepository;
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
public class LaunchService {

    private static final String LAUNCH_CODE_PREFIX = "LCH-";
    private static final String LAUNCH_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int LAUNCH_CODE_LENGTH = 8;
    private static final int MAX_LAUNCH_CODE_RETRY_COUNT = 10;

    private final LaunchRepository launchRepository;
    private final ProductRepository productRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public CreateLaunchResponse createLaunch(CreateLaunchRequest request) {
        Product product = findProductById(request.productId());
        validateProductIsActive(product);

        for (int attempt = 0; attempt < MAX_LAUNCH_CODE_RETRY_COUNT; attempt++) {
            String launchCode = generateLaunchCode();

            Launch launch = Launch.create(product, launchCode, request.name(), request.startAt(), request.endAt());

            try {
                Launch savedLaunch = launchRepository.saveAndFlush(launch);
                return CreateLaunchResponse.from(savedLaunch);
            } catch (DataIntegrityViolationException exception) {
                if (attempt == MAX_LAUNCH_CODE_RETRY_COUNT - 1) {
                    throw new BusinessException(ErrorCode.LAUNCH_CODE_GENERATION_FAILED);
                }
            }
        }

        throw new BusinessException(ErrorCode.LAUNCH_CODE_GENERATION_FAILED);
    }

    public LaunchResponse getLaunch(Long launchId) {
        Launch launch = findLaunchById(launchId);
        return LaunchResponse.from(launch);
    }

    public List<LaunchSummaryResponse> getLaunches() {
        return launchRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(LaunchSummaryResponse::from)
                .toList();
    }

    @Transactional
    public LaunchResponse updateLaunchStatus(Long launchId, UpdateLaunchStatusRequest request) {
        Launch launch = findLaunchById(launchId);

        switch (request.status()) {
            case OPEN -> launch.open();
            case CLOSED -> launch.close();
            case ENDED -> launch.end();
            case CANCELLED -> launch.cancel();
            case UPCOMING -> throw new BusinessException(ErrorCode.INVALID_LAUNCH_STATUS_TRANSITION);
        }

        return LaunchResponse.from(launch);
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Launch findLaunchById(Long launchId) {
        return launchRepository.findById(launchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LAUNCH_NOT_FOUND));
    }

    private void validateProductIsActive(Product product) {
        if (!product.isActive()) {
            throw new BusinessException(ErrorCode.PRODUCT_INACTIVE);
        }
    }

    private String generateLaunchCode() {
        StringBuilder builder = new StringBuilder(LAUNCH_CODE_PREFIX);

        for (int idx = 0; idx < LAUNCH_CODE_LENGTH; idx++) {
            int randomidx = secureRandom.nextInt(LAUNCH_CODE_CHARACTERS.length());
            builder.append(LAUNCH_CODE_CHARACTERS.charAt(randomidx));
        }

        return builder.toString();
    }
}
