# DDIBS

> **Drop Launch Platform for Consistency, Concurrency, and State Transition**  
> DDIBS는 한정 수량 상품 발매 상황에서 **재고 홀드**, **홀드 만료 자동 해제**, **결제 확정**, **정산 생성**, **관리자 운영 조회**까지를 안정적으로 처리하는 Java/Spring 백엔드 드롭 런치 플랫폼입니다.

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
- 관리자 관점의 주문 조회 및 운영 추적

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
- 관리자 입장에서 주문/정산/배치 결과를 어떻게 추적할 것인가?

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

## 현재 구현 범위

### 1. Testcontainers 기반 MySQL 테스트 환경
기존에는 로컬 MySQL 테스트 DB에 의존하던 통합 테스트를,  
이제는 **Testcontainers + MySQL** 기반으로 실행하도록 정리했습니다.

- 테스트 실행 시 컨테이너 기반 MySQL 자동 기동
- Flyway 마이그레이션 기반 테스트 스키마 구성
- `ddl-auto=create-drop` 대신 `Flyway + validate` 기반 검증
- 로컬 테스트 환경 의존성 감소 및 재현성 강화

### 2. 정산 배치 멱등성 / 운영 로그 개선
정산 배치는 단순히 Settlement를 생성하는 수준에서 끝나지 않고,  
**후보 수 / 생성 수 / 경쟁 스킵 수**를 구조적으로 반환하도록 리팩토링했습니다.

이를 통해

- 중복 생성 방지
- 예외 기반 최종 방어
- 배치 실행 결과 설명 가능성

을 더 명확히 했습니다.

### 3. 홀드 만료 배치 결과 구조화
홀드 만료 배치도 정산 배치와 마찬가지로 결과를 구조화했습니다.

- 후보 수
- 실제 만료 처리 수
- 주문 상태 스킵 수
- 홀드 상태 스킵 수

를 구분해, 운영 로그와 테스트가 더 설명 가능해졌습니다.

### 4. 관리자 주문 조회 API
QueryDSL을 도입해 관리자 주문 조회 기능을 확장했습니다.

지원하는 조회 조건:
- 주문 코드
- 주문 상태
- 판매자 ID
- 회원 ID
- 회원 이메일 키워드
- 상품명 키워드
- 기간(from / to)
- 페이징 / 정렬

즉 DDIBS는 단순히 주문/결제/배치가 동작하는 백엔드를 넘어서,  
**운영자가 실제로 추적 가능한 시스템**으로 보강되었습니다.

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

까지만 수행하고,  
정산은 **별도 배치**가 확정 주문을 대상으로 생성합니다.

이렇게 분리해서 결제 플로우의 책임을 줄이고, 운영 자동화를 더 명확하게 표현했습니다.

### 4. 배치는 결과를 구조적으로 반환한다
정산 생성 배치와 홀드 만료 배치는 단순히 처리 건수만 반환하지 않고,  
**후보 / 성공 / 스킵**을 구분해서 반환합니다.

이를 통해
- 운영 로그 품질
- 멱등성 설명력
- 테스트 가독성

을 함께 높였습니다.

### 5. 동시성과 경합을 테스트로 검증했다
DDIBS는 단순 CRUD 테스트가 아니라, 실제로 문제가 될 수 있는 경합 시나리오를 통합 테스트로 검증합니다.

- 재고 1건 상황 동시 주문
- 결제 성공 요청 vs 홀드 만료 배치 경합

즉, **정합성을 테스트 코드로 증명하는 구조**를 목표로 했습니다.

---

## 기술 스택

- **Java 21**
- **Spring Boot 4.0.5**
- **Spring MVC**
- **Spring Data JPA**
- **Spring Security**
- **QueryDSL**
- **MySQL 8.4**
- **Flyway**
- **Actuator**
- **JUnit 5**
- **Testcontainers**
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

## 관리자 API 예시

### 관리자 주문 조회
`GET /api/admin/orders`

지원 예시:

```text
/api/admin/orders?status=CONFIRMED&page=0&size=20
/api/admin/orders?sellerId=1&memberEmailKeyword=test
/api/admin/orders?productNameKeyword=Dunk&sort=createdAt,desc
```

이 API는 QueryDSL 기반으로 동적 조건 검색 + 페이징 + 정렬을 지원합니다.

---

## 테스트

DDIBS는 핵심 플로우와 운영 시나리오를 통합 테스트로 검증합니다.

### 주요 테스트 대상

- `OrderService`
  - 주문 생성 성공
  - 주문 불가 상태 실패
  - 재고 부족 실패

- `PaymentService`
  - 결제 성공
  - 결제 실패
  - 중복 결제 요청 차단

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

- `PaymentHoldExpirationRaceIntegrationTest`
  - 결제 성공 요청과 홀드 만료 배치가 동시에 실행되어도 최종 상태가 일관되게 수렴하는지 검증

- `AdminOrderQueryServiceIntegrationTest`
  - 관리자 주문 조회 검색 조건
  - 페이징 / 정렬
  - 상태 / 판매자 / 회원 / 상품명 기준 조회

### 테스트 환경
- 테스트는 **Testcontainers 기반 MySQL** 환경에서 실행됩니다.
- 스키마는 **Flyway 마이그레이션**을 기준으로 구성됩니다.
- 테스트 실행 시 Docker가 필요합니다.

---

## 실행 방법

### 1. 애플리케이션용 MySQL 준비
로컬 MySQL에 아래 데이터베이스를 생성합니다.

- `ddibs`

### 2. 환경 변수 설정

```bash
export DDIBS_DB_USERNAME=root
export DDIBS_DB_PASSWORD=your_password
```

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

### 4. 테스트 실행
테스트는 Testcontainers 기반으로 실행되므로, **Docker Desktop 또는 Docker Engine이 실행 중이어야 합니다.**

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
- `docs/DDIBS_V1_FINAL_REVIEW.md`
- `docs/DDIBS_V2_PLAN.md`

이 문서들은 DDIBS의 기준과 각 단계의 정리 내용을 담고 있습니다.

---

## 트러블슈팅 예시

### 1. 결제와 정산 생성을 분리한 이유
결제 성공 시점에 너무 많은 책임을 한 번에 몰아넣지 않고,

- 주문 확정
- 홀드 소비
- 정산 생성

을 분리하기 위해, 정산은 별도 배치로 생성하도록 설계했습니다.

### 2. 홀드 만료 시 자동 복구 처리
홀드가 `ACTIVE` 상태에서 만료되면

- `HoldReservation.EXPIRED`
- `Order.HOLD_EXPIRED`
- `LaunchVariant.availableStock` 복구

가 함께 일어나도록 설계해, 실패/복구 시나리오를 명확하게 표현했습니다.

### 3. 동시성 테스트로 과판매 방지 확인
재고 1개인 상황에서 동시 주문 테스트를 작성해,  
1건만 성공하고 나머지는 실패하는지 확인했습니다.

### 4. 결제 vs 홀드 만료 배치 경합 처리
결제 성공 요청과 홀드 만료 배치가 거의 동시에 실행될 수 있는 경합 시나리오를 별도 통합 테스트로 검증했습니다.  
이를 통해 최종 상태가 **결제 성공 종결 상태** 또는 **홀드 만료 종결 상태** 중 하나로만 수렴하는지 확인했습니다.

### 5. Testcontainers 도입 후 테스트 재현성 개선
기존에는 로컬 테스트 DB 환경에 영향을 받을 수 있었지만,  
Testcontainers + MySQL + Flyway 조합으로 테스트를 정리하면서 환경 재현성과 신뢰도를 높였습니다.

---

## 한 줄 요약

> DDIBS는 기능 수를 늘리는 커머스 프로젝트가 아니라,  
> **한정 수량 발매 상황에서 재고 홀드, 만료 해제, 결제 확정, 정산 생성, 관리자 운영 조회까지를 안정적으로 처리하는 백엔드 시스템**입니다.
