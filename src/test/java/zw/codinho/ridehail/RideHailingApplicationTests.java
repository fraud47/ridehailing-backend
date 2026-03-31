package zw.codinho.ridehail;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import zw.codinho.ridehail.auth.google.GoogleIdentityTokenVerifier;

@SpringBootTest
@ActiveProfiles("test")
class RideHailingApplicationTests {

    @MockBean
    JwtDecoder jwtDecoder;

    @MockBean
    GoogleIdentityTokenVerifier googleIdentityTokenVerifier;

    @Test
    void contextLoads() {
    }
}
