package io.github.park4ever.ddibs.holdreservation.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.holdreservation.domain.HoldReservation;
import io.github.park4ever.ddibs.holdreservation.repository.HoldReservationRepository;
import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;
import io.github.park4ever.ddibs.launchvariant.repository.LaunchVariantRepository;
import io.github.park4ever.ddibs.order.domain.Order;
import io.github.park4ever.ddibs.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HoldExpirationItemProcessor {

    private final HoldReservationRepository holdReservationRepository;
    private final LaunchVariantRepository launchVariantRepository;
    private final OrderRepository orderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public HoldExpirationProcessingResult expire(Long orderId, LocalDateTime now) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        HoldReservation holdReservation = holdReservationRepository.findByOrderIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HOLD_NOT_FOUND));

        if (!order.isCreated()) {
            return HoldExpirationProcessingResult.ORDER_STATE_SKIPPED;
        }
        if (!holdReservation.isExpiredAt(now)) {
            return HoldExpirationProcessingResult.HOLD_STATE_SKIPPED;
        }

        LaunchVariant launchVariant = launchVariantRepository.findByIdForUpdate(
                order.getLaunchVariant().getId()
        ).orElseThrow(() -> new BusinessException(ErrorCode.LAUNCH_VARIANT_NOT_FOUND));

        holdReservation.expire();
        order.expireHold();
        launchVariant.restoreAvailableStock(order.getQuantity());

        return HoldExpirationProcessingResult.EXPIRED;
    }
}
