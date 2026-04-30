package io.github.park4ever.ddibs.productvariant.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductVariant is a Querydsl query type for ProductVariant
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductVariant extends EntityPathBase<ProductVariant> {

    private static final long serialVersionUID = 2032141253L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductVariant productVariant = new QProductVariant("productVariant");

    public final io.github.park4ever.ddibs.common.entity.QBaseTimeEntity _super = new io.github.park4ever.ddibs.common.entity.QBaseTimeEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final io.github.park4ever.ddibs.product.domain.QProduct product;

    public final EnumPath<ProductVariantStatus> status = createEnum("status", ProductVariantStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath variantCode = createString("variantCode");

    public QProductVariant(String variable) {
        this(ProductVariant.class, forVariable(variable), INITS);
    }

    public QProductVariant(Path<? extends ProductVariant> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductVariant(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductVariant(PathMetadata metadata, PathInits inits) {
        this(ProductVariant.class, metadata, inits);
    }

    public QProductVariant(Class<? extends ProductVariant> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.product = inits.isInitialized("product") ? new io.github.park4ever.ddibs.product.domain.QProduct(forProperty("product"), inits.get("product")) : null;
    }

}

