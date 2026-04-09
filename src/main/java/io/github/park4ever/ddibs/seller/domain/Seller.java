package io.github.park4ever.ddibs.seller.domain;

import io.github.park4ever.ddibs.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Table(
        name = "seller",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seller_code", columnNames = "seller_code")
        }
)
@NoArgsConstructor(access = PROTECTED)
public class Seller extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "seller_code", nullable = false, length = 20)
    private String sellerCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(STRING)
    @Column(nullable = false, length = 20)
    private SellerStatus status;

    private Seller(String sellerCode, String name, SellerStatus status) {
        this.sellerCode = sellerCode;
        this.name = name;
        this.status = status;
    }

    public static Seller create(String sellerCode, String name) {
        return new Seller(sellerCode, name, SellerStatus.ACTIVE);
    }

    public void activate() {
        this.status = SellerStatus.ACTIVE;
    }

    public void inactivate() {
        this.status = SellerStatus.INACTIVE;
    }

    public void suspend() {
        this.status = SellerStatus.SUSPENDED;
    }

    public boolean isActive() {
        return this.status == SellerStatus.ACTIVE;
    }
}
