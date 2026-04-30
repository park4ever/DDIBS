package io.github.park4ever.ddibs.launch.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLaunch is a Querydsl query type for Launch
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLaunch extends EntityPathBase<Launch> {

    private static final long serialVersionUID = 490057503L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLaunch launch = new QLaunch("launch");

    public final io.github.park4ever.ddibs.common.entity.QBaseTimeEntity _super = new io.github.park4ever.ddibs.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> endAt = createDateTime("endAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath launchCode = createString("launchCode");

    public final StringPath name = createString("name");

    public final io.github.park4ever.ddibs.product.domain.QProduct product;

    public final DateTimePath<java.time.LocalDateTime> startAt = createDateTime("startAt", java.time.LocalDateTime.class);

    public final EnumPath<LaunchStatus> status = createEnum("status", LaunchStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QLaunch(String variable) {
        this(Launch.class, forVariable(variable), INITS);
    }

    public QLaunch(Path<? extends Launch> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLaunch(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLaunch(PathMetadata metadata, PathInits inits) {
        this(Launch.class, metadata, inits);
    }

    public QLaunch(Class<? extends Launch> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new io.github.park4ever.ddibs.product.domain.QProduct(forProperty("product"), inits.get("product")) : null;
    }

}

