package io.github.park4ever.ddibs.settlement.batch;

import io.github.park4ever.ddibs.settlement.service.SettlementBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementBatchScheduler {

    private final SettlementBatchService settlementBatchService;

    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Seoul")
    public void generateSettlements() {
        int createdCount = settlementBatchService.generateSettlements();

        if (createdCount > 0) {
            log.info("정산 생성 배치 완료 - 생성 건수: {}", createdCount);
            return;
        }

        log.debug("정산 생성 배치 완료 - 생성 대상 없음");
    }
}
