package io.github.park4ever.ddibs.seller.repository;

import io.github.park4ever.ddibs.seller.domain.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findBySellerCode(String sellerCode);

    boolean existsBySellerCode(String sellerCode);
}
