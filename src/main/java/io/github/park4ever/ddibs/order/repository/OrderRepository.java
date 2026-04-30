package io.github.park4ever.ddibs.order.repository;

import io.github.park4ever.ddibs.order.domain.Order;
import io.github.park4ever.ddibs.order.domain.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderRepositoryCustom {

    Optional<Order> findByOrderCode(String orderCode);

    boolean existsByOrderCode(String orderCode);

    Optional<Order> findByIdAndMemberId(Long orderId, Long memberId);

    List<Order> findAllByMemberIdOrderByIdDesc(Long memberId);

    List<Order> findAllByStatusOrderByIdAsc(OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o
            from Order o
            where o.id = :orderId
                and o.member.id = :memberId
            """)
    Optional<Order> findByIdAndMemberIdForUpdate(Long orderId, Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select o
            from Order o
            where o.id = :orderId
            """)
    Optional<Order> findByIdForUpdate(Long orderId);

    @Query("""
            select o
            from Order o
            where o.status = :status
                and not exists (
                    select 1
                    from Settlement s
                    where s.order.id = o.id
                )
            order by o.id asc
            """)
    List<Order> findSettlementCandidates(OrderStatus status);
}
