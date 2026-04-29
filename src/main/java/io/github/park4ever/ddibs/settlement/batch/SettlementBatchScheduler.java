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
        SettlementBatchResult result = settlementBatchService.generateSettlements();

        if (result.candidateCount() == 0) {
            log.debug("정산 생성 배치 완료 - 생성 대상 없음");
            return;
        }

        log.info(
                "정산 생성 배치 완료 - 후보: {}, 생성: {}, 경쟁 스킵: {}",
                result.candidateCount(),
                result.createdCount(),
                result.raceSkippedCount()
        );
    }
}
