package io.github.park4ever.ddibs.order.service;

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
import io.github.park4ever.ddibs.order.dto.admin.AdminOrderSearchRequest;
import io.github.park4ever.ddibs.order.dto.admin.AdminOrderSummaryResponse;
import io.github.park4ever.ddibs.order.repository.OrderRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Transactional
public class AdminOrderQueryServiceIntegrationTest extends MySqlContainerIntegrationTestSupport {

    @Autowired
    private AdminOrderQueryService adminOrderQueryService;

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

    @Test
    @DisplayName("관리자는 상태와 판매자 기준으로 주문 목록을 조회할 수 있다.")
    void getOrders_filterByStatusAndSellerId() {
        //given
        OrderFixture confirmedFixture = createOrderFixture(
                "admin-confirmed",
                "검색 상품 A",
                new BigDecimal("159000.00"),
                true
        );

        createOrderFixture(
                "admin-pending",
                "검색 상품 B",
                new BigDecimal("99000.00"),
                false
        );

        AdminOrderSearchRequest request = new AdminOrderSearchRequest(
                null,
                OrderStatus.CONFIRMED,
                confirmedFixture.order.getSellerId(),
                null,
                null,
                null,
                null,
                null
        );

        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));

        //when
        Page<AdminOrderSummaryResponse> result = adminOrderQueryService.getOrders(request, pageable);

        //then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);

        AdminOrderSummaryResponse response = result.getContent().get(0);
        assertThat(response.orderCode()).isEqualTo(confirmedFixture.order().getOrderCode());
        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.sellerId()).isEqualTo(confirmedFixture.order().getSellerId());
        assertThat(response.memberId()).isEqualTo(confirmedFixture.member().getId());
    }

    @Test
    @DisplayName("관리자는 회원 이메일 키워드와 상품명 키워드로 주문 목록을 조회할 수 있다.")
    void getOrders_filterByMemberEmailKeywordAndProductNameKeyword() {
        // given
        OrderFixture matchedFixture = createOrderFixture(
                "alpha-member",
                "오프화이트 후드",
                new BigDecimal("149000.00"),
                false
        );

        createOrderFixture(
                "beta-member",
                "나이키 팬츠",
                new BigDecimal("89000.00"),
                false
        );

        AdminOrderSearchRequest request = new AdminOrderSearchRequest(
                null,
                null,
                null,
                null,
                "alpha-member",
                "오프화이트",
                null,
                null
        );

        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));

        // when
        Page<AdminOrderSummaryResponse> result = adminOrderQueryService.getOrders(request, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);

        AdminOrderSummaryResponse response = result.getContent().get(0);
        assertThat(response.orderCode()).isEqualTo(matchedFixture.order().getOrderCode());
        assertThat(response.memberEmail()).contains("alpha-member");
        assertThat(response.productName()).contains("오프화이트");
    }

    @Test
    @DisplayName("관리자는 총액 기준 내림차순 정렬과 페이징으로 주문 목록을 조회할 수 있다.")
    void getOrders_pageAndSortByTotalPriceDesc() {
        // given
        createOrderFixture(
                "price-low",
                "가격 상품 LOW",
                new BigDecimal("100000.00"),
                false
        );

        createOrderFixture(
                "price-mid",
                "가격 상품 MID",
                new BigDecimal("200000.00"),
                false
        );

        createOrderFixture(
                "price-high",
                "가격 상품 HIGH",
                new BigDecimal("300000.00"),
                false
        );

        AdminOrderSearchRequest request = new AdminOrderSearchRequest(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        PageRequest pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "totalPrice"));

        // when
        Page<AdminOrderSummaryResponse> result = adminOrderQueryService.getOrders(request, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);

        AdminOrderSummaryResponse first = result.getContent().get(0);
        AdminOrderSummaryResponse second = result.getContent().get(1);

        assertThat(first.totalPrice()).isEqualByComparingTo("300000.00");
        assertThat(second.totalPrice()).isEqualByComparingTo("200000.00");
    }

    private OrderFixture createOrderFixture(
            String memberEmailPrefix,
            String productNameKeyword,
            BigDecimal salePrice,
            boolean confirmed
    ) {
        Member member = createMember(memberEmailPrefix);
        Seller seller = createSeller();
        Product product = createProduct(seller, productNameKeyword);
        ProductVariant productVariant = createProductVariant(product);

        LocalDateTime now = LocalDateTime.now();
        Launch launch = createOpenLaunch(product, now.minusMinutes(1), now.plusMinutes(30));
        LaunchVariant launchVariant = createLaunchVariant(launch, productVariant, salePrice, 10);

        CreateOrderResponse orderResponse = orderService.createOrder(
                member.getId(),
                new CreateOrderRequest(launchVariant.getId())
        );

        Order order = orderRepository.findById(orderResponse.id()).orElseThrow();

        if (confirmed) {
            paymentService.requestPayment(
                    member.getId(),
                    new RequestPaymentRequest(order.getId(), true, null)
            );
            order = orderRepository.findById(order.getId()).orElseThrow();
        }

        return new OrderFixture(member, order);
    }

    private Member createMember(String emailPrefix) {
        String suffix = uniqueSuffix();

        Member member = Member.createUser(
                emailPrefix + "-" + suffix + "@test.com",
                "encoded-password",
                "관리자조회유저-" + suffix
        );

        return memberRepository.saveAndFlush(member);
    }

    private Seller createSeller() {
        String suffix = uniqueSuffix();

        Seller seller = Seller.create(
                "SEL-" + suffix,
                "관리자조회 테스트 셀러 " + suffix
        );

        return sellerRepository.saveAndFlush(seller);
    }

    private Product createProduct(Seller seller, String productNameKeyword) {
        String suffix = uniqueSuffix();

        Product product = Product.create(
                seller,
                "PRD-" + suffix,
                productNameKeyword + " " + suffix
        );

        return productRepository.saveAndFlush(product);
    }

    private ProductVariant createProductVariant(Product product) {
        String suffix = uniqueSuffix();

        ProductVariant productVariant = ProductVariant.create(
                product,
                "PVT-" + suffix,
                "ADMIN-ORDER-VARIANT-" + suffix
        );

        return productVariantRepository.saveAndFlush(productVariant);
    }

    private Launch createOpenLaunch(Product product, LocalDateTime startAt, LocalDateTime endAt) {
        String suffix = uniqueSuffix();

        Launch launch = Launch.create(
                product,
                "LCH-" + suffix,
                "관리자조회 테스트 발매 " + suffix,
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

    private record OrderFixture(
            Member member,
            Order order
    ) {
    }
}
