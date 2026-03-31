package zw.codinho.ridehail;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    JwtDecoder jwtDecoder;

    @Test
    void authProbeAllowsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.anonymous").value(true));
    }

    @Test
    void ridersCannotListDriversWithoutDispatcherRole() throws Exception {
        mockMvc.perform(get("/api/v1/drivers")
                        .with(jwt().jwt(jwt -> jwt
                                .subject("rider-subject")
                                .claim("realm_access", java.util.Map.of("roles", java.util.List.of("RIDER"))))
                                .authorities(createAuthorityList("ROLE_RIDER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void dispatchersCanListDrivers() throws Exception {
        mockMvc.perform(get("/api/v1/drivers")
                        .with(jwt().jwt(jwt -> jwt
                                .subject("dispatcher-subject")
                                .claim("preferred_username", "dispatcher.demo")
                                .claim("realm_access", java.util.Map.of("roles", java.util.List.of("DISPATCHER"))))
                                .authorities(createAuthorityList("ROLE_DISPATCHER"))))
                .andExpect(status().isOk());
    }
}
