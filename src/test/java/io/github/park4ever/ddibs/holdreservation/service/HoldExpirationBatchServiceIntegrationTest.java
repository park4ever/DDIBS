package io.github.park4ever.ddibs.holdreservation.service;

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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Transactional
public class HoldExpirationBatchServiceIntegrationTest extends MySqlContainerIntegrationTestSupport {

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
        //given
        PendingOrderFixture fixture = createPendingOrderFixture(10);

        HoldReservation holdReservation = holdReservationRepository.findByOrderId(fixture.order.getId()).orElseThrow();
        ReflectionTestUtils.setField(holdReservation, "expiresAt", LocalDateTime.now().minusMinutes(1));
        holdReservationRepository.flush();

        //when
        int expiredCount = holdExpirationBatchService.expireHolds();

        //then
        HoldReservation expireHold = holdReservationRepository.findByOrderId(fixture.order.getId()).orElseThrow();
        Order expiredOrder = orderRepository.findById(fixture.order.getId()).orElseThrow();
        LaunchVariant restoredLaunchVariant = launchVariantRepository.findById(fixture.launchVariant.getId()).orElseThrow();

        assertThat(expiredCount).isEqualTo(1);
        assertThat(expireHold.getStatus()).isEqualTo(HoldStatus.EXPIRED);
        assertThat(expiredOrder.getStatus()).isEqualTo(OrderStatus.HOLD_EXPIRED);
        assertThat(restoredLaunchVariant.getAvailableStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("ACITVE가 아닌 홀드는 만료 배치 대상이 아니다.")
    void expireHolds_skipWhenHoldIsNotActive() {
        //given
        ConfirmedOrderFixture fixture = createConfirmedOrderFixture(10);

        HoldReservation holdReservation = holdReservationRepository.findByOrderId(fixture.order.getId()).orElseThrow();
        ReflectionTestUtils.setField(holdReservation, "expiresAt", LocalDateTime.now().minusMinutes(1));
        holdReservationRepository.flush();

        //when
        int expiredCount = holdExpirationBatchService.expireHolds();

        //then
        HoldReservation unchangedHold = holdReservationRepository.findByOrderId(fixture.order().getId()).orElseThrow();
        Order unchangedOrder = orderRepository.findById(fixture.order().getId()).orElseThrow();
        LaunchVariant launchVariant = launchVariantRepository.findById(fixture.launchVariant().getId()).orElseThrow();

        assertThat(expiredCount).isEqualTo(0);
        assertThat(unchangedHold.getStatus()).isEqualTo(HoldStatus.CONSUMED);
        assertThat(unchangedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(launchVariant.getAvailableStock()).isEqualTo(9);
    }

    @Test
    @DisplayName("아직 만료 시간이 지나지 않은 ACTIVE 홀드는 만료 배치 대상이 아니다.")
    void expireHolds_skipWhenHoldIsNotExpiredYet() {
        // given
        PendingOrderFixture fixture = createPendingOrderFixture(10);

        // when
        int expiredCount = holdExpirationBatchService.expireHolds();

        // then
        HoldReservation holdReservation = holdReservationRepository.findByOrderId(fixture.order().getId()).orElseThrow();
        Order order = orderRepository.findById(fixture.order().getId()).orElseThrow();
        LaunchVariant launchVariant = launchVariantRepository.findById(fixture.launchVariant().getId()).orElseThrow();

        assertThat(expiredCount).isEqualTo(0);
        assertThat(holdReservation.getStatus()).isEqualTo(HoldStatus.ACTIVE);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(launchVariant.getAvailableStock()).isEqualTo(9);
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
        Member member = Member.createUser(
                "user@test.com",
                "1q2w3e4r!",
                "testuser"
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
