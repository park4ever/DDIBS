# DDIBS

> **Drop Launch Platform for Consistency, Concurrency, and State Transition**  
> DDIBS는 한정 수량 상품 발매 상황에서 **재고 홀드**, **홀드 만료 자동 해제**, **결제 확정**, **정산 생성**까지를 안정적으로 처리하는 Java/Spring 백엔드 드롭 런치 플랫폼입니다.

---

## 프로젝트 소개

DDIBS는 일반적인 상시 판매형 쇼핑몰이 아닙니다.  
특정 시점에 주문이 몰리는 **고수요 발매 상황**을 가정하고, 아래 문제를 백엔드 관점에서 해결하는 데 집중했습니다.

- 발매 가능 여부 검증
- 주문 생성 시 재고 홀드
- 결제 전 임시 선점 유지
- 결제 실패 또는 홀드 만료 시 자동 해제 및 재고 복구
- 동시 주문 상황에서 과판매 방지
- 확정 주문 기준 정산 생성

즉, DDIBS의 핵심은 기능 수를 늘리는 것이 아니라  
**정합성 · 동시성 제어 · 상태 전이 · 설명 가능한 설계**를 코드와 테스트로 증명하는 데 있습니다.

---

## 왜 이 프로젝트를 만들었는가

일반적인 커머스 포트폴리오는 상품 등록, 주문, 결제 같은 CRUD 중심으로 끝나는 경우가 많습니다.  
하지만 실제 한정판 발매나 선착순 구매 환경에서는 더 중요한 문제가 있습니다.

- 재고 1개일 때 동시에 여러 주문이 들어오면 어떻게 할 것인가?
- 결제 전에 재고를 어떻게 임시 선점할 것인가?
- 결제 실패 또는 결제 미완료 상태가 발생하면 재고를 어떻게 복구할 것인가?
- 확정 주문을 기준으로 정산은 어떻게 분리해서 생성할 것인가?

DDIBS는 이 질문들에 답하기 위해 설계한 프로젝트입니다.

---

## 핵심 플로우

### 1) 주문 생성 → 재고 홀드 → 결제 → 주문 확정 → 정산 생성

```text
발매 조회
→ 발매 가능 여부 검증
→ 주문 생성 (Order.CREATED)
→ 재고 홀드 생성 (HoldReservation.ACTIVE)
→ LaunchVariant.availableStock 감소
→ 결제 요청 (Payment.PENDING)
→ 결제 성공
→ Order.CONFIRMED / HoldReservation.CONSUMED / Payment.SUCCESS
→ 정산 생성 배치
→ Settlement.CREATED
```

### 2) 홀드 만료 배치 → 자동 해제 → 주문 만료 처리 → 재고 복구

```text
만료 대상 HoldReservation 조회
→ HoldReservation.EXPIRED
→ Order.HOLD_EXPIRED
→ LaunchVariant.availableStock 복구
```

---

## 핵심 설계 포인트

### 1. 재고는 ProductVariant가 아니라 LaunchVariant가 가진다
DDIBS에서 실제 주문과 재고 차감의 기준은 `LaunchVariant`입니다.

- `ProductVariant`: 실제 판매 단위의 원형
- `LaunchVariant`: 이번 발매에서 실제로 열린 판매 단위

즉, **이번 발매의 판매가와 재고는 LaunchVariant가 책임집니다.**

### 2. 주문 생성과 홀드 생성은 함께 움직인다
주문이 생성되면 바로 홀드가 생성되고, 가용 재고가 감소합니다.

- `Order.CREATED`
- `HoldReservation.ACTIVE`
- `expiresAt = now + 10min`
- `availableStock` 감소

### 3. 결제와 정산 생성을 분리했다
결제 성공 시점에는

- Payment 성공 처리
- Order 확정
- Hold 소비 처리

까지 수행하고,  
정산은 **별도 배치**가 확정 주문을 대상으로 생성합니다.

이렇게 분리해서 결제 플로우의 책임을 줄이고, 운영 자동화를 더 명확하게 표현했습니다.

### 4. 동시성 처리 방식을 테스트로 검증했다
주문 생성 시 `LaunchVariant`를 비관적 락 기반으로 조회하여, 재고 1건 상황에서 동시에 여러 요청이 들어와도 **1건만 성공하고 과판매가 발생하지 않도록** 설계했습니다.

---

## 기술 스택

- **Java 21**
- **Spring Boot 4.0.5**
- **Spring MVC**
- **Spring Data JPA**
- **Spring Security**
- **MySQL 8.4**
- **Flyway**
- **Actuator**
- **JUnit 5**
- **Gradle**

---

## 도메인 모델

### 핵심 엔티티

- `Member`
- `Seller`
- `Product`
- `ProductVariant`
- `Launch`
- `LaunchVariant`
- `Order`
- `HoldReservation`
- `Payment`
- `Settlement`

### 관계 요약

- Seller 1 : N Product
- Product 1 : N ProductVariant
- Product 1 : N Launch
- Launch 1 : N LaunchVariant
- ProductVariant 1 : N LaunchVariant
- Member 1 : N Order
- LaunchVariant 1 : N Order
- Order 1 : 1 HoldReservation
- Order 1 : 1 Payment
- Order 1 : 0..1 Settlement

---

## 상태 전이

### LaunchStatus
- `UPCOMING`
- `OPEN`
- `CLOSED`
- `ENDED`
- `CANCELLED`

### OrderStatus
- `CREATED`
- `CONFIRMED`
- `PAYMENT_FAILED`
- `HOLD_EXPIRED`

### HoldStatus
- `ACTIVE`
- `CONSUMED`
- `CANCELLED`
- `EXPIRED`

### PaymentStatus
- `PENDING`
- `SUCCESS`
- `FAILED`

### SettlementStatus
- `CREATED`
- `CONFIRMED`

---

## 테스트

DDIBS는 핵심 플로우를 테스트 코드로 검증합니다.

### 주요 테스트 대상

- `OrderService`
    - 주문 생성 성공
    - 주문 불가 상태 실패
    - 재고 부족 실패

- `PaymentService`
    - 결제 성공
    - 결제 실패
    - 중복 결제 요청 방지

- `SettlementBatchService`
    - 확정 주문 정산 생성
    - 중복 정산 생성 방지
    - 미확정 주문 제외

- `HoldExpirationBatchService`
    - 만료된 ACTIVE 홀드 처리
    - ACTIVE가 아닌 홀드 제외
    - 아직 만료되지 않은 홀드 제외

- `OrderConcurrencyIntegrationTest`
    - 재고 1개 상황 동시 주문 시 1건만 성공

---

## 실행 방법

### 1. MySQL 준비
- `ddibs`
- `ddibs_test`

데이터베이스를 생성합니다.

### 2. 환경 변수 설정

```bash
export DDIBS_DB_USERNAME=root
export DDIBS_DB_PASSWORD=your_password
export DDIBS_TEST_DB_USERNAME=root
export DDIBS_TEST_DB_PASSWORD=your_password
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 4. 테스트 실행

```bash
./gradlew test
```

---

## 프로젝트 구조

```text
src/main/java/io/github/park4ever/ddibs
├── auth
├── common
├── config
├── exception
├── holdreservation
├── launch
├── launchvariant
├── member
├── order
├── payment
├── product
├── productvariant
├── seller
└── settlement
```

---

## 문서

상세 기준 문서는 `docs/` 디렉토리에 정리했습니다.

- `docs/DDIBS_MASTER_BLUEPRINT_v1.1.md`

이 문서는 DDIBS의 단일 기준 문서이며,  
프로젝트 정체성, 도메인 구조, 상태 전이, 핵심 플로우, 기술 기준을 통합 정리합니다.

---

## 트러블슈팅 예시

### 1. 주문 생성과 결제 로직을 분리한 이유
결제 성공 시점에 너무 많은 책임을 한 번에 몰아넣지 않고,

- 주문 확정
- 홀드 소비
- 정산 생성

을 분리하기 위해, 정산은 별도 배치로 생성하도록 설계했습니다.

### 2. Hold 만료 시 자동 복구 처리
홀드가 `ACTIVE` 상태에서 만료되면

- `HoldReservation.EXPIRED`
- `Order.HOLD_EXPIRED`
- `LaunchVariant.availableStock` 복구

가 함께 일어나도록 설계해, 실패/복구 시나리오를 명확하게 표현했습니다.

### 3. 동시성 테스트로 과판매 방지 확인
재고 1개인 상황에서 동시 주문 테스트를 작성해,  
1건만 성공하고 나머지는 실패하는지 확인했습니다.

---

## 앞으로의 개선 방향

### V1 마감 후 고려할 수 있는 확장
- 정산 조회 고도화
- 관리자 검색/필터 조건 개선
- Testcontainers 기반 테스트 환경 정리
- 결제/정산 시나리오 고도화
- 운영 로그/모니터링 강화

---

## 한 줄 요약

> DDIBS는 기능 수를 늘리는 커머스 프로젝트가 아니라,  
> **한정 수량 발매 상황에서 재고 홀드, 만료 해제, 결제 확정, 정산 생성까지를 안정적으로 처리하는 백엔드 시스템**입니다.
