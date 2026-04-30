package io.github.park4ever.ddibs.order.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.holdreservation.domain.HoldReservation;
import io.github.park4ever.ddibs.holdreservation.domain.HoldStatus;
import io.github.park4ever.ddibs.holdreservation.repository.HoldReservationRepository;
import io.github.park4ever.ddibs.launch.domain.Launch;
import io.github.park4ever.ddibs.launch.repository.LaunchRepository;
import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;
import io.github.park4ever.ddibs.launchvariant.repository.LaunchVariantRepository;
import io.github.park4ever.ddibs.member.domain.Member;
import io.github.park4ever.ddibs.member.repository.MemberRepository;
import io.github.park4ever.ddibs.order.domain.Order;
import io.github.park4ever.ddibs.order.domain.OrderStatus;
import io.github.park4ever.ddibs.order.dto.CreateOrderRequest;
import io.github.park4ever.ddibs.order.dto.CreateOrderResponse;
import io.github.park4ever.ddibs.order.repository.OrderRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class OrderServiceIntegrationTest extends MySqlContainerIntegrationTestSupport {

    @Autowired
    private OrderService orderService;

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
    @DisplayName("주문 생성 성공 시, 주문과 홀드가 생성되고 가용 재고가 감소한다.")
    void createOrder_success() {
        // given
        OrderCreateFixture fixture = createFixture(10, LaunchScenario.OPEN);
        CreateOrderRequest request = new CreateOrderRequest(fixture.launchVariant().getId());

        LocalDateTime beforeRequest = LocalDateTime.now();

        // when
        CreateOrderResponse response = orderService.createOrder(fixture.member().getId(), request);

        LocalDateTime afterRequest = LocalDateTime.now();

        // then
        Order savedOrder = orderRepository.findById(response.id()).orElseThrow();
        HoldReservation holdReservation = holdReservationRepository.findByOrderId(savedOrder.getId()).orElseThrow();
        LaunchVariant savedLaunchVariant = launchVariantRepository.findById(fixture.launchVariant().getId()).orElseThrow();

        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(savedOrder.getQuantity()).isEqualTo(1);
        assertThat(savedOrder.getUnitPrice()).isEqualByComparingTo("159000.00");
        assertThat(savedOrder.getTotalPrice()).isEqualByComparingTo("159000.00");

        assertThat(holdReservation.getStatus()).isEqualTo(HoldStatus.ACTIVE);
        assertThat(holdReservation.getQuantity()).isEqualTo(1);
        assertThat(holdReservation.getExpiresAt())
                .isAfterOrEqualTo(beforeRequest.plusMinutes(10))
                .isBeforeOrEqualTo(afterRequest.plusMinutes(10));

        assertThat(savedLaunchVariant.getAvailableStock()).isEqualTo(9);
    }

    @Test
    @DisplayName("주문 불가 발매 상태이면 주문 생성에 실패하고, 홀드와 재고 차감이 발생하지 않는다.")
    void createOrder_fail_whenLaunchIsNotOrderable() {
        // given
        OrderCreateFixture fixture = createFixture(10, LaunchScenario.UPCOMING);

        long orderCountBefore = orderRepository.count();
        long holdCountBefore = holdReservationRepository.count();

        CreateOrderRequest request = new CreateOrderRequest(fixture.launchVariant().getId());

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(fixture.member().getId(), request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_CREATION_NOT_ALLOWED);

        LaunchVariant savedLaunchVariant = launchVariantRepository.findById(fixture.launchVariant().getId()).orElseThrow();

        assertThat(orderRepository.count()).isEqualTo(orderCountBefore);
        assertThat(holdReservationRepository.count()).isEqualTo(holdCountBefore);
        assertThat(savedLaunchVariant.getAvailableStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("가용 재고가 부족하면 주문 생성에 실패하고, 홀드와 재고 차감이 발생하지 않는다.")
    void createOrder_fail_whenInsufficientStock() {
        // given
        OrderCreateFixture fixture = createFixture(0, LaunchScenario.OPEN);

        long orderCountBefore = orderRepository.count();
        long holdCountBefore = holdReservationRepository.count();

        CreateOrderRequest request = new CreateOrderRequest(fixture.launchVariant().getId());

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(fixture.member().getId(), request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_LAUNCH_VARIANT_STOCK);

        LaunchVariant savedLaunchVariant = launchVariantRepository.findById(fixture.launchVariant().getId()).orElseThrow();

        assertThat(orderRepository.count()).isEqualTo(orderCountBefore);
        assertThat(holdReservationRepository.count()).isEqualTo(holdCountBefore);
        assertThat(savedLaunchVariant.getAvailableStock()).isEqualTo(0);
    }

    private OrderCreateFixture createFixture(int totalStock, LaunchScenario launchScenario) {
        Member member = createMember();
        Seller seller = createSeller();
        Product product = createProduct(seller);
        ProductVariant productVariant = createProductVariant(product);

        LocalDateTime now = LocalDateTime.now();
        Launch launch = switch (launchScenario) {
            case OPEN -> createLaunch(product, now.minusMinutes(1), now.plusMinutes(10), true);
            case UPCOMING -> createLaunch(product, now.plusMinutes(10), now.plusMinutes(20), false);
        };

        LaunchVariant launchVariant = createLaunchVariant(
                launch,
                productVariant,
                new BigDecimal("159000.00"),
                totalStock
        );

        return new OrderCreateFixture(member, launchVariant);
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
                "테스트 셀러 " + suffix
        );

        return sellerRepository.save(seller);
    }

    private Product createProduct(Seller seller) {
        String suffix = uniqueSuffix();

        Product product = Product.create(
                seller,
                "PRD-" + suffix,
                "Nike Dunk Low " + suffix
        );

        return productRepository.save(product);
    }

    private ProductVariant createProductVariant(Product product) {
        String suffix = uniqueSuffix();

        ProductVariant productVariant = ProductVariant.create(
                product,
                "VRT-" + suffix,
                "Black / 270 / " + suffix
        );

        return productVariantRepository.save(productVariant);
    }

    private Launch createLaunch(
            Product product,
            LocalDateTime startAt,
            LocalDateTime endAt,
            boolean open
    ) {
        String suffix = uniqueSuffix();

        Launch launch = Launch.create(
                product,
                "LCH-" + suffix,
                (open ? "오픈" : "예정") + " 테스트 발매 " + suffix,
                startAt,
                endAt
        );

        if (open) {
            launch.open();
        }

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

    private record OrderCreateFixture(
            Member member,
            LaunchVariant launchVariant
    ) {
    }

    private enum LaunchScenario {
        OPEN,
        UPCOMING
    }
}
