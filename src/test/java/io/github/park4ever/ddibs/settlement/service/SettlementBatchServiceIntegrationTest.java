package io.github.park4ever.ddibs.settlement.service;

import io.github.park4ever.ddibs.launch.domain.Launch;
import io.github.park4ever.ddibs.launch.repository.LaunchRepository;
import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;
import io.github.park4ever.ddibs.launchvariant.repository.LaunchVariantRepository;
import io.github.park4ever.ddibs.member.domain.Member;
import io.github.park4ever.ddibs.member.repository.MemberRepository;
import io.github.park4ever.ddibs.order.domain.Order;
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
import io.github.park4ever.ddibs.settlement.domain.Settlement;
import io.github.park4ever.ddibs.settlement.domain.SettlementStatus;
import io.github.park4ever.ddibs.settlement.repository.SettlementRepository;
import io.github.park4ever.ddibs.support.MySqlContainerIntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class SettlementBatchServiceIntegrationTest extends MySqlContainerIntegrationTestSupport {

    @Autowired
    private SettlementBatchService settlementBatchService;

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
    private SettlementRepository settlementRepository;

    @Test
    @DisplayName("확정 주문이 있으면 정산 생성 배치가 Settlement를 생성한다.")
    void generateSettlements_success() {
        // given
        ConfirmedOrderFixture fixture = createConfirmedOrderFixture();

        // when
        int createdCount = settlementBatchService.generateSettlements();

        // then
        Settlement settlement = settlementRepository.findByOrderId(fixture.order().getId()).orElseThrow();

        assertThat(createdCount).isEqualTo(1);
        assertThat(settlement.getOrder().getId()).isEqualTo(fixture.order().getId());
        assertThat(settlement.getSellerId()).isEqualTo(fixture.order().getSellerId());
        assertThat(settlement.getSettlementAmount()).isEqualByComparingTo(fixture.order().getTotalPrice());
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.CREATED);
        assertThat(settlement.getSettledAt()).isNull();
    }

    @Test
    @DisplayName("이미 정산이 생성된 주문은 배치가 다시 실행되어도 중복 생성되지 않는다.")
    void generateSettlements_skipWhenAlreadyExists() {
        // given
        ConfirmedOrderFixture fixture = createConfirmedOrderFixture();

        int firstCreatedCount = settlementBatchService.generateSettlements();
        long settlementCountAfterFirstRun = settlementRepository.count();

        // when
        int secondCreatedCount = settlementBatchService.generateSettlements();

        // then
        assertThat(firstCreatedCount).isEqualTo(1);
        assertThat(secondCreatedCount).isEqualTo(0);
        assertThat(settlementRepository.count()).isEqualTo(settlementCountAfterFirstRun);
        assertThat(settlementRepository.findByOrderId(fixture.order().getId())).isPresent();
    }

    @Test
    @DisplayName("확정되지 않은 주문은 정산 생성 대상이 아니다.")
    void generateSettlements_skipWhenOrderIsNotConfirmed() {
        // given
        PendingOrderFixture fixture = createPendingOrderFixture();

        // when
        int createdCount = settlementBatchService.generateSettlements();

        // then
        assertThat(createdCount).isEqualTo(0);
        assertThat(settlementRepository.findByOrderId(fixture.order().getId())).isEmpty();
    }

    private ConfirmedOrderFixture createConfirmedOrderFixture() {
        Member member = createMember();
        Seller seller = createSeller();
        Product product = createProduct(seller);
        ProductVariant productVariant = createProductVariant(product);

        LocalDateTime now = LocalDateTime.now();
        Launch launch = createOpenLaunch(product, now.minusMinutes(1), now.plusMinutes(30));
        LaunchVariant launchVariant = createLaunchVariant(launch, productVariant, new BigDecimal("159000.00"), 10);

        CreateOrderResponse orderResponse = orderService.createOrder(
                member.getId(),
                new CreateOrderRequest(launchVariant.getId())
        );

        Order order = orderRepository.findById(orderResponse.id()).orElseThrow();

        paymentService.requestPayment(
                member.getId(),
                new RequestPaymentRequest(order.getId(), true, null)
        );

        Order confirmedOrder = orderRepository.findById(order.getId()).orElseThrow();
        return new ConfirmedOrderFixture(member, confirmedOrder);
    }

    private PendingOrderFixture createPendingOrderFixture() {
        Member member = createMember();
        Seller seller = createSeller();
        Product product = createProduct(seller);
        ProductVariant productVariant = createProductVariant(product);

        LocalDateTime now = LocalDateTime.now();
        Launch launch = createOpenLaunch(product, now.minusMinutes(1), now.plusMinutes(30));
        LaunchVariant launchVariant = createLaunchVariant(launch, productVariant, new BigDecimal("159000.00"), 10);

        CreateOrderResponse orderResponse = orderService.createOrder(
                member.getId(),
                new CreateOrderRequest(launchVariant.getId())
        );

        Order order = orderRepository.findById(orderResponse.id()).orElseThrow();
        return new PendingOrderFixture(member, order);
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
                "정산 테스트 셀러 " + suffix
        );

        return sellerRepository.save(seller);
    }

    private Product createProduct(Seller seller) {
        String suffix = uniqueSuffix();

        Product product = Product.create(
                seller,
                "PRD-" + suffix,
                "정산 테스트 상품 " + suffix
        );

        return productRepository.save(product);
    }

    private ProductVariant createProductVariant(Product product) {
        String suffix = uniqueSuffix();

        ProductVariant productVariant = ProductVariant.create(
                product,
                "PVT-" + suffix,
                "SETTLEMENT-VARIANT-" + suffix
        );

        return productVariantRepository.save(productVariant);
    }

    private Launch createOpenLaunch(Product product, LocalDateTime startAt, LocalDateTime endAt) {
        String suffix = uniqueSuffix();

        Launch launch = Launch.create(
                product,
                "LCH-" + suffix,
                "정산 테스트 발매 " + suffix,
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

    private record ConfirmedOrderFixture(
            Member member,
            Order order
    ) {
    }

    private record PendingOrderFixture(
            Member member,
            Order order
    ) {
    }
}
