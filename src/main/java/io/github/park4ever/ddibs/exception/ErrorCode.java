package io.github.park4ever.ddibs.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_001", "서버 내부에 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_002", "잘못된 입력입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_003", "지원하지 않는 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_004", "대상을 찾을 수 없습니다."),

    ACCESS_DENIED(HttpStatus.FORBIDDEN, "AUTH_001", "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_002", "인증이 필요합니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_003", "이메일 또는 비밀번호가 올바르지 않습니다."),

    MEMBER_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER_001", "이미 사용 중인 이메일입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_002", "회원을 찾을 수 없습니다."),

    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "SELLER_001", "판매자를 찾을 수 없습니다."),
    SELLER_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SELLER_002", "판매자 코드 생성에 실패했습니다."),

    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_001", "상품을 찾을 수 없습니다."),
    PRODUCT_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PRODUCT_002", "상품 코드 생성에 실패했습니다."),

    PRODUCT_VARIANT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_VARIANT_001", "상품 Variant를 찾을 수 없습니다."),
    PRODUCT_VARIANT_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "PRODUCT_VARIANT_002", "상품 Variant 코드 생성에 실패했습니다."),
    DUPLICATE_PRODUCT_VARIANT_NAME(HttpStatus.CONFLICT, "PRODUCT_VARIANT_003", "같은 상품에 동일한 Variant명이 이미 존재합니다."),
    PRODUCT_INACTIVE(HttpStatus.BAD_REQUEST, "PRODUCT_003", "비활성 상품에는 Variant를 생성할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final  String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
