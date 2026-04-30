package io.github.park4ever.ddibs.order.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.park4ever.ddibs.member.domain.QMember;
import io.github.park4ever.ddibs.order.domain.OrderStatus;
import io.github.park4ever.ddibs.order.domain.QOrder;
import io.github.park4ever.ddibs.order.dto.admin.AdminOrderSearchRequest;
import io.github.park4ever.ddibs.order.dto.admin.AdminOrderSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static io.github.park4ever.ddibs.member.domain.QMember.*;
import static io.github.park4ever.ddibs.order.domain.QOrder.*;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminOrderSummaryResponse> searchAdminOrders(
            AdminOrderSearchRequest condition,
            Pageable pageable
    ) {
        List<AdminOrderSummaryResponse> content = queryFactory
                .select(Projections.constructor(
                        AdminOrderSummaryResponse.class,
                        order.id,
                        order.orderCode,
                        member.id,
                        member.email,
                        member.name,
                        order.sellerId,
                        order.productName,
                        order.variantName,
                        order.quantity,
                        order.totalPrice,
                        order.status,
                        order.createdAt
                ))
                .from(order)
                .join(order.member, member)
                .where(
                        orderCodeEq(condition.orderCode()),
                        statusEq(condition.status()),
                        sellerIdEq(condition.sellerId()),
                        memberIdEq(condition.memberId()),
                        memberEmailContains(condition.memberEmailKeyword()),
                        productNameContains(condition.productNameKeyword()),
                        createdAtGoe(condition.from()),
                        createdAtLoe(condition.to())
                )
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(order.count())
                .from(order)
                .join(order.member, member)
                .where(
                        orderCodeEq(condition.orderCode()),
                        statusEq(condition.status()),
                        sellerIdEq(condition.sellerId()),
                        memberIdEq(condition.memberId()),
                        memberEmailContains(condition.memberEmailKeyword()),
                        productNameContains(condition.productNameKeyword()),
                        createdAtGoe(condition.from()),
                        createdAtLoe(condition.to())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    private BooleanExpression orderCodeEq(String orderCode) {
        if (orderCode == null || orderCode.isBlank()) {
            return null;
        }
        return order.orderCode.eq(orderCode);
    }

    private BooleanExpression statusEq(OrderStatus status) {
        if (status == null) {
            return null;
        }
        return order.status.eq(status);
    }

    private BooleanExpression sellerIdEq(Long sellerId) {
        if (sellerId == null) {
            return null;
        }
        return order.sellerId.eq(sellerId);
    }

    private BooleanExpression memberIdEq(Long memberId) {
        if (memberId == null) {
            return null;
        }
        return member.id.eq(memberId);
    }

    private BooleanExpression memberEmailContains(String memberEmailKeyword) {
        if (memberEmailKeyword == null || memberEmailKeyword.isBlank()) {
            return null;
        }
        return member.email.containsIgnoreCase(memberEmailKeyword);
    }

    private BooleanExpression productNameContains(String productNameKeyword) {
        if (productNameKeyword == null || productNameKeyword.isBlank()) {
            return null;
        }
        return order.productName.containsIgnoreCase(productNameKeyword);
    }

    private BooleanExpression createdAtGoe(LocalDateTime from) {
        if (from == null) {
            return null;
        }
        return order.createdAt.goe(from);
    }

    private BooleanExpression createdAtLoe(LocalDateTime to) {
        if (to == null) {
            return null;
        }
        return order.createdAt.loe(to);
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
            case "createdAt" -> new OrderSpecifier<>(direction, order.createdAt);
            case "id" -> new OrderSpecifier<>(direction, order.id);
            case "totalPrice" -> new OrderSpecifier<>(direction, order.totalPrice);
            default -> null;
        };
    }

    private OrderSpecifier<?>[] defaultOrderSpecifiers() {
        return new OrderSpecifier[]{
                new OrderSpecifier<>(com.querydsl.core.types.Order.DESC, order.createdAt),
                new OrderSpecifier<>(com.querydsl.core.types.Order.DESC, order.id)
        };
    }
}
