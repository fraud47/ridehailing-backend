package zw.codinho.ridehail.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<String> roles = new ArrayList<>(jwt.getClaimAsStringList("roles") == null
                ? List.of()
                : jwt.getClaimAsStringList("roles"));

        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            Object rolesClaim = realmAccess.get("roles");
            if (rolesClaim instanceof List<?> keycloakRoles) {
                keycloakRoles.stream()
                        .filter(String.class::isInstance)
                        .map(String.class::cast)
                        .forEach(roles::add);
            }
        }

        if (roles.isEmpty()) {
            return Collections.emptyList();
        }

        return roles.stream()
                .filter(String.class::isInstance)
                .map(String::trim)
                .filter(role -> !role.isBlank())
                .map(String::toUpperCase)
                .map(role -> "ROLE_" + role)
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }
}
