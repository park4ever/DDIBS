package io.github.park4ever.ddibs.productvariant.domain;

import io.github.park4ever.ddibs.common.entity.BaseTimeEntity;
import io.github.park4ever.ddibs.product.domain.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Table(
        name = "product_variant",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_variant_code", columnNames = "variant_code"),
                @UniqueConstraint(name = "uk_product_variant_product_id_name", columnNames = {"product_id", "name"})
        }
)
@NoArgsConstructor(access = PROTECTED)
public class ProductVariant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "variant_code", nullable = false, length = 20)
    private String variantCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(STRING)
    @Column(nullable = false, length = 20)
    private ProductVariantStatus status;

    private ProductVariant(Product product, String variantCode, String name, ProductVariantStatus status) {
        this.product = product;
        this.variantCode = variantCode;
        this.name = name;
        this.status = status;
    }

    public static ProductVariant create(Product product, String variantCode, String name) {
        return new ProductVariant(product, variantCode, name, ProductVariantStatus.ACTIVE);
    }

    public void activate() {
        this.status = ProductVariantStatus.ACTIVE;
    }

    public void inactivate() {
        this.status = ProductVariantStatus.INACTIVE;
    }

    public boolean isActive() {
        return this.status == ProductVariantStatus.ACTIVE;
    }
}
