package io.github.park4ever.ddibs.settlement.service;

import io.github.park4ever.ddibs.order.domain.OrderStatus;
import io.github.park4ever.ddibs.order.repository.OrderRepository;
import io.github.park4ever.ddibs.settlement.batch.SettlementBatchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementBatchService {

    private final OrderRepository orderRepository;
    private final SettlementItemProcessor settlementItemProcessor;

    public SettlementBatchResult generateSettlements() {
        List<Long> candidateOrderIds = orderRepository.findSettlementCandidateOrderIds(OrderStatus.CONFIRMED);

        int createdCount = 0;
        int raceSkippedCount = 0;

        for (Long orderId : candidateOrderIds) {
            boolean created;

            try {
                created = settlementItemProcessor.create(orderId);
            } catch (RuntimeException exception) {
                log.error(
                        "Settlement item processing failed. orderId={}, message={}",
                        orderId,
                        exception.getMessage(),
                        exception
                );
                throw exception;
            }

            if (created) {
                createdCount++;
                continue;
            }

            raceSkippedCount++;
        }

        return new SettlementBatchResult(
                candidateOrderIds.size(),
                createdCount,
                raceSkippedCount
        );
    }
}