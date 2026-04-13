package io.github.park4ever.ddibs.productvariant.repository;

import io.github.park4ever.ddibs.productvariant.domain.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    Optional<ProductVariant> findByVariantCode(String variantCode);

    boolean existsByVariantCode(String variantCode);

    boolean existsByProductIdAndName(Long productId, String name);
}
