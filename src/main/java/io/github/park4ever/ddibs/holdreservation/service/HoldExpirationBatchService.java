package io.github.park4ever.ddibs.holdreservation.service;

import io.github.park4ever.ddibs.holdreservation.batch.HoldExpirationBatchResult;
import io.github.park4ever.ddibs.holdreservation.domain.HoldStatus;
import io.github.park4ever.ddibs.holdreservation.repository.HoldReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HoldExpirationBatchService {

    private final HoldReservationRepository holdReservationRepository;
    private final HoldExpirationItemProcessor holdExpirationItemProcessor;
    private final Clock clock;

    public HoldExpirationBatchResult expireHolds() {
        LocalDateTime now = LocalDateTime.now(clock);

        List<Long> expiredOrderIds = holdReservationRepository.findExpiredOrderIds(HoldStatus.ACTIVE, now);

        int expiredCount = 0;
        int orderStateSkipped = 0;
        int holdStateSkipped = 0;

        for (Long orderId : expiredOrderIds) {
            HoldExpirationProcessingResult result;

            try {
                result = holdExpirationItemProcessor.expire(orderId, now);
            } catch (RuntimeException exception) {
                log.error(
                        "Hold expiration item processing failed. orderId={}, 기준시각={}, message={}",
                        orderId,
                        now,
                        exception.getMessage(),
                        exception
                );
                throw exception;
            }

            switch (result) {
                case EXPIRED -> expiredCount++;
                case ORDER_STATE_SKIPPED -> orderStateSkipped++;
                case HOLD_STATE_SKIPPED -> holdStateSkipped++;
            }
        }

        return new HoldExpirationBatchResult(
                expiredOrderIds.size(),
                expiredCount,
                orderStateSkipped,
                holdStateSkipped
        );
    }
}
