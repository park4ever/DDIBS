package io.github.park4ever.ddibs.launchvariant.repository;

import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaunchVariantRepository extends JpaRepository<LaunchVariant, Long> {

    boolean existsByLaunchIdAndProductVariantId(Long launchId, Long productVariantId);
}
