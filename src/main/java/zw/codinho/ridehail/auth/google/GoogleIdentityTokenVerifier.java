package zw.codinho.ridehail.auth.google;

public interface GoogleIdentityTokenVerifier {

    GooglePrincipal verify(String googleIdToken);
}
