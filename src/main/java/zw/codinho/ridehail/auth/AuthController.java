package zw.codinho.ridehail.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import zw.codinho.ridehail.shared.api.ApiResponse;
import zw.codinho.ridehail.shared.api.ApiResponseFactory;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Authentication and token introspection endpoints for the ride service")
public class AuthController {

    private final GoogleAuthenticationService googleAuthenticationService;

    @PostMapping("/google/login")
    @Operation(summary = "Google login", description = "Verifies a Google ID token, creates the rider if needed, and returns an API JWT")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        AuthTokenResponse response = googleAuthenticationService.authenticate(request);
        String message = response.newUser() ? "Google login successful and rider profile created" : "Google login successful";
        return ApiResponseFactory.ok(message, response);
    }

    @GetMapping("/me")
    @Operation(summary = "Current principal", description = "Returns the authenticated principal or a placeholder when the request is anonymous")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> me(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            return ApiResponseFactory.ok("Anonymous principal", CurrentUserResponse.anonymousUser());
        }

        List<String> roles = jwtAuthenticationToken.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .sorted()
                .toList();

        return ApiResponseFactory.ok(
                "Authenticated principal",
                new CurrentUserResponse(
                        false,
                        jwtAuthenticationToken.getName(),
                        jwtAuthenticationToken.getToken().getClaimAsString("preferred_username"),
                        jwtAuthenticationToken.getToken().getClaimAsString("email"),
                        roles));
    }
}
