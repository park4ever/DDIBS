package io.github.park4ever.ddibs.common.init;

import io.github.park4ever.ddibs.launch.domain.Launch;
import io.github.park4ever.ddibs.launch.repository.LaunchRepository;
import io.github.park4ever.ddibs.launchvariant.domain.LaunchVariant;
import io.github.park4ever.ddibs.launchvariant.repository.LaunchVariantRepository;
import io.github.park4ever.ddibs.member.domain.Member;
import io.github.park4ever.ddibs.member.repository.MemberRepository;
import io.github.park4ever.ddibs.order.dto.CreateOrderRequest;
import io.github.park4ever.ddibs.order.dto.CreateOrderResponse;
import io.github.park4ever.ddibs.order.service.OrderService;
import io.github.park4ever.ddibs.payment.dto.RequestPaymentRequest;
import io.github.park4ever.ddibs.payment.service.PaymentService;
import io.github.park4ever.ddibs.product.domain.Product;
import io.github.park4ever.ddibs.product.repository.ProductRepository;
import io.github.park4ever.ddibs.productvariant.domain.ProductVariant;
import io.github.park4ever.ddibs.productvariant.repository.ProductVariantRepository;
import io.github.park4ever.ddibs.seller.domain.Seller;
import io.github.park4ever.ddibs.seller.repository.SellerRepository;
import io.github.park4ever.ddibs.settlement.service.SettlementBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LocalDevDataService {

    private static final String ADMIN_EMAIL = "admin@ddibs.local";
    private static final String ADMIN_PASSWORD = "Admin1234!";
    private static final String USER_ONE_EMAIL = "user1@ddibs.local";
    private static final String USER_TWO_EMAIL = "user2@ddibs.local";
    private static final String DEFAULT_USER_PASSWORD = "User1234!";

    private static final String SELLER_ONE_CODE = "SEL-LOCAL-001";
    private static final String SELLER_TWO_CODE = "SEL-LOCAL-002";

    private static final String PRODUCT_ONE_CODE = "PRD-LOCAL-001";
    private static final String PRODUCT_TWO_CODE = "PRD-LOCAL-002";

    private static final String VARIANT_ONE_CODE = "PVT-LOCAL-001";
    private static final String VARIANT_TWO_CODE = "PVT-LOCAL-002";
    private static final String VARIANT_THREE_CODE = "PVT-LOCAL-003";
    private static final String VARIANT_FOUR_CODE = "PVT-LOCAL-004";

    private static final String LAUNCH_ONE_CODE = "LCH-LOCAL-001";
    private static final String LAUNCH_TWO_CODE = "LCH-LOCAL-002";

    private final MemberRepository memberRepository;
    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final LaunchRepository launchRepository;
    private final LaunchVariantRepository launchVariantRepository;

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final SettlementBatchService settlementBatchService;
    private final PasswordEncoder passwordEncoder;

    public void initialize() {
        if (memberRepository.existsByEmail(USER_ONE_EMAIL)) {
            log.info("로컬 개발용 초기 데이터가 이미 존재하므로 생성을 건너뜁니다.");
            return;
        }

        Member admin = ensureAdmin();
        Member userOne = ensureUser(USER_ONE_EMAIL, "로컬 회원 1");
        Member userTwo = ensureUser(USER_TWO_EMAIL, "로컬 회원 2");

        Seller sellerOne = ensureSeller(SELLER_ONE_CODE, "로컬 셀러 1");
        Seller sellerTwo = ensureSeller(SELLER_TWO_CODE, "로컬 셀러 2");

        Product productOne = ensureProduct(sellerOne, PRODUCT_ONE_CODE, "DDIBS 후드 집업");
        Product productTwo = ensureProduct(sellerTwo, PRODUCT_TWO_CODE, "DDIBS 러닝 스니커즈");

        ProductVariant variantOne = ensureProductVariant(productOne, VARIANT_ONE_CODE, "BLACK / M");
        ProductVariant variantTwo = ensureProductVariant(productOne, VARIANT_TWO_CODE, "GRAY / L");
        ProductVariant variantThree = ensureProductVariant(productTwo, VARIANT_THREE_CODE, "WHITE / 270");
        ProductVariant variantFour = ensureProductVariant(productTwo, VARIANT_FOUR_CODE, "BLACK / 275");

        LocalDateTime now = LocalDateTime.now();

        Launch launchOne = ensureOpenLaunch(
                productOne,
                LAUNCH_ONE_CODE,
                "DDIBS 후드 집업 발매",
                now.minusDays(1),
                now.plusDays(7)
        );

        Launch launchTwo = ensureOpenLaunch(
                productTwo,
                LAUNCH_TWO_CODE,
                "DDIBS 러닝 스니커즈 발매",
                now.minusDays(1),
                now.plusDays(7)
        );

        LaunchVariant launchVariantOne = ensureLaunchVariant(
                launchOne,
                variantOne,
                new BigDecimal("89000.00"),
                10
        );

        LaunchVariant launchVariantTwo = ensureLaunchVariant(
                launchOne,
                variantTwo,
                new BigDecimal("92000.00"),
                8
        );

        LaunchVariant launchVariantThree = ensureLaunchVariant(
                launchTwo,
                variantThree,
                new BigDecimal("159000.00"),
                12
        );

        LaunchVariant launchVariantFour = ensureLaunchVariant(
                launchTwo,
                variantFour,
                new BigDecimal("159000.00"),
                12
        );

        createCreatedOrder(userOne, launchVariantOne);
        createConfirmedOrder(userOne, launchVariantTwo);
        createConfirmedOrder(userTwo, launchVariantThree);
        createFailedOrder(userTwo, launchVariantFour, "모킹 결제 실패 - 한도 초과");

        settlementBatchService.generateSettlements();

        log.info(
                "로컬 개발용 초기 데이터 생성 완료 - adminEmail={}, userEmails=[{}, {}]",
                admin.getEmail(),
                userOne.getEmail(),
                userTwo.getEmail()
        );
    }

    private Member ensureAdmin() {
        return memberRepository.findByEmail(ADMIN_EMAIL)
                .orElseGet(() -> memberRepository.save(
                        Member.createAdmin(
                                ADMIN_EMAIL,
                                passwordEncoder.encode(ADMIN_PASSWORD),
                                "로컬 관리자"
                        )
                ));
    }

    private Member ensureUser(String email, String name) {
        return memberRepository.findByEmail(email)
                .orElseGet(() -> memberRepository.save(
                        Member.createUser(
                                email,
                                passwordEncoder.encode(DEFAULT_USER_PASSWORD),
                                name
                        )
                ));
    }

    private Seller ensureSeller(String sellerCode, String name) {
        return sellerRepository.findBySellerCode(sellerCode)
                .orElseGet(() -> sellerRepository.save(
                        Seller.create(sellerCode, name)
                ));
    }

    private Product ensureProduct(Seller seller, String productCode, String name) {
        return productRepository.findByProductCode(productCode)
                .orElseGet(() -> productRepository.save(
                        Product.create(seller, productCode, name)
                ));
    }

    private ProductVariant ensureProductVariant(Product product, String variantCode, String name) {
        return productVariantRepository.findByVariantCode(variantCode)
                .orElseGet(() -> productVariantRepository.save(
                        ProductVariant.create(product, variantCode, name)
                ));
    }

    private Launch ensureOpenLaunch(
            Product product,
            String launchCode,
            String name,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        return launchRepository.findByLaunchCode(launchCode)
                .orElseGet(() -> {
                    Launch launch = Launch.create(product, launchCode, name, startAt, endAt);
                    launch.open();
                    return launchRepository.save(launch);
                });
    }

    private LaunchVariant ensureLaunchVariant(
            Launch launch,
            ProductVariant productVariant,
            BigDecimal salePrice,
            int totalStock
    ) {
        return launchVariantRepository.findByLaunchIdAndProductVariantId(
                        launch.getId(),
                        productVariant.getId()
                )
                .orElseGet(() -> launchVariantRepository.save(
                        LaunchVariant.create(launch, productVariant, salePrice, totalStock)
                ));
    }

    private void createCreatedOrder(Member member, LaunchVariant launchVariant) {
        orderService.createOrder(member.getId(), new CreateOrderRequest(launchVariant.getId()));
    }

    private void createConfirmedOrder(Member member, LaunchVariant launchVariant) {
        CreateOrderResponse createdOrder = orderService.createOrder(
                member.getId(),
                new CreateOrderRequest(launchVariant.getId())
        );

        paymentService.requestPayment(
                member.getId(),
                new RequestPaymentRequest(createdOrder.id(), true, null)
        );
    }

    private void createFailedOrder(Member member, LaunchVariant launchVariant, String failureReason) {
        CreateOrderResponse createdOrder = orderService.createOrder(
                member.getId(),
                new CreateOrderRequest(launchVariant.getId())
        );

        paymentService.requestPayment(
                member.getId(),
                new RequestPaymentRequest(createdOrder.id(), false, failureReason)
        );
    }
}
