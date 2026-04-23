package io.github.park4ever.ddibs.order.service;

import io.github.park4ever.ddibs.exception.BusinessException;
import io.github.park4ever.ddibs.holdreservation.repository.HoldReservationRepository;
import io.github.park4ever.ddibs.launch.domain.Launch;
import io.github.park4ever.ddibs.launch.repository.LaunchRepository;
import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;
import io.github.park4ever.ddibs.launchvariant.repository.LaunchVariantRepository;
import io.github.park4ever.ddibs.member.domain.Member;
import io.github.park4ever.ddibs.member.domain.Role;
import io.github.park4ever.ddibs.member.repository.MemberRepository;
import io.github.park4ever.ddibs.order.dto.CreateOrderRequest;
import io.github.park4ever.ddibs.order.repository.OrderRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class OrderConcurrencyIntegrationTest {

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
    @DisplayName("재고가 1개인 발매 상품을 동시에 주문하면 1건만 성공하고 과판매가 발생하지 않는다.")
    void createOrder_concurrently_onlyOneSucceeds() throws Exception {
        //given
        Seller seller = createSeller();
        Product product = createProduct(seller);
        ProductVariant productVariant = createProductVariant(product);

        LocalDateTime now = LocalDateTime.now();
        Launch launch = createOpenLaunch(product, now.minusMinutes(1), now.plusMinutes(30));
        LaunchVariant launchVariant = createLaunchVariant(
                launch,
                productVariant,
                new BigDecimal("159000.00"),
                1
        );

        Member memberA = createMember("concurrency-a");
        Member memberB = createMember("concurrency-b");

        CreateOrderRequest request = new CreateOrderRequest(launchVariant.getId());

        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        ConcurrentLinkedQueue<Throwable> unexpectedExceptions = new ConcurrentLinkedQueue<>();

        Runnable taskA = createOrderTask(
                memberA.getId(),
                request,
                readyLatch,
                startLatch,
                doneLatch,
                successCount,
                failureCount,
                unexpectedExceptions
        );

        Runnable taskB = createOrderTask(
                memberB.getId(),
                request,
                readyLatch,
                startLatch,
                doneLatch,
                successCount,
                failureCount,
                unexpectedExceptions
        );

        executorService.submit(taskA);
        executorService.submit(taskB);

        boolean allReady = readyLatch.await(5, TimeUnit.SECONDS);
        assertThat(allReady).isTrue();

        //when
        startLatch.countDown();

        boolean allDone = doneLatch.await(10, TimeUnit.SECONDS);
        executorService.shutdownNow();

        //then
        assertThat(allDone).isTrue();
        assertThat(unexpectedExceptions).isEmpty();

        LaunchVariant savedLaunchVariant
                = launchVariantRepository.findById(launchVariant.getId()).orElseThrow();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(1);
        assertThat(savedLaunchVariant.getAvailableStock()).isEqualTo(0);
        assertThat(orderRepository.count()).isEqualTo(1);
        assertThat(holdReservationRepository.count()).isEqualTo(1);
    }

    private Runnable createOrderTask(
            Long memberId,
            CreateOrderRequest request,
            CountDownLatch readyLatch,
            CountDownLatch startLatch,
            CountDownLatch doneLatch,
            AtomicInteger successCount,
            AtomicInteger failureCount,
            ConcurrentLinkedQueue<Throwable> unexpectedExceptions
    ) {
        return () -> {
            try {
                readyLatch.countDown();
                startLatch.await();

                orderService.createOrder(memberId, request);
                successCount.incrementAndGet();
            } catch (BusinessException exception) {
                failureCount.incrementAndGet();
            } catch (Throwable throwable) {
                unexpectedExceptions.add(throwable);
            } finally {
                doneLatch.countDown();
            }
        };
    }

    private Member createMember(String prefix) {
        String suffix = uniqueSuffix();

        Member member = Member.createUser(
                prefix + "-" + suffix + "@test.com",
                "encoded-password",
                "동시성유저-" + suffix
        );

        return memberRepository.saveAndFlush(member);
    }

    private Seller createSeller() {
        String suffix = uniqueSuffix();

        Seller seller = Seller.create(
                "SEL-" + suffix,
                "동시성 테스트 셀러 " + suffix
        );

        return sellerRepository.saveAndFlush(seller);
    }

    private Product createProduct(Seller seller) {
        String suffix = uniqueSuffix();

        Product product = Product.create(
                seller,
                "PRD-" + suffix,
                "동시성 테스트 상품 " + suffix
        );

        return productRepository.saveAndFlush(product);
    }

    private ProductVariant createProductVariant(Product product) {
        String suffix = uniqueSuffix();

        ProductVariant productVariant = ProductVariant.create(
                product,
                "PVT-" + suffix,
                "CONCURRENCY-VARIANT-" + suffix
        );

        return productVariantRepository.saveAndFlush(productVariant);
    }

    private Launch createOpenLaunch(Product product, LocalDateTime startAt, LocalDateTime endAt) {
        String suffix = uniqueSuffix();

        Launch launch = Launch.create(
                product,
                "LCH-" + suffix,
                "동시성 테스트 발매 " + suffix,
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
}
