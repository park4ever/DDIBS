package io.github.park4ever.ddibs.holdreservation.batch;

import io.github.park4ever.ddibs.holdreservation.service.HoldExpirationBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HoldExpirationBatchScheduler {

    private final HoldExpirationBatchService holdExpirationBatchService;

    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    public void expireHolds() {
        HoldExpirationBatchResult result = holdExpirationBatchService.expireHolds();

        if (result.candidateCount() == 0) {
            log.debug("홀드 만료 배치 완료 - 만료 대상 없음");
            return;
        }

        log.info(
                "홀드 만료 배치 완료 - 후보: {}, 만료 처리: {}, 주문 상태 스킵: {}, 홀드 상태 스킵: {}",
                result.candidateCount(),
                result.expiredCount(),
                result.orderStateSkippedCount(),
                result.holdStateSkippedCount()
        );
    }
}
