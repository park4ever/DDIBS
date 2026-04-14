package io.github.park4ever.ddibs.launchvariant.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.launch.domain.Launch;
import io.github.park4ever.ddibs.launch.domain.LaunchStatus;
import io.github.park4ever.ddibs.launch.repository.LaunchRepository;
import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;
import io.github.park4ever.ddibs.launchvariant.dto.CreateLaunchVariantRequest;
import io.github.park4ever.ddibs.launchvariant.dto.CreateLaunchVariantResponse;
import io.github.park4ever.ddibs.launchvariant.dto.LaunchVariantResponse;
import io.github.park4ever.ddibs.launchvariant.dto.LaunchVariantSummaryResponse;
import io.github.park4ever.ddibs.launchvariant.repository.LaunchVariantRepository;
import io.github.park4ever.ddibs.productvariant.domain.ProductVariant;
import io.github.park4ever.ddibs.productvariant.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LaunchVariantService {

    private final LaunchVariantRepository launchVariantRepository;
    private final LaunchRepository launchRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public CreateLaunchVariantResponse createLaunchVariant(CreateLaunchVariantRequest request) {
        Launch launch = findLaunchById(request.launchId());
        ProductVariant productVariant = findProductVariantById(request.productVariantId());

        validateLaunchCanRegisterVariant(launch);
        validateProductVariantIsActive(productVariant);
        validateDuplicateLaunchVariant(launch.getId(), productVariant.getId());

        LaunchVariant launchVariant = LaunchVariant.create(launch, productVariant, request.salePrice(), request.totalStock());

        try {
            LaunchVariant savedLaunchVariant = launchVariantRepository.saveAndFlush(launchVariant);
            return CreateLaunchVariantResponse.from(savedLaunchVariant);
        } catch (DataIntegrityViolationException exception) {
            if (launchVariantRepository.existsByLaunchIdAndProductVariantId(launch.getId(), productVariant.getId())) {
                throw new BusinessException(ErrorCode.DUPLICATE_LAUNCH_VARIANT);
            }

            throw exception;
        }
    }

    public LaunchVariantResponse getLaunchVariant(Long launchVariantId) {
        LaunchVariant launchVariant = findLaunchVariantById(launchVariantId);
        return LaunchVariantResponse.from(launchVariant);
    }

    public List<LaunchVariantSummaryResponse> getLaunchVariants() {
        return launchVariantRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(LaunchVariantSummaryResponse::from)
                .toList();
    }

    private Launch findLaunchById(Long launchId) {
        return launchRepository.findById(launchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LAUNCH_NOT_FOUND));
    }

    private ProductVariant findProductVariantById(Long productVariantId) {
        return productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
    }

    private LaunchVariant findLaunchVariantById(Long launchVariantId) {
        return launchVariantRepository.findById(launchVariantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LAUNCH_VARIANT_NOT_FOUND));
    }

    private void validateLaunchCanRegisterVariant(Launch launch) {
        if (launch.getStatus() != LaunchStatus.UPCOMING) {
            throw new BusinessException(ErrorCode.LAUNCH_VARIANT_REGISTRATION_NOT_ALLOWED);
        }
    }

    private void validateProductVariantIsActive(ProductVariant productVariant) {
        if (!productVariant.isActive()) {
            throw new BusinessException(ErrorCode.PRODUCT_VARIANT_INACTIVE);
        }
    }

    private void validateDuplicateLaunchVariant(Long launchId, Long productVariantId) {
        if (launchVariantRepository.existsByLaunchIdAndProductVariantId(launchId, productVariantId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_LAUNCH_VARIANT);
        }
    }
}
