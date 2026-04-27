package zw.codinho.ridehail;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import zw.codinho.ridehail.auth.google.GoogleIdentityTokenVerifier;
import zw.codinho.ridehail.auth.google.GooglePrincipal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GoogleAuthenticationIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    JwtDecoder jwtDecoder;

    @MockBean
    GoogleIdentityTokenVerifier googleIdentityTokenVerifier;

    @Test
    void googleLoginCreatesRiderAndReturnsReusableApiJwt() throws Exception {
        when(googleIdentityTokenVerifier.verify("google-token"))
                .thenReturn(new GooglePrincipal("google-subject-1", "new.rider@example.com", "New Rider", true));

        String responseBody = mockMvc.perform(post("/api/v1/auth/google/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idToken": "google-token",
                                  "phoneNumber": "+263771234567"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newUser").value(true))
                .andExpect(jsonPath("$.data.roles[0]").value("RIDER"))
                .andExpect(jsonPath("$.data.rider.emailAddress").value("new.rider@example.com"))
                .andExpect(jsonPath("$.data.driver").isEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode responseJson = objectMapper.readTree(responseBody);
        String accessToken = responseJson.path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.anonymous").value(false))
                .andExpect(jsonPath("$.data.email").value("new.rider@example.com"))
                .andExpect(jsonPath("$.data.authorities[0]").value("ROLE_RIDER"))
                .andExpect(jsonPath("$.data.riderId").isNotEmpty())
                .andExpect(jsonPath("$.data.driverId").isEmpty());
    }

    @Test
    void googleLoginCanIssueDriverAndRiderRolesForTheSameAccount() throws Exception {
        when(googleIdentityTokenVerifier.verify("google-driver-token"))
                .thenReturn(new GooglePrincipal("google-subject-2", "dual.role@example.com", "Dual Role", true));

        String responseBody = mockMvc.perform(post("/api/v1/auth/google/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idToken": "google-driver-token",
                                  "phoneNumber": "+263771111111",
                                  "requestedRoles": ["RIDER", "DRIVER"],
                                  "driver": {
                                    "phoneNumber": "+263772222222",
                                    "licenseNumber": "LIC-2026-001",
                                    "currentLatitude": -17.825166,
                                    "currentLongitude": 31.033510
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.newUser").value(true))
                .andExpect(jsonPath("$.data.roles[0]").value("DRIVER"))
                .andExpect(jsonPath("$.data.roles[1]").value("RIDER"))
                .andExpect(jsonPath("$.data.rider.emailAddress").value("dual.role@example.com"))
                .andExpect(jsonPath("$.data.driver.licenseNumber").value("LIC-2026-001"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode responseJson = objectMapper.readTree(responseBody);
        String accessToken = responseJson.path("data").path("accessToken").asText();

        mockMvc.perform(get("/api/v1/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.anonymous").value(false))
                .andExpect(jsonPath("$.data.email").value("dual.role@example.com"))
                .andExpect(jsonPath("$.data.authorities[0]").value("ROLE_DRIVER"))
                .andExpect(jsonPath("$.data.authorities[1]").value("ROLE_RIDER"))
                .andExpect(jsonPath("$.data.riderId").isNotEmpty())
                .andExpect(jsonPath("$.data.driverId").isNotEmpty());
    }
}
