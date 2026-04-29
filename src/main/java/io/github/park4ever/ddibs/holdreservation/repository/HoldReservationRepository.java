package io.github.park4ever.ddibs.holdreservation.repository;

import io.github.park4ever.ddibs.holdreservation.domain.HoldReservation;
import io.github.park4ever.ddibs.holdreservation.domain.HoldStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HoldReservationRepository extends JpaRepository<HoldReservation, Long> {

    Optional<HoldReservation> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select h
            from HoldReservation h 
            where h.order.id = :orderId
            """)
    Optional<HoldReservation> findByOrderIdForUpdate(Long orderId);

    @Query("""
            select h.order.id
            from HoldReservation h
            where h.status = :status
                and h.expiresAt < :expiresAt
            order by h.expiresAt asc
            """)
    List<Long> findExpiredOrderIds(HoldStatus status, LocalDateTime expiresAt);
}
