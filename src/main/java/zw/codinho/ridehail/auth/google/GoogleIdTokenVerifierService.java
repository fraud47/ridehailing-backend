package zw.codinho.ridehail.auth.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Component;
import zw.codinho.ridehail.auth.AuthProperties;
import zw.codinho.ridehail.shared.exception.BadRequestException;

import java.security.GeneralSecurityException;
import java.util.List;

@Component
public class GoogleIdTokenVerifierService implements GoogleIdentityTokenVerifier {

    private final AuthProperties authProperties;

    public GoogleIdTokenVerifierService(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    @Override
    public GooglePrincipal verify(String googleIdToken) {
        List<String> clientIds = authProperties.getGoogle().getClientIds().stream()
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();

        if (clientIds.isEmpty()) {
            throw new BadRequestException("Google sign-in is not configured. Add at least one Google client ID.");
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(clientIds)
                    .build();

            GoogleIdToken idToken = verifier.verify(googleIdToken);
            if (idToken == null) {
                throw new BadRequestException("The supplied Google ID token is invalid");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            Boolean emailVerified = payload.getEmailVerified();

            if (email == null || email.isBlank()) {
                throw new BadRequestException("The Google account does not expose an email address");
            }

            return new GooglePrincipal(
                    payload.getSubject(),
                    email,
                    name == null || name.isBlank() ? email : name,
                    Boolean.TRUE.equals(emailVerified));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Failed to initialize Google token verification", exception);
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Failed to verify the Google ID token", exception);
        }
    }
}
