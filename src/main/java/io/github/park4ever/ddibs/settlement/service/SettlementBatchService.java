package io.github.park4ever.ddibs.settlement.service;

import io.github.park4ever.ddibs.order.domain.OrderStatus;
import io.github.park4ever.ddibs.order.repository.OrderRepository;
import io.github.park4ever.ddibs.settlement.batch.SettlementBatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
            boolean created = settlementItemProcessor.create(orderId);

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