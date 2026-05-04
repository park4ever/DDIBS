package io.github.park4ever.ddibs.launch.service;

import io.github.park4ever.ddibs.launch.domain.Launch;
import io.github.park4ever.ddibs.launch.domain.LaunchStatus;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchDetailResponse;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchSearchRequest;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchSummaryResponse;
import io.github.park4ever.ddibs.launch.dto.admin.AdminLaunchVariantStockResponse;
import io.github.park4ever.ddibs.launch.repository.LaunchRepository;
import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;
import io.github.park4ever.ddibs.launchvariant.repository.LaunchVariantRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class AdminLaunchQueryServiceIntegrationTest extends MySqlContainerIntegrationTestSupport {

    @Autowired
    private AdminLaunchQueryService adminLaunchQueryService;

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

    @Test
    @DisplayName("관리자는 발매 상태와 판매자 기준으로 발매 목록을 조회할 수 있다.")
    void searchLaunches_filterByStatusAndSellerId() {
        // given
        LaunchFixture matchedFixture = createLaunchFixture(
                "launch-status-match",
                "조던 발매 상품",
                LaunchStatus.OPEN,
                null,
                new VariantStock("BLACK / 270", new BigDecimal("159000.00"), 10, 7),
                new VariantStock("WHITE / 275", new BigDecimal("169000.00"), 5, 3)
        );

        createLaunchFixture(
                "launch-status-upcoming",
                "조던 발매 상품",
                LaunchStatus.UPCOMING,
                matchedFixture.seller().getId(),
                new VariantStock("RED / 260", new BigDecimal("149000.00"), 8, 8)
        );

        createLaunchFixture(
                "launch-status-other-seller",
                "다른 셀러 상품",
                LaunchStatus.OPEN,
                null,
                new VariantStock("NAVY / 280", new BigDecimal("179000.00"), 12, 11)
        );

        AdminLaunchSearchRequest request = new AdminLaunchSearchRequest(
                null,
                LaunchStatus.OPEN,
                matchedFixture.seller().getId(),
                null,
                null,
                null
        );

        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));

        // when
        Page<AdminLaunchSummaryResponse> result =
                adminLaunchQueryService.searchLaunches(request, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);

        AdminLaunchSummaryResponse response = result.getContent().get(0);

        assertThat(response.id()).isEqualTo(matchedFixture.launch().getId());
        assertThat(response.launchCode()).isEqualTo(matchedFixture.launch().getLaunchCode());
        assertThat(response.status()).isEqualTo(LaunchStatus.OPEN);
        assertThat(response.sellerId()).isEqualTo(matchedFixture.seller().getId());
        assertThat(response.productName()).contains("조던");
        assertThat(response.variantCount()).isEqualTo(2L);
        assertThat(response.totalStock()).isEqualTo(15L);
        assertThat(response.availableStock()).isEqualTo(10L);
    }

    @Test
    @DisplayName("관리자는 launchCode와 상품명 키워드 기준으로 발매 목록을 조회할 수 있다.")
    void searchLaunches_filterByLaunchCodeAndProductNameKeyword() {
        // given
        LaunchFixture matchedFixture = createLaunchFixture(
                "launch-code-match",
                "오프화이트 후드",
                LaunchStatus.OPEN,
                null,
                new VariantStock("BLACK / M", new BigDecimal("189000.00"), 4, 4)
        );

        createLaunchFixture(
                "launch-code-other",
                "나이키 팬츠",
                LaunchStatus.OPEN,
                null,
                new VariantStock("GRAY / L", new BigDecimal("99000.00"), 6, 6)
        );

        AdminLaunchSearchRequest request = new AdminLaunchSearchRequest(
                matchedFixture.launch().getLaunchCode(),
                null,
                null,
                "오프화이트",
                null,
                null
        );

        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id"));

        // when
        Page<AdminLaunchSummaryResponse> result =
                adminLaunchQueryService.searchLaunches(request, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);

        AdminLaunchSummaryResponse response = result.getContent().get(0);
        assertThat(response.id()).isEqualTo(matchedFixture.launch().getId());
        assertThat(response.launchCode()).isEqualTo(matchedFixture.launch().getLaunchCode());
        assertThat(response.productName()).contains("오프화이트");
    }

    @Test
    @DisplayName("관리자는 발매 시작일 기준 내림차순 정렬과 페이징으로 발매 목록을 조회할 수 있다.")
    void searchLaunches_pageAndSortByStartAtDesc() {
        // given
        createLaunchFixture(
                "launch-page-low",
                "페이지 상품 LOW",
                LaunchStatus.OPEN,
                null,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().plusDays(1),
                new VariantStock("LOW / 260", new BigDecimal("100000.00"), 3, 3)
        );

        createLaunchFixture(
                "launch-page-mid",
                "페이지 상품 MID",
                LaunchStatus.OPEN,
                null,
                LocalDateTime.now().minusDays(2),
                LocalDateTime.now().plusDays(2),
                new VariantStock("MID / 270", new BigDecimal("200000.00"), 4, 4)
        );

        createLaunchFixture(
                "launch-page-high",
                "페이지 상품 HIGH",
                LaunchStatus.OPEN,
                null,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(3),
                new VariantStock("HIGH / 280", new BigDecimal("300000.00"), 5, 5)
        );

        AdminLaunchSearchRequest request = new AdminLaunchSearchRequest(
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
                Sort.by(Sort.Direction.DESC, "startAt")
        );

        // when
        Page<AdminLaunchSummaryResponse> result =
                adminLaunchQueryService.searchLaunches(request, pageable);

        // then
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);

        AdminLaunchSummaryResponse first = result.getContent().get(0);
        AdminLaunchSummaryResponse second = result.getContent().get(1);

        assertThat(first.productName()).contains("HIGH");
        assertThat(second.productName()).contains("MID");
    }

    @Test
    @DisplayName("관리자는 발매 상세 조회 시 LaunchVariant별 재고 현황을 함께 확인할 수 있다.")
    void getLaunchDetail_includesVariantStocks() {
        // given
        LaunchFixture fixture = createLaunchFixture(
                "launch-detail",
                "상세 조회 상품",
                LaunchStatus.OPEN,
                null,
                new VariantStock("BLACK / 270", new BigDecimal("159000.00"), 10, 8),
                new VariantStock("WHITE / 275", new BigDecimal("169000.00"), 6, 2)
        );

        // when
        AdminLaunchDetailResponse response =
                adminLaunchQueryService.getLaunchDetail(fixture.launch().getId());

        // then
        assertThat(response.id()).isEqualTo(fixture.launch().getId());
        assertThat(response.launchCode()).isEqualTo(fixture.launch().getLaunchCode());
        assertThat(response.launchName()).isEqualTo(fixture.launch().getName());
        assertThat(response.sellerId()).isEqualTo(fixture.seller().getId());
        assertThat(response.productId()).isEqualTo(fixture.product().getId());
        assertThat(response.productName()).isEqualTo(fixture.product().getName());

        assertThat(response.variants()).hasSize(2);

        AdminLaunchVariantStockResponse firstVariant = response.variants().get(0);
        AdminLaunchVariantStockResponse secondVariant = response.variants().get(1);

        assertThat(firstVariant.variantName()).isEqualTo("BLACK / 270");
        assertThat(firstVariant.totalStock()).isEqualTo(10);
        assertThat(firstVariant.availableStock()).isEqualTo(8);

        assertThat(secondVariant.variantName()).isEqualTo("WHITE / 275");
        assertThat(secondVariant.totalStock()).isEqualTo(6);
        assertThat(secondVariant.availableStock()).isEqualTo(2);
    }

    private LaunchFixture createLaunchFixture(
            String launchNamePrefix,
            String productName,
            LaunchStatus launchStatus,
            Long sellerId,
            VariantStock... variantStocks
    ) {
        LocalDateTime now = LocalDateTime.now();
        return createLaunchFixture(
                launchNamePrefix,
                productName,
                launchStatus,
                sellerId,
                now.minusMinutes(1),
                now.plusMinutes(30),
                variantStocks
        );
    }

    private LaunchFixture createLaunchFixture(
            String launchNamePrefix,
            String productName,
            LaunchStatus launchStatus,
            Long sellerId,
            LocalDateTime startAt,
            LocalDateTime endAt,
            VariantStock... variantStocks
    ) {
        Seller seller = (sellerId == null) ? createSeller() : findSellerById(sellerId);
        Product product = createProduct(seller, productName);

        String suffix = uniqueSuffix();
        Launch launch = Launch.create(
                product,
                "LCH-" + suffix,
                launchNamePrefix + "-" + suffix,
                startAt,
                endAt
        );

        applyLaunchStatus(launch, launchStatus);
        Launch savedLaunch = launchRepository.saveAndFlush(launch);

        for (VariantStock variantStock : variantStocks) {
            ProductVariant savedProductVariant = createProductVariant(product, variantStock.variantName());

            LaunchVariant savedLaunchVariant = LaunchVariant.create(
                    savedLaunch,
                    savedProductVariant,
                    variantStock.salePrice(),
                    variantStock.totalStock()
            );

            int restoreQuantity = variantStock.totalStock() - variantStock.availableStock();
            if (restoreQuantity > 0) {
                savedLaunchVariant.decreaseAvailableStock(restoreQuantity);
            }

            launchVariantRepository.saveAndFlush(savedLaunchVariant);
        }

        return new LaunchFixture(seller, product, savedLaunch);
    }

    private void applyLaunchStatus(Launch launch, LaunchStatus status) {
        switch (status) {
            case UPCOMING -> {
                // 기본 상태 유지
            }
            case OPEN -> launch.open();
            case CLOSED -> {
                launch.open();
                launch.close();
            }
            case ENDED -> launch.end();
            case CANCELLED -> launch.cancel();
        }
    }

    private Seller createSeller() {
        String suffix = uniqueSuffix();

        Seller seller = Seller.create(
                "SEL-" + suffix,
                "발매조회 테스트 셀러 " + suffix
        );

        return sellerRepository.saveAndFlush(seller);
    }

    private Seller findSellerById(Long sellerId) {
        return sellerRepository.findById(sellerId).orElseThrow();
    }

    private Product createProduct(Seller seller, String productName) {
        String suffix = uniqueSuffix();

        Product product = Product.create(
                seller,
                "PRD-" + suffix,
                productName + " " + suffix
        );

        return productRepository.saveAndFlush(product);
    }

    private ProductVariant createProductVariant(Product product, String variantName) {
        String suffix = uniqueSuffix();

        ProductVariant productVariant = ProductVariant.create(
                product,
                "PVT-" + suffix,
                variantName
        );

        return productVariantRepository.saveAndFlush(productVariant);
    }

    private String uniqueSuffix() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private record LaunchFixture(
            Seller seller,
            Product product,
            Launch launch
    ) {
    }

    private record VariantStock(
            String variantName,
            BigDecimal salePrice,
            int totalStock,
            int availableStock
    ) {
    }
}
