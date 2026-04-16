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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class OrderServiceIntegrationTest {

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
        //given
        Member member = createMember();
        Seller seller = createSeller();
        Product product = createProduct(seller);
        ProductVariant productVariant = createProductVariant(product);

        LocalDateTime now = LocalDateTime.now();
        Launch launch = createOpenLaunch(product, now.minusMinutes(1), now.plusMinutes(10));
        LaunchVariant launchVariant = createLaunchVariant(launch, productVariant, new BigDecimal("159000.00"), 10);

        CreateOrderRequest request = new CreateOrderRequest(launchVariant.getId());

        //when
        CreateOrderResponse response = orderService.createOrder(member.getId(), request);

        //then
        Order savedOrder = orderRepository.findById(response.id()).orElseThrow();
        HoldReservation holdReservation = holdReservationRepository.findByOrderId(savedOrder.getId()).orElseThrow();
        LaunchVariant savedLaunchVariant = launchVariantRepository.findById(launchVariant.getId()).orElseThrow();

        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(savedOrder.getQuantity()).isEqualTo(1);
        assertThat(savedOrder.getUnitPrice()).isEqualByComparingTo("159000.00");
        assertThat(savedOrder.getTotalPrice()).isEqualByComparingTo("159000.00");

        assertThat(holdReservation.getStatus()).isEqualTo(HoldStatus.ACTIVE);
        assertThat(holdReservation.getQuantity()).isEqualTo(1);
        assertThat(holdReservation.getExpiresAt()).isAfter(LocalDateTime.now());

        assertThat(savedLaunchVariant.getAvailableStock()).isEqualTo(9);
    }

    @Test
    @DisplayName("주문 불가 발매 상태이면 주문 생성에 실패하고, 홀드와 재고 차감이 발생하지 않는다.")
    void createOrder_fail_whenLaunchIsNotOrderable() {
        //given
        Member member = createMember();
        Seller seller = createSeller();
        Product product = createProduct(seller);
        ProductVariant productVariant = createProductVariant(product);

        LocalDateTime now = LocalDateTime.now();
        Launch launch = createUpcomingLaunch(product, now.plusMinutes(10), now.plusMinutes(20));
        LaunchVariant launchVariant = createLaunchVariant(launch, productVariant, new BigDecimal("159000.00"), 10);

        long orderCountBefore = orderRepository.count();
        long holdCountBefore = holdReservationRepository.count();

        CreateOrderRequest request = new CreateOrderRequest(launchVariant.getId());

        //when & then
        assertThatThrownBy(() -> orderService.createOrder(member.getId(), request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ORDER_CREATION_NOT_ALLOWED);

        LaunchVariant savedLaunchVariant = launchVariantRepository.findById(launchVariant.getId()).orElseThrow();

        assertThat(orderRepository.count()).isEqualTo(orderCountBefore);
        assertThat(holdReservationRepository.count()).isEqualTo(holdCountBefore);
        assertThat(savedLaunchVariant.getAvailableStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("가용 재고가 부족하면 주문 생성에 실패하고, 홀드와 재고 차감이 발생하지 않는다.")
    void createOrder_fail_whenInsufficientStock() {
        //given
        Member member = createMember();
        Seller seller = createSeller();
        Product product = createProduct(seller);
        ProductVariant productVariant = createProductVariant(product);

        LocalDateTime now = LocalDateTime.now();
        Launch launch = createOpenLaunch(product, now.minusMinutes(1), now.plusMinutes(10));
        LaunchVariant launchVariant = createLaunchVariant(launch, productVariant, new BigDecimal("159000.00"), 0);

        long orderCountBefore = orderRepository.count();
        long holdCountBefore = holdReservationRepository.count();

        CreateOrderRequest request = new CreateOrderRequest(launchVariant.getId());

        //when & then
        assertThatThrownBy(() -> orderService.createOrder(member.getId(), request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_LAUNCH_VARIANT_STOCK);

        LaunchVariant savedLaunchVariant = launchVariantRepository.findById(launchVariant.getId()).orElseThrow();

        assertThat(orderRepository.count()).isEqualTo(orderCountBefore);
        assertThat(holdReservationRepository.count()).isEqualTo(holdCountBefore);
        assertThat(savedLaunchVariant.getAvailableStock()).isEqualTo(0);
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
        Seller seller = Seller.create("SEL-TEST001", "나이키 코리아");
        return sellerRepository.save(seller);
    }

    private Product createProduct(Seller seller) {
        Product product = Product.create(seller, "PRD-TEST001", "Nike Dunk Low");
        return productRepository.save(product);
    }

    private ProductVariant createProductVariant(Product product) {
        ProductVariant productVariant = ProductVariant.create(product, "VRT-TEST001", "Black / 270");
        return productVariantRepository.save(productVariant);
    }

    private Launch createOpenLaunch(Product product, LocalDateTime startAt, LocalDateTime endAt) {
        Launch launch = Launch.create(product, "LCH-TEST001", "Nike Dunk Low 1차 드롭", startAt, endAt);
        launch.open();
        return launchRepository.save(launch);
    }

    private LaunchVariant createLaunchVariant(
            Launch launch, ProductVariant productVariant, BigDecimal salePrice, int totalStock
    ) {
        LaunchVariant launchVariant = LaunchVariant.create(launch, productVariant, salePrice, totalStock);
        return launchVariantRepository.save(launchVariant);
    }

    private Launch createUpcomingLaunch(Product product, LocalDateTime startAt, LocalDateTime endAt) {
        Launch launch = Launch.create(product, "LCH-TEST-UPCOMING", "Nike Dunk Low 예정 드롭", startAt, endAt);
        return launchRepository.save(launch);
    }
}
