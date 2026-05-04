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
import io.github.park4ever.ddibs.settlement.dto.admin.AdminSettlementSearchRequest;
import io.github.park4ever.ddibs.settlement.dto.admin.AdminSettlementSummaryResponse;
import io.github.park4ever.ddibs.settlement.repository.SettlementRepository;
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
class AdminSettlementQueryServiceIntegrationTest extends MySqlContainerIntegrationTestSupport {

    @Autowired
    private AdminSettlementQueryService adminSettlementQueryService;

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
    @DisplayName("관리자는 정산 상태와 판매자 기준으로 정산 목록을 조회할 수 있다.")
    void searchSettlements_filterByStatusAndSellerId() {
        //given
        SettlementFixture matchedFixture = createSettlementFixture(
                "settlement-status-match",
                new BigDecimal("159000.00"),
                SettlementStatus.CONFIRMED
        );

        createSettlementFixture(
                "settlement-status-created",
                new BigDecimal("129000.00"),
                SettlementStatus.CREATED,
                matchedFixture.order.getSellerId()
        );

        createSettlementFixture(
                "settlement-other-seller",
                new BigDecimal("199000.00"),
                SettlementStatus.CONFIRMED
        );

        AdminSettlementSearchRequest request = new AdminSettlementSearchRequest(
                null,
                null,
                matchedFixture.order.getSellerId(),
                SettlementStatus.CONFIRMED,
                null,
                null
        );

        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));

        //when
        Page<AdminSettlementSummaryResponse> result =
                adminSettlementQueryService.searchSettlements(request, pageable);

        //then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);

        AdminSettlementSummaryResponse response = result.getContent().get(0);
        assertThat(response.id()).isEqualTo(matchedFixture.settlement().getId());
        assertThat(response.sellerId()).isEqualTo(matchedFixture.order().getSellerId());
        assertThat(response.status()).isEqualTo(SettlementStatus.CONFIRMED);
        assertThat(response.orderCode()).isEqualTo(matchedFixture.order().getOrderCode());
    }

    @Test
    @DisplayName("관리자는 정산 코드와 주문 코드 기준으로 정산 목록을 조회할 수 있다.")
    void searchSettlements_filterBySettlementCodeAndOrderCode() {
        // given
        SettlementFixture matchedFixture = createSettlementFixture(
                "settlement-code-match",
                new BigDecimal("179000.00"),
                SettlementStatus.CREATED
        );

        createSettlementFixture(
                "settlement-code-other",
                new BigDecimal("89000.00"),
                SettlementStatus.CREATED
        );

        AdminSettlementSearchRequest request = new AdminSettlementSearchRequest(
                matchedFixture.settlement().getSettlementCode(),
                matchedFixture.order().getOrderCode(),
                null,
                null,
                null,
                null
        );

        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));

        // when
        Page<AdminSettlementSummaryResponse> result =
                adminSettlementQueryService.searchSettlements(request, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);

        AdminSettlementSummaryResponse response = result.getContent().get(0);
        assertThat(response.id()).isEqualTo(matchedFixture.settlement().getId());
        assertThat(response.settlementCode()).isEqualTo(matchedFixture.settlement().getSettlementCode());
        assertThat(response.orderCode()).isEqualTo(matchedFixture.order().getOrderCode());
    }

    @Test
    @DisplayName("관리자는 정산 금액 기준 내림차순 정렬과 페이징으로 정산 목록을 조회할 수 있다.")
    void searchSettlements_pageAndSortBySettlementAmountDesc() {
        // given
        createSettlementFixture(
                "settlement-price-low",
                new BigDecimal("100000.00"),
                SettlementStatus.CREATED
        );

        createSettlementFixture(
                "settlement-price-mid",
                new BigDecimal("200000.00"),
                SettlementStatus.CREATED
        );

        createSettlementFixture(
                "settlement-price-high",
                new BigDecimal("300000.00"),
                SettlementStatus.CREATED
        );

        AdminSettlementSearchRequest request = new AdminSettlementSearchRequest(
                null,
                null,
                null,
                null,
                null,
                null
        );

        PageRequest pageable = PageRequest.of(
                0,
                2,
                Sort.by(Sort.Direction.DESC, "settlementAmount")
        );

        // when
        Page<AdminSettlementSummaryResponse> result =
                adminSettlementQueryService.searchSettlements(request, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);

        AdminSettlementSummaryResponse first = result.getContent().get(0);
        AdminSettlementSummaryResponse second = result.getContent().get(1);

        assertThat(first.settlementAmount()).isEqualByComparingTo("300000.00");
        assertThat(second.settlementAmount()).isEqualByComparingTo("200000.00");
    }

    private SettlementFixture createSettlementFixture(
            String memberEmailPrefix,
            BigDecimal salePrice,
            SettlementStatus settlementStatus
    ) {
        return createSettlementFixture(memberEmailPrefix, salePrice, settlementStatus, null);
    }

    private SettlementFixture createSettlementFixture(
            String memberEmailPrefix,
            BigDecimal salePrice,
            SettlementStatus settlementStatus,
            Long sellerId
    ) {
        Member member = createMember(memberEmailPrefix);
        Seller seller = (sellerId == null) ? createSeller() : findSellerById(sellerId);
        Product product = createProduct(seller);
        ProductVariant productVariant = createProductVariant(product);

        LocalDateTime now = LocalDateTime.now();
        Launch launch = createOpenLaunch(product, now.minusMinutes(1), now.plusMinutes(30));
        LaunchVariant launchVariant = createLaunchVariant(launch, productVariant, salePrice, 10);

        CreateOrderResponse orderResponse = orderService.createOrder(
                member.getId(),
                new CreateOrderRequest(launchVariant.getId())
        );

        Order order = orderRepository.findById(orderResponse.id()).orElseThrow();

        paymentService.requestPayment(
                member.getId(),
                new RequestPaymentRequest(order.getId(), true, null)
        );

        settlementBatchService.generateSettlements();

        Order confirmedOrder = orderRepository.findById(order.getId()).orElseThrow();
        Settlement settlement = settlementRepository.findByOrderId(confirmedOrder.getId()).orElseThrow();

        if (settlementStatus == SettlementStatus.CONFIRMED && settlement.isCreated()) {
            settlement.confirm(LocalDateTime.now());
            settlementRepository.flush();
        }

        return new SettlementFixture(member, confirmedOrder, settlement);
    }

    private Member createMember(String emailPrefix) {
        String suffix = uniqueSuffix();

        Member member = Member.createUser(
                emailPrefix + "-" + suffix + "@test.com",
                "encoded-password",
                "정산조회유저-" + suffix
        );

        return memberRepository.saveAndFlush(member);
    }

    private Seller createSeller() {
        String suffix = uniqueSuffix();

        Seller seller = Seller.create(
                "SEL-" + suffix,
                "정산조회 테스트 셀러 " + suffix
        );

        return sellerRepository.saveAndFlush(seller);
    }

    private Seller findSellerById(Long sellerId) {
        return sellerRepository.findById(sellerId).orElseThrow();
    }

    private Product createProduct(Seller seller) {
        String suffix = uniqueSuffix();

        Product product = Product.create(
                seller,
                "PRD-" + suffix,
                "정산조회 테스트 상품 " + suffix
        );

        return productRepository.saveAndFlush(product);
    }

    private ProductVariant createProductVariant(Product product) {
        String suffix = uniqueSuffix();

        ProductVariant productVariant = ProductVariant.create(
                product,
                "PVT-" + suffix,
                "SETTLEMENT-QUERY-VARIANT-" + suffix
        );

        return productVariantRepository.saveAndFlush(productVariant);
    }

    private Launch createOpenLaunch(Product product, LocalDateTime startAt, LocalDateTime endAt) {
        String suffix = uniqueSuffix();

        Launch launch = Launch.create(
                product,
                "LCH-" + suffix,
                "정산조회 테스트 발매 " + suffix,
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

    private record SettlementFixture(
            Member member,
            Order order,
            Settlement settlement
    ) {
    }
}
