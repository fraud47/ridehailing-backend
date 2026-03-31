package zw.codinho.ridehail.shared.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;

public final class ApiResponseFactory {

    private ApiResponseFactory() {
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return respond(HttpStatus.OK, message, data);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return respond(HttpStatus.CREATED, message, data);
    }

    public static ResponseEntity<ApiResponse<Void>> noContent(String message) {
        return respond(HttpStatus.OK, message, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> respond(HttpStatus status, String message, T data) {
        return ResponseEntity.status(status)
                .body(new ApiResponse<>(true, message, data, OffsetDateTime.now()));
    }
}
