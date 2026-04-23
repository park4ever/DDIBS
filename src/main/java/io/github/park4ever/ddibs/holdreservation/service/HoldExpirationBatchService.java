package io.github.park4ever.ddibs.holdreservation.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.holdreservation.domain.HoldReservation;
import io.github.park4ever.ddibs.holdreservation.domain.HoldStatus;
import io.github.park4ever.ddibs.holdreservation.repository.HoldReservationRepository;
import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;
import io.github.park4ever.ddibs.launchvariant.repository.LaunchVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HoldExpirationBatchService {

    private final HoldReservationRepository holdReservationRepository;
    private final LaunchVariantRepository launchVariantRepository;

    @Transactional
    public int expireHolds() {
        LocalDateTime now = LocalDateTime.now();

        List<HoldReservation> expiredTargets =
                holdReservationRepository.findAllByStatusAndExpiresAtBeforeOrderByExpiresAtAsc(
                HoldStatus.ACTIVE,
                now
        );

        int expiredCount = 0;

        for (HoldReservation holdReservation : expiredTargets) {
            if (!holdReservation.isExpiredAt(now)) {
                continue;
            }

            LaunchVariant launchVariant = launchVariantRepository.findByIdForUpdate(
                    holdReservation.getOrder().getLaunchVariant().getId()
            ).orElseThrow(() -> new BusinessException(ErrorCode.LAUNCH_VARIANT_NOT_FOUND));

            holdReservation.expire();

            if (holdReservation.getOrder().isCreated()) {
                holdReservation.getOrder().expireHold();
            }

            launchVariant.restoreAvailableStock(holdReservation.getQuantity());
            expiredCount++;
        }

        return expiredCount;
    }
}
