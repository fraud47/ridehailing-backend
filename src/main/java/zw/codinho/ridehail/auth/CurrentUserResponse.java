package zw.codinho.ridehail.auth;

import java.util.List;

public record CurrentUserResponse(
        boolean anonymous,
        String subject,
        String username,
        String email,
        List<String> authorities
) {
    public static CurrentUserResponse anonymousUser() {
        return new CurrentUserResponse(true, null, null, null, List.of());
    }
}
