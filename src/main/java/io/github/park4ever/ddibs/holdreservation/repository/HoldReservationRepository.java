package io.github.park4ever.ddibs.holdreservation.repository;

import io.github.park4ever.ddibs.holdreservation.domain.HoldReservation;
import io.github.park4ever.ddibs.holdreservation.domain.HoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface HoldReservationRepository extends JpaRepository<HoldReservation, Long> {

    Optional<HoldReservation> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    List<HoldReservation> findAllByStatusAndExpiresAtBeforeOrderByExpiresAtAsc(
            HoldStatus status,
            LocalDateTime expiresAt
    );
}
