package io.github.park4ever.ddibs.launch.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.park4ever.ddibs.launch.domain.LaunchStatus;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchDetailResponse;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchSearchRequest;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchSummaryResponse;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchVariantStockResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.github.park4ever.ddibs.launch.domain.QLaunch.*;
import static io.github.park4ever.ddibs.launchvariant.domain.QLaunchVariant.*;
import static io.github.park4ever.ddibs.product.domain.QProduct.*;
import static io.github.park4ever.ddibs.productvariant.domain.QProductVariant.*;
import static io.github.park4ever.ddibs.seller.domain.QSeller.*;

@Repository
@RequiredArgsConstructor
public class LaunchRepositoryImpl implements LaunchRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminLaunchSummaryResponse> searchAdminLaunches(
            AdminLaunchSearchRequest condition,
            Pageable pageable
    ) {
        List<AdminLaunchSummaryResponse> content = queryFactory
                .select(Projections.constructor(
                        AdminLaunchSummaryResponse.class,
                        launch.id,
                        launch.launchCode,
                        launch.name,
                        launch.status,
                        seller.id,
                        seller.name,
                        product.id,
                        product.name,
                        launch.startAt,
                        launch.endAt,
                        launchVariant.id.countDistinct(),
                        launchVariant.totalStock.sumLong().coalesce(0L),
                        launchVariant.availableStock.sumLong().coalesce(0L)
                ))
                .from(launch)
                .join(launch.product, product)
                .join(product.seller, seller)
                .leftJoin(launchVariant).on(launchVariant.launch.eq(launch))
                .where(
                        launchCodeContains(condition.launchCode()),
                        statusEq(condition.status()),
                        sellerIdEq(condition.sellerId()),
                        productNameContains(condition.productNameKeyword()),
                        periodOverlapsFrom(condition.from()),
                        periodOverlapsTo(condition.to())
                )
                .groupBy(
                        launch.id,
                        launch.launchCode,
                        launch.name,
                        launch.status,
                        seller.id,
                        seller.name,
                        product.id,
                        product.name,
                        launch.startAt,
                        launch.endAt
                )
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(launch.countDistinct())
                .from(launch)
                .join(launch.product, product)
                .join(product.seller, seller)
                .where(
                        launchCodeContains(condition.launchCode()),
                        statusEq(condition.status()),
                        sellerIdEq(condition.sellerId()),
                        productNameContains(condition.productNameKeyword()),
                        periodOverlapsFrom(condition.from()),
                        periodOverlapsTo(condition.to())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }

    @Override
    public Optional<AdminLaunchDetailResponse> findAdminLaunchDetail(Long launchId) {
        Tuple detailTuple = queryFactory
                .select(
                        launch.id,
                        launch.launchCode,
                        launch.name,
                        launch.status,
                        seller.id,
                        seller.name,
                        product.id,
                        product.name,
                        launch.startAt,
                        launch.endAt,
                        launch.createdAt,
                        launch.updatedAt
                )
                .from(launch)
                .join(launch.product, product)
                .join(product.seller, seller)
                .where(launch.id.eq(launchId))
                .fetchOne();

        if (detailTuple == null) {
            return Optional.empty();
        }

        List<AdminLaunchVariantStockResponse> variants = queryFactory
                .select(Projections.constructor(
                        AdminLaunchVariantStockResponse.class,
                        launchVariant.id,
                        productVariant.id,
                        productVariant.name,
                        launchVariant.salePrice,
                        launchVariant.totalStock,
                        launchVariant.availableStock
                ))
                .from(launchVariant)
                .join(launchVariant.productVariant, productVariant)
                .where(launchVariant.launch.id.eq(launchId))
                .orderBy(launchVariant.id.asc())
                .fetch();

        AdminLaunchDetailResponse response = new AdminLaunchDetailResponse(
                detailTuple.get(launch.id),
                detailTuple.get(launch.launchCode),
                detailTuple.get(launch.name),
                detailTuple.get(launch.status),
                detailTuple.get(seller.id),
                detailTuple.get(seller.name),
                detailTuple.get(product.id),
                detailTuple.get(product.name),
                detailTuple.get(launch.startAt),
                detailTuple.get(launch.endAt),
                variants,
                detailTuple.get(launch.createdAt),
                detailTuple.get(launch.updatedAt)
        );

        return Optional.of(response);
    }

    private BooleanExpression launchCodeContains(String launchCode) {
        if (launchCode == null) {
            return null;
        }

        String keyword = launchCode.trim();
        if (keyword.isBlank()) {
            return null;
        }

        return launch.launchCode.containsIgnoreCase(keyword);
    }

    private BooleanExpression statusEq(LaunchStatus status) {
        if (status == null) {
            return null;
        }
        return launch.status.eq(status);
    }

    private BooleanExpression sellerIdEq(Long sellerId) {
        if (sellerId == null) {
            return null;
        }
        return seller.id.eq(sellerId);
    }

    private BooleanExpression productNameContains(String productNameKeyword) {
        if (productNameKeyword == null || productNameKeyword.isBlank()) {
            return null;
        }
        return product.name.containsIgnoreCase(productNameKeyword);
    }

    /**
     * 검색 시작 시각이 주어지면, 그 시각 이후까지 살아있는(겹치는) 발매를 찾는다.
     * launch.endAt >= from
     */
    private BooleanExpression periodOverlapsFrom(LocalDateTime from) {
        if (from == null) {
            return null;
        }
        return launch.endAt.goe(from);
    }

    /**
     * 검색 종료 시각이 주어지면, 그 시각 이전에 시작한(겹치는) 발매를 찾는다.
     * launch.startAt <= to
     */
    private BooleanExpression periodOverlapsTo(LocalDateTime to) {
        if (to == null) {
            return null;
        }
        return launch.startAt.loe(to);
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
            case "createdAt" -> new OrderSpecifier<>(direction, launch.createdAt);
            case "startAt" -> new OrderSpecifier<>(direction, launch.startAt);
            case "endAt" -> new OrderSpecifier<>(direction, launch.endAt);
            case "id" -> new OrderSpecifier<>(direction, launch.id);
            default -> null;
        };
    }

    private OrderSpecifier<?>[] defaultOrderSpecifiers() {
        return new OrderSpecifier[]{
                new OrderSpecifier<>(com.querydsl.core.types.Order.DESC, launch.createdAt),
                new OrderSpecifier<>(com.querydsl.core.types.Order.DESC, launch.id)
        };
    }
}
