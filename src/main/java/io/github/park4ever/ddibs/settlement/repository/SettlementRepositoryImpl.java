package io.github.park4ever.ddibs.settlement.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.park4ever.ddibs.order.domain.QOrder;
import io.github.park4ever.ddibs.settlement.domain.QSettlement;
import io.github.park4ever.ddibs.settlement.domain.SettlementStatus;
import io.github.park4ever.ddibs.settlement.dto.admin.AdminSettlementSearchRequest;
import io.github.park4ever.ddibs.settlement.dto.admin.AdminSettlementSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.github.park4ever.ddibs.order.domain.QOrder.*;
import static io.github.park4ever.ddibs.settlement.domain.QSettlement.*;

@Repository
@RequiredArgsConstructor
public class SettlementRepositoryImpl implements SettlementRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminSettlementSummaryResponse> searchAdminSettlements(
            AdminSettlementSearchRequest condition,
            Pageable pageable
    ) {
        List<AdminSettlementSummaryResponse> content = queryFactory
                .select(Projections.constructor(
                        AdminSettlementSummaryResponse.class,
                        settlement.id,
                        order.id,
                        order.orderCode,
                        settlement.sellerId,
                        settlement.settlementCode,
                        settlement.settlementAmount,
                        settlement.status,
                        settlement.settledAt,
                        settlement.createdAt
                ))
                .from(settlement)
                .join(settlement.order, order)
                .where(
                        settlementCodeEq(condition.settlementCode()),
                        orderCodeEq(condition.orderCode()),
                        sellerIdEq(condition.sellerId()),
                        statusEq(condition.status()),
                        createdAtGoe(condition.from()),
                        createdAtLoe(condition.to())
                )
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(settlement.count())
                .from(settlement)
                .join(settlement.order, order)
                .where(
                        settlementCodeEq(condition.settlementCode()),
                        orderCodeEq(condition.orderCode()),
                        sellerIdEq(condition.sellerId()),
                        statusEq(condition.status()),
                        createdAtGoe(condition.from()),
                        createdAtLoe(condition.to())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    private BooleanExpression settlementCodeEq(String settlementCode) {
        if (settlementCode == null || settlementCode.isBlank()) {
            return null;
        }
        return settlement.settlementCode.eq(settlementCode);
    }

    private BooleanExpression orderCodeEq(String orderCode) {
        if (orderCode == null || orderCode.isBlank()) {
            return null;
        }
        return order.orderCode.eq(orderCode);
    }

    private BooleanExpression sellerIdEq(Long sellerId) {
        if (sellerId == null) {
            return null;
        }
        return settlement.sellerId.eq(sellerId);
    }

    private BooleanExpression statusEq(SettlementStatus status) {
        if (status == null) {
            return null;
        }
        return settlement.status.eq(status);
    }

    private BooleanExpression createdAtGoe(LocalDateTime from) {
        if (from == null) {
            return null;
        }
        return settlement.createdAt.goe(from);
    }

    private BooleanExpression createdAtLoe(LocalDateTime to) {
        if (to == null) {
            return null;
        }
        return settlement.createdAt.loe(to);
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        if (sort.isUnsorted()) {
            return defaultOrderSpecifiers();
        }

        for (Sort.Order sortOrder : sort) {
            OrderSpecifier<?> orderSpecifier = createOrderSpecifier(sortOrder);
            if (orderSpecifier != null) {
                orderSpecifiers.add(orderSpecifier);
            }
        }

        if (orderSpecifiers.isEmpty()) {
            return defaultOrderSpecifiers();
        }

        return orderSpecifiers.toArray(OrderSpecifier[]::new);
    }

    private OrderSpecifier<?> createOrderSpecifier(Sort.Order sortOrder) {
        com.querydsl.core.types.Order direction =
                sortOrder.isAscending()
                        ? com.querydsl.core.types.Order.ASC
                        : com.querydsl.core.types.Order.DESC;

        return switch (sortOrder.getProperty()) {
            case "createdAt" -> new OrderSpecifier<>(direction, settlement.createdAt);
            case "settledAt" -> new OrderSpecifier<>(direction, settlement.settledAt);
            case "settlementAmount" -> new OrderSpecifier<>(direction, settlement.settlementAmount);
            case "id" -> new OrderSpecifier<>(direction, settlement.id);
            default -> null;
        };
    }

    private OrderSpecifier<?>[] defaultOrderSpecifiers() {
        return new OrderSpecifier[]{
                new OrderSpecifier<>(com.querydsl.core.types.Order.DESC, settlement.createdAt),
                new OrderSpecifier<>(com.querydsl.core.types.Order.DESC, settlement.id)
        };
    }
}
