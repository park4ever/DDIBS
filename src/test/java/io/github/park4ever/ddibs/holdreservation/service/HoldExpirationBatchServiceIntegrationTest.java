package io.github.park4ever.ddibs.holdreservation.service;

import io.github.park4ever.ddibs.holdreservation.batch.HoldExpirationBatchResult;
import io.github.park4ever.ddibs.holdreservation.domain.HoldReservation;
import io.github.park4ever.ddibs.holdreservation.domain.HoldStatus;
import io.github.park4ever.ddibs.holdreservation.repository.HoldReservationRepository;
import io.github.park4ever.ddibs.launch.domain.Launch;
import io.github.park4ever.ddibs.launch.repository.LaunchRepository;
import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;
import io.github.park4ever.ddibs.launchvariant.repository.LaunchVariantRepository;
import io.github.park4ever.ddibs.member.domain.Member;
import io.github.park4ever.ddibs.member.domain.Role;
import io.github.park4ever.ddibs.member.repository.MemberRepository;
import io.github.park4ever.ddibs.order.domain.Order;
import io.github.park4ever.ddibs.order.domain.OrderStatus;
import io.github.park4ever.ddibs.order.dto.CreateOrderRequest;
import io.github.park4ever.ddibs.order.dto.CreateOrderResponse;
import io.github.park4ever.ddibs.order.repository.OrderRepository;
import io.github.park4ever.ddibs.order.service.OrderService;
import io.github.park4ever.ddibs.payment.dto.RequestPaymentRequest;
import io.github.park4ever.ddibs.payment.service.PaymentService;
import io.github.park4ever.ddibs.product.domain.Product;
import io.github.park4ever.ddibs.product.repository.ProductRepository;
import io.github.park4ever.ddibs.productvariant.domain.ProductVariant;
import io.github.park4ever.ddibs.productvariant.repository.ProductVariantRepository;
import io.github.park4ever.ddibs.seller.domain.Seller;
import io.github.park4ever.ddibs.seller.repository.SellerRepository;
import io.github.park4ever.ddibs.support.MySqlContainerIntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;

public class HoldExpirationBatchServiceIntegrationTest extends MySqlContainerIntegrationTestSupport {

    @MockitoSpyBean
    private HoldExpirationItemProcessor holdExpirationItemProcessor;

    @Autowired
    private HoldExpirationBatchService holdExpirationBatchService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private LaunchRepository launchRepository;

    @Autowired
    private LaunchVariantRepository launchVariantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private HoldReservationRepository holdReservationRepository;

    @Test
    @DisplayName("만료된 ACTIVE 홀드는 EXPIRED 처리되고, 주문 만료 및 재고 복구가 수행된다.")
    void expireHolds_success() {
        // given
        PendingOrderFixture fixture = createPendingOrderFixture(10);
        LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 20, 12, 0, 0);
        useFixedClock(fixedNow);

        expireHoldAt(fixture.order().getId(), fixedNow.minusMinutes(1));

        // when
        HoldExpirationBatchResult result = holdExpirationBatchService.expireHolds();

        // then
        HoldReservation expiredHold = holdReservationRepository.findByOrderId(fixture.order().getId()).orElseThrow();
        Order expiredOrder = orderRepository.findById(fixture.order().getId()).orElseThrow();
        LaunchVariant restoredLaunchVariant = launchVariantRepository.findById(fixture.launchVariant().getId()).orElseThrow();

        assertThat(result.candidateCount()).isEqualTo(1);
        assertThat(result.expiredCount()).isEqualTo(1);
        assertThat(result.orderStateSkippedCount()).isEqualTo(0);
        assertThat(result.holdStateSkippedCount()).isEqualTo(0);
        assertThat(expiredHold.getStatus()).isEqualTo(HoldStatus.EXPIRED);
        assertThat(expiredOrder.getStatus()).isEqualTo(OrderStatus.HOLD_EXPIRED);
        assertThat(restoredLaunchVariant.getAvailableStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("ACTIVE가 아닌 홀드는 만료 배치 대상이 아니다.")
    void expireHolds_skipWhenHoldIsNotActive() {
        // given
        ConfirmedOrderFixture fixture = createConfirmedOrderFixture(10);
        LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 20, 12, 0, 0);
        useFixedClock(fixedNow);

        expireHoldAt(fixture.order().getId(), fixedNow.minusMinutes(1));

        // when
        HoldExpirationBatchResult result = holdExpirationBatchService.expireHolds();

        // then
        HoldReservation unchangedHold = holdReservationRepository.findByOrderId(fixture.order().getId()).orElseThrow();
        Order unchangedOrder = orderRepository.findById(fixture.order().getId()).orElseThrow();
        LaunchVariant launchVariant = launchVariantRepository.findById(fixture.launchVariant().getId()).orElseThrow();

        assertThat(result.candidateCount()).isEqualTo(0);
        assertThat(result.expiredCount()).isEqualTo(0);
        assertThat(result.orderStateSkippedCount()).isEqualTo(0);
        assertThat(result.holdStateSkippedCount()).isEqualTo(0);
        assertThat(unchangedHold.getStatus()).isEqualTo(HoldStatus.CONSUMED);
        assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(launchVariant.getAvailableStock()).isEqualTo(9);
    }

    @Test
    @DisplayName("아직 만료 시간이 지나지 않은 ACTIVE 홀드는 만료 배치 대상이 아니다.")
    void expireHolds_skipWhenHoldIsNotExpiredYet() {
        // given
        PendingOrderFixture fixture = createPendingOrderFixture(10);
        LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 20, 12, 0, 0);
        useFixedClock(fixedNow);

        expireHoldAt(fixture.order().getId(), fixedNow.plusMinutes(1));

        // when
        HoldExpirationBatchResult result = holdExpirationBatchService.expireHolds();

        // then
        HoldReservation holdReservation = holdReservationRepository.findByOrderId(fixture.order().getId()).orElseThrow();
        Order order = orderRepository.findById(fixture.order().getId()).orElseThrow();
        LaunchVariant launchVariant = launchVariantRepository.findById(fixture.launchVariant().getId()).orElseThrow();

        assertThat(result.candidateCount()).isEqualTo(0);
        assertThat(result.expiredCount()).isEqualTo(0);
        assertThat(result.orderStateSkippedCount()).isEqualTo(0);
        assertThat(result.holdStateSkippedCount()).isEqualTo(0);
        assertThat(holdReservation.getStatus()).isEqualTo(HoldStatus.ACTIVE);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(launchVariant.getAvailableStock()).isEqualTo(9);
    }

    @Test
    @DisplayName("expiresAt과 현재 시각이 같으면 ACTIVE 홀드는 만료 배치 대상이다.")
    void expireHolds_whenNowEqualsExpiresAt() {
        // given
        PendingOrderFixture fixture = createPendingOrderFixture(10);
        LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 20, 12, 0, 0);
        useFixedClock(fixedNow);

        expireHoldAt(fixture.order().getId(), fixedNow);

        // when
        HoldExpirationBatchResult result = holdExpirationBatchService.expireHolds();

        // then
        HoldReservation expiredHold = holdReservationRepository.findByOrderId(fixture.order().getId()).orElseThrow();
        Order expiredOrder = orderRepository.findById(fixture.order().getId()).orElseThrow();
        LaunchVariant restoredLaunchVariant = launchVariantRepository.findById(fixture.launchVariant().getId()).orElseThrow();

        assertThat(result.candidateCount()).isEqualTo(1);
        assertThat(result.expiredCount()).isEqualTo(1);
        assertThat(result.orderStateSkippedCount()).isEqualTo(0);
        assertThat(result.holdStateSkippedCount()).isEqualTo(0);
        assertThat(expiredHold.getStatus()).isEqualTo(HoldStatus.EXPIRED);
        assertThat(expiredOrder.getStatus()).isEqualTo(OrderStatus.HOLD_EXPIRED);
        assertThat(restoredLaunchVariant.getAvailableStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("홀드 만료 배치가 중간 실패 후, 재실행되면 남은 건만 다시 처리된다.")
    void expireHolds_resumeAfterPartialFailure() {
        // given
        PendingOrderFixture firstFixture = createPendingOrderFixture(10);
        PendingOrderFixture secondFixture = createPendingOrderFixture(10);

        LocalDateTime fixedNow = LocalDateTime.of(2026, 5, 20, 12, 0, 0);
        useFixedClock(fixedNow);

        expireHoldAt(firstFixture.order().getId(), fixedNow.minusMinutes(2));
        expireHoldAt(secondFixture.order().getId(), fixedNow.minusMinutes(1));

        AtomicBoolean failOnce = new AtomicBoolean(true);

        doAnswer(invocation -> {
            Long orderId = invocation.getArgument(0);

            if (orderId.equals(secondFixture.order().getId()) && failOnce.getAndSet(false)) {
                throw new RuntimeException("forced failure for retry test");
            }

            return invocation.callRealMethod();
        }).when(holdExpirationItemProcessor).expire(anyLong(), any(LocalDateTime.class));

        // when & then - 1차 실행
        assertThatThrownBy(() -> holdExpirationBatchService.expireHolds())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("forced failure for retry test");

        HoldReservation firstHoldAfterFailure = holdReservationRepository.findByOrderId(firstFixture.order().getId()).orElseThrow();
        Order firstOrderAfterFailure = orderRepository.findById(firstFixture.order().getId()).orElseThrow();
        LaunchVariant firstLaunchVariantAfterFailure = launchVariantRepository.findById(firstFixture.launchVariant().getId()).orElseThrow();

        HoldReservation secondHoldAfterFailure = holdReservationRepository.findByOrderId(secondFixture.order().getId()).orElseThrow();
        Order secondOrderAfterFailure = orderRepository.findById(secondFixture.order().getId()).orElseThrow();
        LaunchVariant secondLaunchVariantAfterFailure = launchVariantRepository.findById(secondFixture.launchVariant().getId()).orElseThrow();

        assertThat(firstHoldAfterFailure.getStatus()).isEqualTo(HoldStatus.EXPIRED);
        assertThat(firstOrderAfterFailure.getStatus()).isEqualTo(OrderStatus.HOLD_EXPIRED);
        assertThat(firstLaunchVariantAfterFailure.getAvailableStock()).isEqualTo(10);

        assertThat(secondHoldAfterFailure.getStatus()).isEqualTo(HoldStatus.ACTIVE);
        assertThat(secondOrderAfterFailure.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(secondLaunchVariantAfterFailure.getAvailableStock()).isEqualTo(9);

        // when - 2차 재실행
        HoldExpirationBatchResult rerunResult = holdExpirationBatchService.expireHolds();

        // then
        HoldReservation secondHoldAfterRerun = holdReservationRepository.findByOrderId(secondFixture.order().getId()).orElseThrow();
        Order secondOrderAfterRerun = orderRepository.findById(secondFixture.order().getId()).orElseThrow();
        LaunchVariant secondLaunchVariantAfterRerun = launchVariantRepository.findById(secondFixture.launchVariant().getId()).orElseThrow();

        assertThat(rerunResult.candidateCount()).isEqualTo(1);
        assertThat(rerunResult.expiredCount()).isEqualTo(1);
        assertThat(rerunResult.orderStateSkippedCount()).isEqualTo(0);
        assertThat(rerunResult.holdStateSkippedCount()).isEqualTo(0);

        assertThat(secondHoldAfterRerun.getStatus()).isEqualTo(HoldStatus.EXPIRED);
        assertThat(secondOrderAfterRerun.getStatus()).isEqualTo(OrderStatus.HOLD_EXPIRED);
        assertThat(secondLaunchVariantAfterRerun.getAvailableStock()).isEqualTo(10);
    }

    private void expireHoldAt(Long orderId, LocalDateTime expiresAt) {
        HoldReservation holdReservation = holdReservationRepository.findByOrderId(orderId).orElseThrow();
        ReflectionTestUtils.setField(holdReservation, "expiresAt", expiresAt);
        holdReservationRepository.saveAndFlush(holdReservation);
    }

    private void useFixedClock(LocalDateTime fixedNow) {
        Clock fixedClock = Clock.fixed(
                fixedNow.atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );
        ReflectionTestUtils.setField(holdExpirationBatchService, "clock", fixedClock);
    }

    private PendingOrderFixture createPendingOrderFixture(int totalStock) {
        Member member = createMember();
        Seller seller = createSeller();
        Product product = createProduct(seller);
        ProductVariant productVariant = createProductVariant(product);

        LocalDateTime now = LocalDateTime.now();
        Launch launch = createOpenLaunch(product, now.minusMinutes(1), now.plusMinutes(30));
        LaunchVariant launchVariant = createLaunchVariant(launch, productVariant, new BigDecimal("159000.00"), totalStock);

        CreateOrderResponse orderResponse = orderService.createOrder(
                member.getId(),
                new CreateOrderRequest(launchVariant.getId())
        );

        Order order = orderRepository.findById(orderResponse.id()).orElseThrow();
        return new PendingOrderFixture(member, order, launchVariant);
    }

    private ConfirmedOrderFixture createConfirmedOrderFixture(int totalStock) {
        PendingOrderFixture pendingFixture = createPendingOrderFixture(totalStock);

        paymentService.requestPayment(
                pendingFixture.member().getId(),
                new RequestPaymentRequest(pendingFixture.order().getId(), true, null)
        );

        Order confirmedOrder = orderRepository.findById(pendingFixture.order().getId()).orElseThrow();
        return new ConfirmedOrderFixture(pendingFixture.member(), confirmedOrder, pendingFixture.launchVariant());
    }

    private Member createMember() {
        String suffix = uniqueSuffix();

        Member member = Member.createUser(
                "user-" + suffix + "@test.com",
                "1q2w3e4r!",
                "testuser-" + suffix
        );

        return memberRepository.save(member);
    }

    private Seller createSeller() {
        String suffix = uniqueSuffix();

        Seller seller = Seller.create(
                "SEL-" + suffix,
                "홀드만료 테스트 셀러 " + suffix
        );

        return sellerRepository.save(seller);
    }

    private Product createProduct(Seller seller) {
        String suffix = uniqueSuffix();

        Product product = Product.create(
                seller,
                "PRD-" + suffix,
                "홀드만료 테스트 상품 " + suffix
        );

        return productRepository.save(product);
    }

    private ProductVariant createProductVariant(Product product) {
        String suffix = uniqueSuffix();

        ProductVariant productVariant = ProductVariant.create(
                product,
                "PVT-" + suffix,
                "HOLD-EXPIRE-VARIANT-" + suffix
        );

        return productVariantRepository.save(productVariant);
    }

    private Launch createOpenLaunch(Product product, LocalDateTime startAt, LocalDateTime endAt) {
        String suffix = uniqueSuffix();

        Launch launch = Launch.create(
                product,
                "LCH-" + suffix,
                "홀드만료 테스트 발매 " + suffix,
                startAt,
                endAt
        );
        launch.open();

        return launchRepository.save(launch);
    }

    private LaunchVariant createLaunchVariant(
            Launch launch,
            ProductVariant productVariant,
            BigDecimal salePrice,
            int totalStock
    ) {
        LaunchVariant launchVariant = LaunchVariant.create(
                launch,
                productVariant,
                salePrice,
                totalStock
        );

        return launchVariantRepository.save(launchVariant);
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private record PendingOrderFixture(
            Member member,
            Order order,
            LaunchVariant launchVariant
    ) {
    }

    private record ConfirmedOrderFixture(
            Member member,
            Order order,
            LaunchVariant launchVariant
    ) {
    }
}