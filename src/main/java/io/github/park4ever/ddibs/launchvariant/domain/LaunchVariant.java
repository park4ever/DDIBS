package io.github.park4ever.ddibs.launchvariant.domain;

import io.github.park4ever.ddibs.common.entity.BaseTimeEntity;
import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.launch.domain.Launch;
import io.github.park4ever.ddibs.productvariant.domain.ProductVariant;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Table(
        name = "launch_variant",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_launch_variant_launch_id_product_variant_id",
                        columnNames = {"launch_id", "product_variant_id"}
                )
        }
)
@NoArgsConstructor(access = PROTECTED)
public class LaunchVariant extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "launch_id", nullable = false)
    private Launch launch;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(name = "sale_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "total_stock", nullable = false)
    private int totalStock;

    @Column(name = "available_stock", nullable = false)
    private int availableStock;

    private LaunchVariant(
            Launch launch, ProductVariant productVariant,
            BigDecimal salePrice, int totalStock, int availableStock
    ) {
        validateLaunchAndProductVariant(launch, productVariant);
        validateSalePrice(salePrice);
        validateStock(totalStock, availableStock);

        this.launch = launch;
        this.productVariant = productVariant;
        this.salePrice = salePrice;
        this.totalStock = totalStock;
        this.availableStock = availableStock;
    }

    public static LaunchVariant create(
            Launch launch, ProductVariant productVariant,
            BigDecimal salePrice, int totalStock
    ) {
        return new LaunchVariant(launch, productVariant, salePrice, totalStock, totalStock);
    }

    public boolean hasAvailableStock(int quantity) {
        validateQuantity(quantity);
        return this.availableStock >= quantity;
    }

    public boolean isOrderableAt(LocalDateTime currentTime, int quantity) {
        return this.launch.isOrderableAt(currentTime) && hasAvailableStock(quantity);
    }

    public void decreaseAvailableStock(int quantity) {
        validateQuantity(quantity);

        if (this.availableStock < quantity) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_LAUNCH_VARIANT_STOCK);
        }

        this.availableStock -= quantity;
    }

    public void restoreAvailableStock(int quantity) {
        validateQuantity(quantity);

        if (this.availableStock + quantity > this.totalStock) {
            throw new BusinessException(ErrorCode.INVALID_LAUNCH_VARIANT_STOCK_RESTORE);
        }

        this.availableStock += quantity;
    }

    private void validateLaunchAndProductVariant(Launch launch, ProductVariant productVariant) {
        if (launch == null || productVariant == null) {
            throw new BusinessException(ErrorCode.INVALID_LAUNCH_VARIANT_RELATION);
        }

        Long launchProductId = launch.getProduct().getId();
        Long productVariantProductId = productVariant.getProduct().getId();

        if (launchProductId == null || productVariantProductId == null
                || !Objects.equals(launchProductId, productVariantProductId)) {
            throw new BusinessException(ErrorCode.INVALID_LAUNCH_VARIANT_RELATION);
        }
    }

    private void validateSalePrice(BigDecimal salePrice) {
        if (salePrice == null || salePrice.signum() < 0) {
            throw new BusinessException(ErrorCode.INVALID_LAUNCH_VARIANT_SALE_PRICE);
        }
    }

    private void validateStock(int totalStock, int availableStock) {
        if (totalStock < 0 || availableStock < 0 || availableStock > totalStock) {
            throw new BusinessException(ErrorCode.INVALID_LAUNCH_VARIANT_STOCK);
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_QUANTITY);
        }
    }
}
