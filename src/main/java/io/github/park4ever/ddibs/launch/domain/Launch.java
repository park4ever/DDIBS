package io.github.park4ever.ddibs.launch.domain;

import io.github.park4ever.ddibs.common.entity.BaseTimeEntity;
import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.product.domain.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.*;
import static jakarta.persistence.FetchType.*;
import static jakarta.persistence.GenerationType.*;
import static lombok.AccessLevel.*;

@Entity
@Getter
@Table(
        name = "launch",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_launch_code", columnNames = "launch_code")
        }
)
@NoArgsConstructor(access = PROTECTED)
public class Launch extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "launch_code", nullable = false, length = 20)
    private String launchCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(STRING)
    @Column(nullable = false, length = 20)
    private LaunchStatus status;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    public Launch(
            Product product, String launchCode, String name,
            LaunchStatus status, LocalDateTime startAt, LocalDateTime endAt
    ) {
        validatePeriod(startAt, endAt);

        this.product = product;
        this.launchCode = launchCode;
        this.name = name;
        this.status = status;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public static Launch create(
            Product product, String launchCode,
            String name, LocalDateTime startAt, LocalDateTime endAt
    ) {
        return new Launch(product, launchCode, name, LaunchStatus.UPCOMING, startAt, endAt);
    }

    public void open() {
        if (this.status != LaunchStatus.UPCOMING && this.status != LaunchStatus.CLOSED) {
            throw new BusinessException(ErrorCode.INVALID_LAUNCH_STATUS_TRANSITION);
        }

        this.status = LaunchStatus.OPEN;
    }

    public void close() {
        if (this.status != LaunchStatus.OPEN) {
            throw new BusinessException(ErrorCode.INVALID_LAUNCH_STATUS_TRANSITION);
        }

        this.status = LaunchStatus.CLOSED;
    }

    public void end() {
        if (this.status == LaunchStatus.ENDED || this.status == LaunchStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_LAUNCH_STATUS_TRANSITION);
        }

        this.status = LaunchStatus.ENDED;
    }

    public void cancel() {
        if (this.status == LaunchStatus.ENDED || this.status == LaunchStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.INVALID_LAUNCH_STATUS_TRANSITION);
        }

        this.status = LaunchStatus.CANCELLED;
    }

    public boolean isOrderableAt(LocalDateTime currentTime) {
        return this.status == LaunchStatus.OPEN
                && !currentTime.isBefore(this.startAt)
                && currentTime.isBefore(this.endAt);
    }

    public boolean shouldOpenAutomatically(LocalDateTime currentTime) {
        return this.status == LaunchStatus.UPCOMING
                && !currentTime.isBefore(this.startAt)
                && currentTime.isBefore(this.endAt);
    }

    public boolean shouldEndAutomatically(LocalDateTime currentTime) {
        return (this.status == LaunchStatus.UPCOMING
                || this.status == LaunchStatus.OPEN
                || this.status == LaunchStatus.CLOSED)
                && !currentTime.isBefore(this.endAt);
    }

    private void validatePeriod(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            throw new BusinessException(ErrorCode.INVALID_LAUNCH_PERIOD);
        }
    }
}
