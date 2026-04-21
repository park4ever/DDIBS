package io.github.park4ever.ddibs.settlement.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.order.domain.Order;
import io.github.park4ever.ddibs.order.domain.OrderStatus;
import io.github.park4ever.ddibs.order.repository.OrderRepository;
import io.github.park4ever.ddibs.settlement.domain.Settlement;
import io.github.park4ever.ddibs.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementBatchService {

    private static final String SETTLEMENT_CODE_PREFIX = "STL-";
    private static final String SETTLEMENT_CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SETTLEMENT_CODE_LENGTH = 8;
    private static final int MAX_SETTLEMENT_CODE_RETRY_COUNT = 10;

    private final SettlementRepository settlementRepository;
    private final OrderRepository orderRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public int generateSettlements() {
        List<Order> confirmedOrders = orderRepository.findAllByStatusOrderByIdAsc(OrderStatus.CONFIRMED);

        int createdCount = 0;

        for (Order order : confirmedOrders) {
            if (settlementRepository.existsByOrderId(order.getId())) {
                continue;
            }

            createSettlement(order);
            createdCount++;
        }

        return createdCount;
    }

    private void createSettlement(Order order) {
        for (int attempt = 0; attempt < MAX_SETTLEMENT_CODE_RETRY_COUNT; attempt++) {
            String settlementCode = generateSettlementCode();

            if (settlementRepository.existsBySettlementCode(settlementCode)) {
                continue;
            }

            Settlement settlement = Settlement.create(order, settlementCode);

            try {
                settlementRepository.saveAndFlush(settlement);
                return;
            } catch (DataIntegrityViolationException exception) {
                if (settlementRepository.existsByOrderId(order.getId())) {
                    return;
                }

                if (attempt == MAX_SETTLEMENT_CODE_RETRY_COUNT - 1) {
                    throw new BusinessException(ErrorCode.SETTLEMENT_CODE_GENERATION_FAILED);
                }
            }
        }

        throw new BusinessException(ErrorCode.SETTLEMENT_CODE_GENERATION_FAILED);
    }

    private String generateSettlementCode() {
        StringBuilder builder = new StringBuilder(SETTLEMENT_CODE_PREFIX);

        for (int idx = 0; idx < SETTLEMENT_CODE_LENGTH; idx++) {
            int randomIdx = secureRandom.nextInt(SETTLEMENT_CODE_CHARACTERS.length());
            builder.append(SETTLEMENT_CODE_CHARACTERS.charAt(randomIdx));
        }

        return builder.toString();
    }
}
