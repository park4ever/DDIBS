package io.github.park4ever.ddibs.order.repository;

import io.github.park4ever.ddibs.order.domain.Order;
import io.github.park4ever.ddibs.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderCode(String orderCode);

    boolean existsByOrderCode(String orderCode);

    Optional<Order> findByIdAndMemberId(Long orderId, Long memberId);

    List<Order> findAllByMemberIdOrderByIdDesc(Long memberId);

    List<Order> findAllByStatusOrderByIdAsc(OrderStatus status);
}
