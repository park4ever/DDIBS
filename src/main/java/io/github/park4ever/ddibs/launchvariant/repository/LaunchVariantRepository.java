package io.github.park4ever.ddibs.launchvariant.repository;

import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LaunchVariantRepository extends JpaRepository<LaunchVariant, Long> {

    boolean existsByLaunchIdAndProductVariantId(Long launchId, Long productVariantId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select lv
            from LaunchVariant lv
            join fetch lv.launch l
            join fetch l.product p
            join fetch p.seller s
            join fetch lv.productVariant pv
            where lv.id = :launchVariantId
            """)
    Optional<LaunchVariant> findByIdForUpdate(@Param("launchVariantId") Long launchVariantId);
}
