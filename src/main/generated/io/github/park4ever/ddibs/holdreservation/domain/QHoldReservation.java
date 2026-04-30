package io.github.park4ever.ddibs.holdreservation.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QHoldReservation is a Querydsl query type for HoldReservation
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHoldReservation extends EntityPathBase<HoldReservation> {

    private static final long serialVersionUID = 767143489L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QHoldReservation holdReservation = new QHoldReservation("holdReservation");

    public final io.github.park4ever.ddibs.common.entity.QBaseTimeEntity _super = new io.github.park4ever.ddibs.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final io.github.park4ever.ddibs.order.domain.QOrder order;

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final EnumPath<HoldStatus> status = createEnum("status", HoldStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QHoldReservation(String variable) {
        this(HoldReservation.class, forVariable(variable), INITS);
    }

    public QHoldReservation(Path<? extends HoldReservation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QHoldReservation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QHoldReservation(PathMetadata metadata, PathInits inits) {
        this(HoldReservation.class, metadata, inits);
    }

    public QHoldReservation(Class<? extends HoldReservation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.order = inits.isInitialized("order") ? new io.github.park4ever.ddibs.order.domain.QOrder(forProperty("order"), inits.get("order")) : null;
    }

}

