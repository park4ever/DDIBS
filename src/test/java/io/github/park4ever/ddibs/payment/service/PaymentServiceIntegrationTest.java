package io.github.park4ever.ddibs.payment.service;

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
import io.github.park4ever.ddibs.order.service.OrderService;
import io.github.park4ever.ddibs.payment.domain.Payment;
import io.github.park4ever.ddibs.payment.domain.PaymentStatus;
import io.github.park4ever.ddibs.payment.dto.RequestPaymentRequest;
import io.github.park4ever.ddibs.payment.dto.RequestPaymentResponse;
import io.github.park4ever.ddibs.payment.repository.PaymentRepository;
import io.github.park4ever.ddibs.product.domain.Product;
import io.github.park4ever.ddibs.product.repository.ProductRepository;
import io.github.park4ever.ddibs.productvariant.domain.ProductVariant;
import io.github.park4ever.ddibs.productvariant.repository.ProductVariantRepository;
import io.github.park4ever.ddibs.seller.domain.Seller;
import io.github.park4ever.ddibs.seller.repository.SellerRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PaymentServiceIntegrationTest {

    @Autowired
    private PaymentService paymentService;

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

    @Autowired
    private PaymentRepository paymentRepository;

    private OrderTestFixture createOrderFixture(int totalStock) {
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

        return new OrderTestFixture(member, order, launchVariant);
    }

    @Test
    @DisplayName("결제 성공 시, Payment는 SUCCESS가 되고 주문 확정 및 홀드 소비가 수행된다.")
    void requestPayment_success() {
        //given
        OrderTestFixture fixture = createOrderFixture(10);

        RequestPaymentRequest request = new RequestPaymentRequest(
                fixture.order.getId(),
                true,
                null
        );

        //when
        RequestPaymentResponse response = paymentService.requestPayment(fixture.member.getId(), request);

        //then
        Payment payment = paymentRepository.findById(response.id()).orElseThrow();
        Order order = orderRepository.findById(fixture.order().getId()).orElseThrow();
        HoldReservation holdReservation = holdReservationRepository.findByOrderId(order.getId()).orElseThrow();
        LaunchVariant launchVariant = launchVariantRepository.findById(fixture.launchVariant().getId()).orElseThrow();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(payment.getAmount()).isEqualByComparingTo(order.getTotalPrice());
        assertThat(payment.getRequestedAt()).isNotNull();
        assertThat(payment.getApprovedAt()).isNotNull();
        assertThat(payment.getFailedAt()).isNull();
        assertThat(payment.getFailureReason()).isNull();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        assertThat(holdReservation.getStatus()).isEqualTo(HoldStatus.CONSUMED);

        //주문 생성 시 10 -> 9 감소, 결제 성공 시 추가 변화 없음
        assertThat(launchVariant.getAvailableStock()).isEqualTo(9);
    }

    @Test
    @DisplayName("결제 실패 시, Payment는 FAILED가 되고 주문 실패, 홀드 취소, 재고 복구가 수행된다.")
    void requestPayment_fail() {
        // given
        OrderTestFixture fixture = createOrderFixture(10);

        RequestPaymentRequest request = new RequestPaymentRequest(
                fixture.order().getId(),
                false,
                "카드 승인 실패"
        );

        // when
        RequestPaymentResponse response = paymentService.requestPayment(fixture.member().getId(), request);

        // then
        Payment payment = paymentRepository.findById(response.id()).orElseThrow();
        Order order = orderRepository.findById(fixture.order().getId()).orElseThrow();
        HoldReservation holdReservation = holdReservationRepository.findByOrderId(order.getId()).orElseThrow();
        LaunchVariant launchVariant = launchVariantRepository.findById(fixture.launchVariant().getId()).orElseThrow();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getRequestedAt()).isNotNull();
        assertThat(payment.getApprovedAt()).isNull();
        assertThat(payment.getFailedAt()).isNotNull();
        assertThat(payment.getFailureReason()).isEqualTo("카드 승인 실패");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);

        assertThat(holdReservation.getStatus()).isEqualTo(HoldStatus.CANCELLED);

        // 주문 생성 시 10 -> 9 감소, 결제 실패 시 다시 10으로 복구
        assertThat(launchVariant.getAvailableStock()).isEqualTo(10);
    }

    @Test
    @DisplayName("이미 결제 요청이 존재하는 주문은 중복 결제 요청할 수 없다.")
    void requestPayment_fail_whenPaymentAlreadyExists() {
        // given
        OrderTestFixture fixture = createOrderFixture(10);

        RequestPaymentRequest firstRequest = new RequestPaymentRequest(
                fixture.order().getId(),
                true,
                null
        );

        paymentService.requestPayment(fixture.member().getId(), firstRequest);

        long paymentCountBefore = paymentRepository.count();

        RequestPaymentRequest secondRequest = new RequestPaymentRequest(
                fixture.order().getId(),
                true,
                null
        );

        // when & then
        assertThatThrownBy(() -> paymentService.requestPayment(fixture.member().getId(), secondRequest))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_ALREADY_EXISTS);

        assertThat(paymentRepository.count()).isEqualTo(paymentCountBefore);
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
                "테스트 셀러 " + suffix
        );

        return sellerRepository.save(seller);
    }

    private Product createProduct(Seller seller) {
        String suffix = uniqueSuffix();

        Product product = Product.create(
                seller,
                "PRD-" + suffix,
                "테스트 상품 " + suffix
        );

        return productRepository.save(product);
    }

    private ProductVariant createProductVariant(Product product) {
        String suffix = uniqueSuffix();

        ProductVariant productVariant = ProductVariant.create(
                product,
                "PVT-" + suffix,
                "BLACK / 270 / " + suffix
        );

        return productVariantRepository.save(productVariant);
    }

    private Launch createOpenLaunch(Product product, LocalDateTime startAt, LocalDateTime endAt) {
        String suffix = uniqueSuffix();

        Launch launch = Launch.create(
                product,
                "LCH-" + suffix,
                "테스트 발매 " + suffix,
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

    private record OrderTestFixture(
            Member member,
            Order order,
            LaunchVariant launchVariant
    ) {
    }
}
