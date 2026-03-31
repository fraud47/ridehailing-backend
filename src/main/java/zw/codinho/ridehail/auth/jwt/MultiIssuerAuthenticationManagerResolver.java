package zw.codinho.ridehail.auth.jwt;

import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import java.text.ParseException;
import java.util.Map;

public class MultiIssuerAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

    private final Map<String, AuthenticationManager> authenticationManagers;
    private final BearerTokenResolver bearerTokenResolver = new DefaultBearerTokenResolver();

    public MultiIssuerAuthenticationManagerResolver(Map<String, AuthenticationManager> authenticationManagers) {
        this.authenticationManagers = Map.copyOf(authenticationManagers);
    }

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        String token = bearerTokenResolver.resolve(request);
        if (token == null || token.isBlank()) {
            throw new InvalidBearerTokenException("Missing bearer token");
        }

        String issuer = extractIssuer(token);
        AuthenticationManager authenticationManager = authenticationManagers.get(issuer);
        if (authenticationManager == null) {
            throw new InvalidBearerTokenException("Unsupported token issuer");
        }

        return authenticationManager;
    }

    private String extractIssuer(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getIssuer();
        } catch (ParseException exception) {
            throw new InvalidBearerTokenException("Malformed JWT", exception);
        }
    }
}
