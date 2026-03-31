package zw.codinho.ridehail.auth.google;

public record GooglePrincipal(
        String subject,
        String emailAddress,
        String fullName,
        boolean emailVerified
) {
}
