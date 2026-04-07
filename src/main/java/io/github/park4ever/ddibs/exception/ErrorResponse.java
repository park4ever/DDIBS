package io.github.park4ever.ddibs.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        int status,
        String code,
        String message,
        String path,
        LocalDateTime timestamp,
        List<ValidationError> errors
) {
    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                path,
                LocalDateTime.now(),
                List.of()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                message,
                path,
                LocalDateTime.now(),
                List.of()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String path, List<ValidationError> errors) {
        return new ErrorResponse(
                errorCode.getStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                path,
                LocalDateTime.now(),
                errors
        );
    }

    public record ValidationError(
            String field,
            Object rejectedValue,
            String reason
    ) {
        public static ValidationError of(String field, Object rejectedValue, String reason) {
            return new ValidationError(field, rejectedValue, reason);
        }
    }
}
