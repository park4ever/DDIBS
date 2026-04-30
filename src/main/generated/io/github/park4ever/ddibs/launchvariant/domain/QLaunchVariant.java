package io.github.park4ever.ddibs.launchvariant.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLaunchVariant is a Querydsl query type for LaunchVariant
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLaunchVariant extends EntityPathBase<LaunchVariant> {

    private static final long serialVersionUID = 1169034145L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLaunchVariant launchVariant = new QLaunchVariant("launchVariant");

    public final io.github.park4ever.ddibs.common.entity.QBaseTimeEntity _super = new io.github.park4ever.ddibs.common.entity.QBaseTimeEntity(this);

    public final NumberPath<Integer> availableStock = createNumber("availableStock", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final io.github.park4ever.ddibs.launch.domain.QLaunch launch;

    public final io.github.park4ever.ddibs.productvariant.domain.QProductVariant productVariant;

    public final NumberPath<java.math.BigDecimal> salePrice = createNumber("salePrice", java.math.BigDecimal.class);

    public final NumberPath<Integer> totalStock = createNumber("totalStock", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QLaunchVariant(String variable) {
        this(LaunchVariant.class, forVariable(variable), INITS);
    }

    public QLaunchVariant(Path<? extends LaunchVariant> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLaunchVariant(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLaunchVariant(PathMetadata metadata, PathInits inits) {
        this(LaunchVariant.class, metadata, inits);
    }

    public QLaunchVariant(Class<? extends LaunchVariant> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.launch = inits.isInitialized("launch") ? new io.github.park4ever.ddibs.launch.domain.QLaunch(forProperty("launch"), inits.get("launch")) : null;
        this.productVariant = inits.isInitialized("productVariant") ? new io.github.park4ever.ddibs.productvariant.domain.QProductVariant(forProperty("productVariant"), inits.get("productVariant")) : null;
    }

}

