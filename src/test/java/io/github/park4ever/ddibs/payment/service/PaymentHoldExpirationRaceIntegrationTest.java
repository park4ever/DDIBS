package io.github.park4ever.ddibs.payment.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.exception.ErrorCode;
import io.github.park4ever.ddibs.holdreservation.domain.HoldReservation;
import io.github.park4ever.ddibs.holdreservation.domain.HoldStatus;
import io.github.park4ever.ddibs.holdreservation.repository.HoldReservationRepository;
import io.github.park4ever.ddibs.holdreservation.service.HoldExpirationBatchService;
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
import io.github.park4ever.ddibs.payment.repository.PaymentRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentHoldExpirationRaceIntegrationTest extends MySqlContainerIntegrationTestSupport {

    private static final BigDecimal SALE_PRICE = new BigDecimal("159000.00");

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private HoldExpirationBatchService holdExpirationBatchService;

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

    @Test
    @DisplayName("결제 성공 요청과 홀드 만료 배치가 동시에 실행되어도 최종 상태는 일관되게 수렴한다.")
    void paymentSuccess_and_holdExpiration_race() throws Exception {
        // given
        PendingOrderFixture fixture = createPendingOrderFixture(1);
        expireHoldNow(fixture.order().getId());

        RequestPaymentRequest request = createSuccessRequest(fixture.order().getId());

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicReference<Throwable> paymentThrowable = new AtomicReference<>();
        AtomicReference<Throwable> batchThrowable = new AtomicReference<>();

        Future<?> paymentFuture = executorService.submit(() -> {
            try {
                readyLatch.countDown();
                startLatch.await();

                paymentService.requestPayment(fixture.member().getId(), request);
            } catch (Throwable throwable) {
                paymentThrowable.set(throwable);
            } finally {
                doneLatch.countDown();
            }
        });

        Future<?> batchFuture = executorService.submit(() -> {
            try {
                readyLatch.countDown();
                startLatch.await();

                holdExpirationBatchService.expireHolds();
            } catch (Throwable throwable) {
                batchThrowable.set(throwable);
            } finally {
                doneLatch.countDown();
            }
        });

        boolean allReady = readyLatch.await(5, TimeUnit.SECONDS);
        assertThat(allReady).isTrue();

        // when
        startLatch.countDown();

        boolean allDone = doneLatch.await(10, TimeUnit.SECONDS);

        paymentFuture.get(1, TimeUnit.SECONDS);
        batchFuture.get(1, TimeUnit.SECONDS);

        executorService.shutdown();
        boolean terminated = executorService.awaitTermination(5, TimeUnit.SECONDS);

        // then
        assertThat(allDone).isTrue();
        assertThat(terminated).isTrue();
        assertThat(batchThrowable.get()).isNull();

        if (paymentThrowable.get() != null) {
            assertThat(paymentThrowable.get()).isInstanceOf(BusinessException.class);
            BusinessException exception = (BusinessException) paymentThrowable.get();
            assertThat(exception.getErrorCode())
                    .isIn(expectedPaymentFailureErrorCodes());
        }

        Order savedOrder = orderRepository.findById(fixture.order().getId()).orElseThrow();
        HoldReservation savedHold = holdReservationRepository.findByOrderId(fixture.order().getId()).orElseThrow();
        LaunchVariant savedLaunchVariant = launchVariantRepository.findById(fixture.launchVariant().getId()).orElseThrow();
        Payment savedPayment = paymentRepository.findByOrderId(fixture.order().getId()).orElse(null);

        boolean paymentWon = isPaymentWon(
                paymentThrowable.get(),
                savedPayment,
                savedOrder,
                savedHold,
                savedLaunchVariant
        );

        boolean expirationWon = isExpirationWon(
                paymentThrowable.get(),
                savedPayment,
                savedOrder,
                savedHold,
                savedLaunchVariant
        );

        assertThat(paymentWon || expirationWon).isTrue();
    }

    private PendingOrderFixture createPendingOrderFixture(int totalStock) {
        Member member = createMember("race-user");
        Seller seller = createSeller();
        Product product = createProduct(seller);
        ProductVariant productVariant = createProductVariant(product);

        LocalDateTime now = LocalDateTime.now();
        Launch launch = createOpenLaunch(product, now.minusMinutes(1), now.plusMinutes(30));
        LaunchVariant launchVariant = createLaunchVariant(
                launch,
                productVariant,
                SALE_PRICE,
                totalStock
        );

        CreateOrderResponse orderResponse = orderService.createOrder(
                member.getId(),
                new CreateOrderRequest(launchVariant.getId())
        );

        Order order = orderRepository.findById(orderResponse.id()).orElseThrow();
        return new PendingOrderFixture(member, order, launchVariant);
    }

    private void expireHoldNow(Long orderId) {
        HoldReservation holdReservation = holdReservationRepository.findByOrderId(orderId).orElseThrow();
        ReflectionTestUtils.setField(holdReservation, "expiresAt", LocalDateTime.now().minusSeconds(5));
        holdReservationRepository.saveAndFlush(holdReservation);
    }

    private RequestPaymentRequest createSuccessRequest(Long orderId) {
        return new RequestPaymentRequest(orderId, true, null);
    }

    private Set<ErrorCode> expectedPaymentFailureErrorCodes() {
        return Set.of(
                ErrorCode.INVALID_PAYMENT_ORDER_STATUS,
                ErrorCode.INVALID_HOLD_STATUS_TRANSITION
        );
    }

    private boolean isPaymentWon(
            Throwable paymentThrowable,
            Payment savedPayment,
            Order savedOrder,
            HoldReservation savedHold,
            LaunchVariant savedLaunchVariant
    ) {
        return paymentThrowable == null
                && savedPayment != null
                && savedPayment.getStatus() == PaymentStatus.SUCCESS
                && savedOrder.getStatus() == OrderStatus.CONFIRMED
                && savedHold.getStatus() == HoldStatus.CONSUMED
                && savedLaunchVariant.getAvailableStock() == 0;
    }

    private boolean isExpirationWon(
            Throwable paymentThrowable,
            Payment savedPayment,
            Order savedOrder,
            HoldReservation savedHold,
            LaunchVariant savedLaunchVariant
    ) {
        return paymentThrowable != null
                && savedPayment == null
                && savedOrder.getStatus() == OrderStatus.HOLD_EXPIRED
                && savedHold.getStatus() == HoldStatus.EXPIRED
                && savedLaunchVariant.getAvailableStock() == 1;
    }

    private Member createMember(String prefix) {
        String suffix = uniqueSuffix();

        Member member = Member.createUser(
                prefix + "-" + suffix + "@test.com",
                "encoded-password",
                "경합유저-" + suffix
        );

        return memberRepository.saveAndFlush(member);
    }

    private Seller createSeller() {
        String suffix = uniqueSuffix();

        Seller seller = Seller.create(
                "SEL-" + suffix,
                "경합 테스트 셀러 " + suffix
        );

        return sellerRepository.saveAndFlush(seller);
    }

    private Product createProduct(Seller seller) {
        String suffix = uniqueSuffix();

        Product product = Product.create(
                seller,
                "PRD-" + suffix,
                "경합 테스트 상품 " + suffix
        );

        return productRepository.saveAndFlush(product);
    }

    private ProductVariant createProductVariant(Product product) {
        String suffix = uniqueSuffix();

        ProductVariant productVariant = ProductVariant.create(
                product,
                "PVT-" + suffix,
                "RACE-VARIANT-" + suffix
        );

        return productVariantRepository.saveAndFlush(productVariant);
    }

    private Launch createOpenLaunch(Product product, LocalDateTime startAt, LocalDateTime endAt) {
        String suffix = uniqueSuffix();

        Launch launch = Launch.create(
                product,
                "LCH-" + suffix,
                "경합 테스트 발매 " + suffix,
                startAt,
                endAt
        );
        launch.open();

        return launchRepository.saveAndFlush(launch);
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

        return launchVariantRepository.saveAndFlush(launchVariant);
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
}
