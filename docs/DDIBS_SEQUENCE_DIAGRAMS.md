# DDIBS Sequence Diagrams

## 1. 문서 목적

이 문서는 DDIBS의 핵심 플로우를 Mermaid 시퀀스 다이어그램으로 정리한 문서다.

DDIBS는 일반적인 쇼핑몰이 아니라, 한정 수량 상품 발매 상황에서
**주문 생성 시 재고 홀드**, **홀드 만료 시 자동 해제 및 재고 복구**,
**결제 성공/실패에 따른 상태 전이**, **확정 주문 기준 정산 생성**
까지를 안정적으로 처리하는 백엔드 시스템이다.

따라서 다이어그램도 아래 두 흐름을 중심으로 정리한다.

1. 주문 생성 → 홀드 → 결제 → 주문 확정 → 정산 생성
2. 홀드 만료 배치 → 주문 만료 → 재고 복구

---

## 2. 플로우 1: 주문 생성 → 재고 홀드 → 결제 → 주문 확정 → 정산 생성

```mermaid
sequenceDiagram
    autonumber
    actor User as 사용자
    participant AdminUI as 프론트/클라이언트
    participant OrderAPI as OrderService
    participant LaunchVariant as LaunchVariant
    participant HoldRepo as HoldReservation
    participant PaymentAPI as PaymentService
    participant Payment as Payment
    participant Order as Order
    participant SettlementBatch as SettlementBatchService
    participant Settlement as Settlement

    User->>AdminUI: 발매 상품 선택 후 주문 요청
    AdminUI->>OrderAPI: 주문 생성 요청(launchVariantId)

    OrderAPI->>LaunchVariant: 발매 가능 여부 검증 + 재고 확인
    LaunchVariant-->>OrderAPI: 주문 가능

    OrderAPI->>Order: Order.CREATED 생성
    OrderAPI->>HoldRepo: HoldReservation.ACTIVE 생성(expiresAt = now + 10분)
    OrderAPI->>LaunchVariant: availableStock 감소
    OrderAPI-->>AdminUI: 주문 생성 성공(orderCode)

    User->>AdminUI: 결제 요청
    AdminUI->>PaymentAPI: 결제 요청(orderId, mockSuccess)

    PaymentAPI->>Order: 결제 가능 주문인지 재검증
    PaymentAPI->>HoldRepo: ACTIVE 홀드인지 재검증
    PaymentAPI->>Payment: Payment.PENDING 생성

    alt 결제 성공
        PaymentAPI->>Payment: Payment.SUCCESS
        PaymentAPI->>Order: Order.CONFIRMED
        PaymentAPI->>HoldRepo: HoldReservation.CONSUMED
        PaymentAPI-->>AdminUI: 결제 성공 응답
    else 결제 실패
        PaymentAPI->>Payment: Payment.FAILED
        PaymentAPI->>Order: Order.PAYMENT_FAILED
        PaymentAPI->>HoldRepo: HoldReservation.CANCELED
        PaymentAPI->>LaunchVariant: availableStock 복구
        PaymentAPI-->>AdminUI: 결제 실패 응답
    end

    Note over SettlementBatch,Settlement: 별도 배치에서 확정 주문 기준 정산 생성
    SettlementBatch->>Order: CONFIRMED 주문 후보 조회
    SettlementBatch->>Settlement: Settlement.CREATED 생성
```

### 설명 포인트
- 주문 생성 시점에 홀드를 함께 만들고 재고를 즉시 감소시킨다.
- 결제 성공 시점에는 정산을 직접 생성하지 않고, 확정 주문만 만든다.
- 정산은 별도 배치가 `CONFIRMED` 주문 기준으로 생성한다.
- 결제 실패 시 재고는 즉시 복구된다.

---

## 3. 플로우 2: 홀드 만료 배치 → 자동 해제 → 주문 만료 처리 → 재고 복구

```mermaid
sequenceDiagram
    autonumber
    participant Scheduler as HoldExpirationBatchScheduler
    participant Batch as HoldExpirationBatchService
    participant HoldRepo as HoldReservationRepository
    participant OrderRepo as OrderRepository
    participant Hold as HoldReservation
    participant Order as Order
    participant LaunchVariantRepo as LaunchVariantRepository
    participant LaunchVariant as LaunchVariant

    Scheduler->>Batch: expireHolds() 실행
    Batch->>HoldRepo: 만료 후보 orderId 목록 조회(status=ACTIVE, expiresAt < now)
    HoldRepo-->>Batch: expiredOrderIds

    loop 각 만료 후보 처리
        Batch->>OrderRepo: Order 락 조회
        Batch->>HoldRepo: HoldReservation 락 조회

        alt 주문이 CREATED이고 홀드가 실제 만료 상태
            Batch->>LaunchVariantRepo: LaunchVariant 락 조회
            Batch->>Hold: HoldReservation.EXPIRED
            Batch->>Order: Order.HOLD_EXPIRED
            Batch->>LaunchVariant: availableStock 복구
        else 이미 종결된 주문/홀드
            Batch-->>Batch: 스킵 처리
        end
    end

    Batch-->>Scheduler: 후보 수 / 만료 수 / 스킵 수 반환
```

### 설명 포인트
- 홀드 만료 배치는 후보를 바로 처리하지 않고, **후보 조회 → 락 재조회 → 최신 상태 검증** 구조로 동작한다.
- 이미 결제가 끝났거나 홀드 상태가 바뀐 경우는 스킵한다.
- 실제 만료 처리 시에는 `HoldReservation.EXPIRED`, `Order.HOLD_EXPIRED`, `availableStock 복구`가 함께 일어난다.
- 결과는 단순 count가 아니라 **후보 / 실제 만료 / 스킵** 기준으로 구조화되어 운영 로그와 테스트 설명력이 좋아진다.

---

## 4. 시연 시 연결해서 설명할 포인트

### 플로우 1 설명 시
- 주문 조회 화면에서 `CREATED`, `CONFIRMED`, `PAYMENT_FAILED` 상태를 보여준다.
- 정산 조회 화면에서 `CONFIRMED` 주문 기준으로 생성된 정산을 보여준다.
- 발매/재고 조회 화면에서 재고 감소 결과를 보여준다.

### 플로우 2 설명 시
- 코드 화면에서는 배치 서비스 구조를,
- README/문서에서는 상태 전이와 복구 시나리오를,
- 테스트에서는 경합과 복구 검증을 함께 설명한다.

---

## 5. 한 줄 결론

DDIBS의 시퀀스 다이어그램은 단순 주문/결제 흐름이 아니라,
**고수요 발매 상황에서 재고 홀드, 만료 해제, 결제 상태 전이, 정산 생성이 어떻게 연결되는지**
를 한눈에 보여주기 위한 문서다.
