package zw.codinho.ridehail.config;

import jakarta.servlet.http.HttpServletRequest;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import zw.codinho.ridehail.auth.AuthProperties;
import zw.codinho.ridehail.auth.jwt.MultiIssuerAuthenticationManagerResolver;
import zw.codinho.ridehail.security.KeycloakRealmRoleConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/actuator/health",
                                "/api/v1/auth/me",
                                "/api/v1/auth/google/login")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.authenticationManagerResolver(authenticationManagerResolver))
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    @Bean
    AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver(AuthProperties authProperties,
                                                                                    JwtAuthenticationConverter jwtAuthenticationConverter) {
        return new MultiIssuerAuthenticationManagerResolver(Map.of(
                authProperties.getJwt().getIssuerUri(), authenticationManager(localJwtDecoder(authProperties), jwtAuthenticationConverter),
                authProperties.getExternal().getIssuerUri(), authenticationManager(externalJwtDecoder(authProperties), jwtAuthenticationConverter)
        ));
    }

    @Bean
    JwtEncoder jwtEncoder(AuthProperties authProperties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecret(authProperties)));
    }

    private AuthenticationManager authenticationManager(JwtDecoder jwtDecoder,
                                                        JwtAuthenticationConverter jwtAuthenticationConverter) {
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwtDecoder);
        provider.setJwtAuthenticationConverter(jwtAuthenticationConverter);
        return provider::authenticate;
    }

    private JwtDecoder localJwtDecoder(AuthProperties authProperties) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(jwtSecret(authProperties)).build();
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(authProperties.getJwt().getIssuerUri()));
        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }

    private JwtDecoder externalJwtDecoder(AuthProperties authProperties) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(authProperties.getExternal().getJwkSetUri()).build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(authProperties.getExternal().getIssuerUri()));
        return jwtDecoder;
    }

    private SecretKey jwtSecret(AuthProperties authProperties) {
        byte[] secret = authProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(secret, "HmacSHA256");
    }
}
