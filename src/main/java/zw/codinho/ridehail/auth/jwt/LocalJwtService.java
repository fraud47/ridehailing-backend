package zw.codinho.ridehail.auth.jwt;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import zw.codinho.ridehail.auth.AuthProperties;
import zw.codinho.ridehail.auth.domain.AuthAccount;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class LocalJwtService {

    private final JwtEncoder jwtEncoder;
    private final AuthProperties authProperties;

    public LocalJwtService(JwtEncoder jwtEncoder, AuthProperties authProperties) {
        this.jwtEncoder = jwtEncoder;
        this.authProperties = authProperties;
    }

    public TokenResult issueToken(AuthAccount account) {
        OffsetDateTime issuedAt = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiresAt = issuedAt.plus(authProperties.getJwt().getAccessTokenTtl());

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(authProperties.getJwt().getIssuerUri())
                .issuedAt(issuedAt.toInstant())
                .expiresAt(expiresAt.toInstant())
                .subject(account.getId().toString())
                .claim("email", account.getEmailAddress())
                .claim("preferred_username", account.getEmailAddress())
                .claim("name", account.getDisplayName())
                .claim("roles", List.of(account.getRole().name()))
                .claim("provider", account.getProvider().name())
                .claim("provider_subject", account.getProviderSubject());

        if (account.getRider() != null) {
            claims.claim("rider_id", account.getRider().getId().toString());
        }

        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(),
                claims.build()))
                .getTokenValue();

        return new TokenResult(token, expiresAt);
    }

    public record TokenResult(String accessToken, OffsetDateTime expiresAt) {
    }
}
