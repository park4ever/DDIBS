package io.github.park4ever.ddibs.settlement.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.settlement.domain.Settlement;
import io.github.park4ever.ddibs.settlement.domain.SettlementStatus;
import io.github.park4ever.ddibs.settlement.dto.SettlementResponse;
import io.github.park4ever.ddibs.settlement.dto.SettlementSummaryResponse;
import io.github.park4ever.ddibs.settlement.dto.UpdateSettlementStatusRequest;
import io.github.park4ever.ddibs.settlement.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementService {

    private final SettlementRepository settlementRepository;

    public SettlementResponse getSettlement(Long settlementId) {
        Settlement settlement = findSettlementById(settlementId);
        return SettlementResponse.from(settlement);
    }

    public List<SettlementSummaryResponse> getSettlements() {
        return settlementRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(SettlementSummaryResponse::from)
                .toList();
    }

    public List<SettlementSummaryResponse> getSettlementsBySeller(Long sellerId) {
        return settlementRepository.findAllBySellerIdOrderByIdDesc(sellerId)
                .stream()
                .map(SettlementSummaryResponse::from)
                .toList();
    }

    public List<SettlementSummaryResponse> getSettlementsByStatus(SettlementStatus status) {
        return settlementRepository.findAllByStatusOrderByIdDesc(status)
                .stream()
                .map(SettlementSummaryResponse::from)
                .toList();
    }

    @Transactional
    public SettlementResponse updateSettlementStatus(
            Long settlementId,
            UpdateSettlementStatusRequest request
    ) {
        Settlement settlement = findSettlementById(settlementId);

        switch (request.status()) {
            case CONFIRMED -> settlement.confirm(LocalDateTime.now());
            case CREATED -> throw new BusinessException(ErrorCode.INVALID_SETTLEMENT_STATUS_TRANSITION);
        }

        return SettlementResponse.from(settlement);
    }

    private Settlement findSettlementById(Long settlementId) {
        return settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SETTLEMENT_NOT_FOUND));
    }
}
