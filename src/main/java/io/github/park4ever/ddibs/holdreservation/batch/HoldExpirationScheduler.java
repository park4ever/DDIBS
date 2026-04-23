package io.github.park4ever.ddibs.holdreservation.batch;

import io.github.park4ever.ddibs.holdreservation.service.HoldExpirationBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HoldExpirationScheduler {

    private final HoldExpirationBatchService holdExpirationBatchService;

    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    public void expireHolds() {
        int expiredCount = holdExpirationBatchService.expireHolds();

        if (expiredCount > 0) {
            log.info("홀드 만료 배치 완료 - 만료 처리 건수: {}", expiredCount);
            return;
        }

        log.debug("홀드 만료 배치 완료 - 만료 대상 없음");
    }
}
