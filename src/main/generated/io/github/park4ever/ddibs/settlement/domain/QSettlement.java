package io.github.park4ever.ddibs.settlement.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSettlement is a Querydsl query type for Settlement
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSettlement extends EntityPathBase<Settlement> {

    private static final long serialVersionUID = -652493813L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSettlement settlement = new QSettlement("settlement");

    public final io.github.park4ever.ddibs.common.entity.QBaseTimeEntity _super = new io.github.park4ever.ddibs.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final io.github.park4ever.ddibs.order.domain.QOrder order;

    public final NumberPath<Long> sellerId = createNumber("sellerId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> settledAt = createDateTime("settledAt", java.time.LocalDateTime.class);

    public final NumberPath<java.math.BigDecimal> settlementAmount = createNumber("settlementAmount", java.math.BigDecimal.class);

    public final StringPath settlementCode = createString("settlementCode");

    public final EnumPath<SettlementStatus> status = createEnum("status", SettlementStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSettlement(String variable) {
        this(Settlement.class, forVariable(variable), INITS);
    }

    public QSettlement(Path<? extends Settlement> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSettlement(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSettlement(PathMetadata metadata, PathInits inits) {
        this(Settlement.class, metadata, inits);
    }

    public QSettlement(Class<? extends Settlement> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.order = inits.isInitialized("order") ? new io.github.park4ever.ddibs.order.domain.QOrder(forProperty("order"), inits.get("order")) : null;
    }

}

