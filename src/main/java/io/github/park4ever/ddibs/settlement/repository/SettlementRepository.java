package io.github.park4ever.ddibs.settlement.repository;

import io.github.park4ever.ddibs.settlement.domain.Settlement;
import io.github.park4ever.ddibs.settlement.domain.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findBySettlementCode(String settlementCode);

    boolean existsBySettlementCode(String settlementCode);

    Optional<Settlement> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    List<Settlement> findAllBySellerIdOrderByIdDesc(Long sellerId);

    List<Settlement> findAllByStatusOrderByIdDesc(SettlementStatus status);
}
