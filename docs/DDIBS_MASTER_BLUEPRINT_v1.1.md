# DDIBS Master Blueprint v1.1

## 0. 문서 목적

이 문서는 DDIBS 프로젝트의 **단일 기준 문서**다.  
기존의 Project Spec, Project Prompt, ERD Draft에 분산되어 있던 기준을 하나로 통합하고, 이후의 설계, 구현, 리팩토링, 문서화, 코드 리뷰, README 작성이 모두 이 문서를 기준으로 진행되도록 한다.

DDIBS는 일반적인 풀스펙 커머스 플랫폼이 아니다.  
DDIBS는 **한정 수량 상품 발매 상황에서 주문, 재고 홀드, 홀드 만료 자동 해제, 결제 확정, 라이트 정산 생성까지를 안정적으로 처리하는 Java/Spring 백엔드 드롭 런치 플랫폼**이다.

이 프로젝트의 핵심 가치는 기능 수가 아니라 다음에 있다.

- 정합성
- 동시성 제어
- 상태 전이의 명확성
- 실무적인 구조
- 설명 가능성
- 일관성

---

## 1. 프로젝트 정체성

### 1.1 프로젝트명
DDIBS

### 1.2 한 줄 소개
DDIBS는 한정 수량 상품의 발매에서 재고 홀드, 만료 자동 해제, 결제 확정, 라이트 정산까지 처리하는 Java/Spring 백엔드 드롭 런치 플랫폼이다.

### 1.3 프로젝트 성격
DDIBS는 상시 판매형 쇼핑몰이 아니다.  
특정 발매 시점에 수요가 집중되는 상품을 안정적으로 처리하기 위한 **드롭 런치 처리 엔진**에 가깝다.

따라서 DDIBS는 아래 문제를 실무적으로 해결하는 데 초점을 둔다.

- 발매 가능 여부 검증
- 주문 생성 시 재고 홀드
- 결제 전 임시 선점 유지
- 결제 실패 또는 홀드 만료 시 자동 해제 및 재고 복구
- 동시 주문 상황에서 과판매 방지
- 주문 확정 건 기준 라이트 정산 생성

### 1.4 포트폴리오 포지셔닝
DDIBS는 “기능이 적은 쇼핑몰”이 아니라, **고수요 발매 상황을 안정적으로 처리하는 특화 백엔드 시스템**으로 포지셔닝한다.

---

## 2. 판단 우선순위

DDIBS 관련 모든 설계와 구현 제안은 아래 순서를 따른다.

1. 정합성
2. 동시성 제어
3. 상태 전이의 명확성
4. 실무적인 구조
5. 설명 가능성
6. 일관성

즉, 기능 수를 늘리는 것보다 아래 질문에 먼저 답할 수 있어야 한다.

- 주문 생성 시 재고 홀드가 정확히 되는가
- 결제 실패/홀드 만료 시 재고가 정확히 복구되는가
- 재고 1건 상황에서 동시에 여러 주문이 들어와도 1건만 성공하는가
- 상태 전이가 정책적으로 보호되는가
- 왜 그렇게 설계했는지 면접에서 설명 가능한가

---

## 3. V1 범위 정의

### 3.1 In Scope
DDIBS V1에서 반드시 다루는 범위는 아래와 같다.

- 회원 가입 / 로그인 / 로그아웃
- ROLE 기반 접근 제어
- Seller 관리
- Product 관리
- ProductVariant 관리
- Launch 관리
- LaunchVariant 관리
- 발매 가능 여부 검증
- 주문 생성
- 재고 홀드
- 결제 모킹
- 결제 성공 / 실패 처리
- 주문 상태 전이
- 홀드 만료 배치
- 재고 복구
- 정산 생성 배치
- 관리자 정산 조회 / 상태 변경
- 예외 처리 표준화
- 테스트
- README / ERD / 시연 시나리오 문서화

### 3.2 Out of Scope
아래 기능은 V1에서 제외한다.

- 장바구니
- 쿠폰 / 포인트
- 리뷰 / 문의
- 배송 / 송장
- 환불 / 교환
- 외부 PG 실제 연동
- 프론트엔드 고도화
- Seller 셀프 서비스 인증 체계
- 복잡한 정산 정책
- 멀티 테넌시
- 마이크로서비스
- Kafka / MQ / K8s
- 과도한 통계 / 추천 / 알림 기능

---

## 4. 핵심 플로우

### 4.1 플로우 1
**발매 조회 → 발매 가능 여부 검증 → 주문 생성(재고 홀드) → 결제 → 주문 확정 → 정산 생성**

#### 목적
아래 항목을 하나의 흐름 안에서 증명한다.

- 정합성
- 트랜잭션 경계
- 상태 전이
- 동시성 제어
- 결제 성공 / 실패 처리
- 정산 생성 연결

#### 시나리오
1. 사용자는 발매 목록 또는 상세를 조회한다.
2. 사용자는 특정 `LaunchVariant`에 대해 주문 생성을 요청한다.
3. 서버는 아래를 검증한다.
   - `Launch` 상태가 `OPEN`인지
   - 현재 시간이 발매 시간 범위 안인지
   - 해당 `LaunchVariant`가 실제 발매 대상인지
   - 가용 재고가 충분한지
4. 조건을 통과하면 서버는 아래를 수행한다.
   - `Order` 생성: `CREATED`
   - `HoldReservation` 생성: `ACTIVE`
   - `expiresAt = now + 10분`
   - `LaunchVariant.availableStock` 감소
5. 사용자는 결제를 요청한다.
6. 서버는 `Payment`를 생성하거나 갱신한다.
   - 결제 진행 상태: `PENDING`
7. 결제 성공 시:
   - `Payment`: `SUCCESS`
   - `Order`: `CONFIRMED`
   - `HoldReservation`: `CONSUMED`
8. 결제 실패 시:
   - `Payment`: `FAILED`
   - `Order`: `PAYMENT_FAILED`
   - `HoldReservation`: `CANCELED`
   - `LaunchVariant.availableStock` 복구
9. 확정 주문은 정산 대상이 되며, 정산 생성 배치가 `Settlement`를 생성한다.

### 4.2 플로우 2
**홀드 만료 배치 → 자동 해제 → 주문 만료 처리 → 재고 복구**

#### 목적
TTL 기반 재고 점유 해제와 운영 자동화 흐름을 보여준다.

#### 시나리오
1. 스케줄러가 일정 주기로 실행된다.
2. 만료 대상 `HoldReservation`을 조회한다.
   - 조건: `holdStatus = ACTIVE`
   - `expiresAt < now`
3. 만료 대상에 대해 아래를 수행한다.
   - `HoldReservation`: `EXPIRED`
   - `Order`가 아직 `CREATED`라면 `HOLD_EXPIRED`
   - `LaunchVariant.availableStock` 복구
4. 처리 결과를 로깅한다.

---

## 5. 핵심 도메인 모델 확정

DDIBS V1의 최종 핵심 엔티티는 아래와 같이 확정한다.

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

### 5.1 Product
판매 대상 상품의 기본 정보다.  
상시 판매의 재고 엔티티가 아니라, 발매의 부모 정보다.

### 5.2 ProductVariant
실제로 주문 가능한 변형 상품 단위다.  
DDIBS V1에서는 기존 `ProductOption` 대신 `ProductVariant`를 채택한다.

### 5.3 Launch
정해진 시간에 상품을 열어 판매하는 발매 이벤트다.  
주문 가능 여부 검증의 핵심 엔티티다.

### 5.4 LaunchVariant
이번 발매에서 실제로 열린 판매 단위다.  
DDIBS의 재고와 발매가를 책임지는 엔티티다.  
재고를 `ProductVariant`가 아니라 `LaunchVariant`가 가진다는 점이 DDIBS 구조의 핵심이다.

### 5.5 HoldReservation
결제 전 일정 시간 동안 재고를 임시 선점하는 레코드다.

### 5.6 Settlement
확정 주문을 기준으로 생성되는 라이트 정산 레코드다.

### 5.7 Seller
`Seller`는 로그인 주체가 아니다.  
V1에서 `Seller`는 상품/발매의 소유 주체이자 정산 귀속 주체로만 사용한다.  
인증 체계는 `USER / ADMIN`만 유지하고, `Seller`는 독립 도메인 엔티티로 둔다.

### 5.8 OrderItem에 대한 결정
DDIBS V1에서는 `OrderItem`을 두지 않는다.  
주문 1건은 하나의 `LaunchVariant`를 대상으로 하는 단일 주문 구조로 간다.

---

## 6. 엔티티 관계 기준

- `Seller` 1 : N `Product`
- `Product` 1 : N `ProductVariant`
- `Product` 1 : N `Launch`
- `Launch` 1 : N `LaunchVariant`
- `ProductVariant` 1 : N `LaunchVariant`
- `Member` 1 : N `Order`
- `LaunchVariant` 1 : N `Order`
- `Seller` 1 : N `Order`
- `Order` 1 : 1 `HoldReservation`
- `Order` 1 : 1 `Payment`
- `Order` 1 : 0..1 `Settlement`
- `Seller` 1 : N `Settlement`

### 관계 해석 원칙

- `ProductVariant`는 실제 판매 단위의 원형이다.
- `LaunchVariant`는 이번 발매에서 열린 실제 판매 단위다.
- `Order`는 `LaunchVariant`를 직접 참조한다.
- `Order`는 정산과 조회 단순화를 위해 `sellerId`를 직접 가진다.
- `Order`는 `productName`, `variantName`, `unitPrice`를 snapshot으로 가진다.

---

## 7. 상태값 정의

### 7.1 LaunchStatus
- `UPCOMING`
- `OPEN`
- `CLOSED`
- `ENDED`
- `CANCELLED`

### 7.2 OrderStatus
- `CREATED`
- `CONFIRMED`
- `PAYMENT_FAILED`
- `HOLD_EXPIRED`

### 7.3 HoldStatus
- `ACTIVE`
- `CONSUMED`
- `CANCELED`
- `EXPIRED`

### 7.4 PaymentStatus
- `PENDING`
- `SUCCESS`
- `FAILED`

### 7.5 SettlementStatus
- `CREATED`
- `CONFIRMED`

---

## 8. 상태 전이 정책

### 8.1 주문 생성
주문은 아래 조건을 모두 만족할 때만 생성 가능하다.

- `Launch` 상태가 `OPEN`
- 현재 시간이 발매 시간 범위 안
- 발매 대상 `LaunchVariant`
- 가용 재고 1 이상

### 8.2 주문 생성 성공 시
- `Order`: `CREATED`
- `HoldReservation`: `ACTIVE`
- `Payment`: 아직 없거나 이후 생성
- `LaunchVariant.availableStock` 감소

### 8.3 결제 성공 시
- `Payment`: `SUCCESS`
- `Order`: `CONFIRMED`
- `HoldReservation`: `CONSUMED`
- 재고는 유지

### 8.4 결제 실패 시
- `Payment`: `FAILED`
- `Order`: `PAYMENT_FAILED`
- `HoldReservation`: `CANCELED`
- 재고 복구

### 8.5 홀드 만료 시
- `HoldReservation`: `EXPIRED`
- `Order`가 `CREATED`라면 `HOLD_EXPIRED`
- 재고 복구

### 8.6 정산 생성 시
- 대상: `CONFIRMED` `Order`
- `Settlement`: `CREATED`

### 8.7 정산 확정 시
- `Settlement`: `CONFIRMED`

---

## 9. 핵심 정책

### 9.1 발매 가능 여부 정책
주문은 아래 조건을 모두 만족해야 한다.

- `Launch.status = OPEN`
- 현재 시간이 `startAt ~ endAt` 범위 안
- `LaunchVariant`가 실제 발매 대상
- `availableStock >= 요청 수량`

### 9.2 재고 정책
V1 기본안은 **홀드 생성 시점 재고 차감**이다.

- 홀드 성공 시 재고 감소
- 결제 실패 시 재고 복구
- 홀드 만료 시 재고 복구
- 결제 성공 시 홀드는 소비 처리되고 재고는 유지

### 9.3 홀드 TTL 정책
기본 TTL은 10분이다.

### 9.4 결제 정책
결제는 V1에서 모킹으로 처리한다.  
핵심은 외부 연동이 아니라 상태 전이와 복구 로직이다.

### 9.5 동시성 정책
재고 1건 상황에서 동시에 여러 주문이 들어오더라도 **1건만 홀드 생성에 성공**해야 한다.  
락 방식은 구현 단계에서 정하되, 보장해야 할 비즈니스 결과는 고정한다.

### 9.6 정산 정책
정산은 주문 확정 직후 동기 계산하지 않고, **정산 생성 배치**가 `CONFIRMED` 주문을 기준으로 생성한다.

---

## 10. 기술 기준

### 10.1 기본 기술 스택
- Java 21
- Spring Boot 4.0.5
- Spring MVC
- Spring Data JPA
- Spring Security
- MySQL 8.4
- Flyway
- Validation
- Actuator
- Gradle
- Scheduler / Batch 성격 작업
- 테스트 코드
- Testcontainers 고려

### 10.2 패키지 구조 기준
도메인 우선 + 내부 계층 분리 구조를 따른다.

- `domain`
- `dto`
- `repository`
- `service`
- `controller`
- `exception`
- `config`
- `common`

### 10.3 구현 원칙
- Request / Response DTO 분리
- DTO는 `record` 중심
- 엔티티는 `@Getter + protected 생성자 + 정적 팩토리 메서드`
- 입력 검증은 `@Valid`
- 예외는 `BusinessException + ErrorCode + GlobalExceptionHandler`
- 트랜잭션 경계를 명확히 둔다
- 상태 변경은 서비스/도메인 정책으로 통제한다
- N+1과 조회 성능을 의식적으로 설계한다
- 테스트 가능성과 설명 가능성을 우선한다

---

## 11. 인증/인가 기준

### 11.1 현재 역할 체계
- `USER`
- `ADMIN`

### 11.2 Member
- 회원 가입 / 로그인 / 로그아웃 주체
- 발매 조회 / 주문 생성 / 결제 요청 주체

### 11.3 Admin
- `Seller` / `Product` / `Launch` 관리
- 정산 조회 / 상태 변경
- 운영성 조회 기능 담당

### 11.4 Seller
- 인증 주체 아님
- 독립 도메인 엔티티
- `Product` 소유 주체
- `Settlement` 귀속 주체

---

## 12. 문서화 / 리뷰 기준

DDIBS 관련 문서는 항상 아래 항목을 설명할 수 있어야 한다.

- 프로젝트 한 줄 소개
- 핵심 플로우 2개
- ERD
- 상태 전이
- 기술 선택 이유
- 동시성 해결 방식
- 트랜잭션 / 정합성 포인트
- 실패 / 복구 시나리오
- 테스트 전략
- 트러블슈팅
- 실행 방법

리뷰 시에는 아래를 우선 확인한다.

- 핵심 플로우를 해치지 않는가
- 상태 전이가 무분별하지 않은가
- 트랜잭션 경계가 명확한가
- 재고/홀드/결제/정산 정합성이 깨질 가능성이 없는가
- 동시성 문제가 숨어 있지 않은가
- 네이밍과 구조가 전체 프로젝트와 통일되는가
- 테스트로 정책이 검증되는가

---

## 13. V1 품질 기준

DDIBS V1은 아래를 만족해야 한다.

- 핵심 플로우 2개 완성
- 실패 / 복구 시나리오 포함
- 동시성 문제 1개 이상 실제 방지
- 예외 처리 표준화
- 테스트 15개 이상
- 로컬 실행 문서화
- ERD 제공
- README에 시연 시나리오 포함
- 기술 선택 이유와 트러블슈팅 정리

---

## 14. 현재 확정 사항

현재 DDIBS에서 이미 완료된 범위는 아래와 같다.

- `application.yml / local / test` 분리
- MySQL `ddibs`, `ddibs_test` 구성
- Actuator health 체크 검증
- `JpaAuditingConfig`, `BaseTimeEntity`
- `ErrorCode`, `BusinessException`, `ErrorResponse`, `GlobalExceptionHandler`
- `SecurityConfig`, `Role`
- `Member` 엔티티 / `MemberRepository`
- 회원가입
- 로그인
- 현재 사용자 조회
- 로그아웃
- Bruno 검증 완료
- DBeaver 확인 완료

즉, 현재부터의 핵심 구현 축은 아래와 같다.

**Seller → Product → ProductVariant → Launch → LaunchVariant → Order/Hold → Payment → Settlement**

---

## 15. 이후 구현 순서

### 15.1 추천 구현 순서
1. Seller
2. Product
3. ProductVariant
4. Launch
5. LaunchVariant
6. Order
7. HoldReservation
8. Payment
9. Settlement
10. 홀드 만료 배치
11. 정산 생성 배치
12. 동시성 테스트
13. README / 시연 시나리오 정리

### 15.2 즉시 다음 작업
- `V2__create_seller.sql`
- `SellerStatus`
- `Seller` 엔티티
- `SellerRepository`

이 순서를 먼저 가는 이유는 `Seller`가 `Product`, `Order`, `Settlement`의 기준점이라 이후 설계를 흔들리지 않게 잡아주기 때문이다.

---

## 16. v1.1에서 정리된 변경 포인트

기존 문서 대비 v1.1에서 명확히 잠그는 변경점은 아래와 같다.

- `ProductOption` 대신 `ProductVariant` 채택
- `LaunchVariant`를 발매 재고/발매가 책임 엔티티로 확정
- `OrderItem` 제거
- `Seller`를 독립 엔티티로 채택
- `Seller`는 로그인 주체가 아님
- 인증 체계는 `USER / ADMIN` 유지
- 단일 `LaunchVariant` 주문 구조 확정
- 텍스트 기준 문서는 이 문서 하나로 통합

---

## 17. 최종 정의

DDIBS는 작은 쇼핑몰이 아니다.  
DDIBS는 **한정 수량 상품의 발매 상황에서 주문, 재고 홀드, 만료 해제, 결제 확정, 정산 생성까지를 안정적으로 처리하는 백엔드 시스템**이다.

이 프로젝트의 핵심은 기능 수를 늘리는 것이 아니라,  
**정합성, 동시성, 상태 전이, 운영 자동화, 설명 가능한 설계를 실제 코드와 테스트로 증명하는 데 있다.**
