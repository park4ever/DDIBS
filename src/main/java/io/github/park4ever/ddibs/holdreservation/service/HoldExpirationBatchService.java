package io.github.park4ever.ddibs.holdreservation.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.holdreservation.domain.HoldReservation;
import io.github.park4ever.ddibs.holdreservation.domain.HoldStatus;
import io.github.park4ever.ddibs.holdreservation.repository.HoldReservationRepository;
import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;
import io.github.park4ever.ddibs.launchvariant.repository.LaunchVariantRepository;
import io.github.park4ever.ddibs.order.domain.Order;
import io.github.park4ever.ddibs.order.repository.OrderRepository;
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
    private final OrderRepository orderRepository;

    @Transactional
    public int expireHolds() {
        LocalDateTime now = LocalDateTime.now();

        List<Long> expiredOrderIds = holdReservationRepository.findExpiredOrderIds(HoldStatus.ACTIVE, now);

        int expiredCount = 0;

        for (Long orderId : expiredOrderIds) {
            Order order = orderRepository.findByIdForUpdate(orderId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

            HoldReservation holdReservation = holdReservationRepository.findByOrderIdForUpdate(orderId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.HOLD_NOT_FOUND));

            if (!order.isCreated()) {
                continue;
            }

            if (!holdReservation.isExpiredAt(now)) {
                continue;
            }

            LaunchVariant launchVariant = launchVariantRepository.findByIdForUpdate(
                    order.getLaunchVariant().getId()
            ).orElseThrow(() -> new BusinessException(ErrorCode.LAUNCH_VARIANT_NOT_FOUND));

            holdReservation.expire();
            order.expireHold();
            launchVariant.restoreAvailableStock(order.getQuantity());

            expiredCount++;
        }

        return expiredCount;
    }
}
