package com.gamehub.api.security;

import org.springframework.security.core.Authentication;

public final class AuthUtil {

    private AuthUtil() {
    }

    /** true si el usuario autenticado es ADMINISTRADOR o VENDEDOR. */
    public static boolean esPersonal(Authentication auth) {
        if (auth == null) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMINISTRADOR") || a.getAuthority().equals("ROLE_VENDEDOR"));
    }
}
