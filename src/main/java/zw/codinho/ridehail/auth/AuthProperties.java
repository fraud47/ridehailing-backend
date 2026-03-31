package zw.codinho.ridehail.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    @NotNull
    private final Jwt jwt = new Jwt();

    @NotNull
    private final Google google = new Google();

    @NotNull
    private final External external = new External();

    public Jwt getJwt() {
        return jwt;
    }

    public Google getGoogle() {
        return google;
    }

    public External getExternal() {
        return external;
    }

    public static class Jwt {

        @NotBlank
        private String issuerUri;

        @NotBlank
        private String secret;

        @NotNull
        private Duration accessTokenTtl = Duration.ofHours(12);

        public String getIssuerUri() {
            return issuerUri;
        }

        public void setIssuerUri(String issuerUri) {
            this.issuerUri = issuerUri;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public Duration getAccessTokenTtl() {
            return accessTokenTtl;
        }

        public void setAccessTokenTtl(Duration accessTokenTtl) {
            this.accessTokenTtl = accessTokenTtl;
        }
    }

    public static class Google {

        private List<String> clientIds = new ArrayList<>();

        public List<String> getClientIds() {
            return clientIds;
        }

        public void setClientIds(List<String> clientIds) {
            this.clientIds = clientIds;
        }
    }

    public static class External {

        @NotBlank
        private String issuerUri;

        @NotBlank
        private String jwkSetUri;

        public String getIssuerUri() {
            return issuerUri;
        }

        public void setIssuerUri(String issuerUri) {
            this.issuerUri = issuerUri;
        }

        public String getJwkSetUri() {
            return jwkSetUri;
        }

        public void setJwkSetUri(String jwkSetUri) {
            this.jwkSetUri = jwkSetUri;
        }
    }
}
