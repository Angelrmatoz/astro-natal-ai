package org.astronatalai.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.astronatalai.backend.config.AuthCookieProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Doble envío: el SPA lee la cookie XSRF-TOKEN y la devuelve en X-XSRF-TOKEN.
 * Solo se exige cuando la autenticación viaja por cookie; los clientes con Bearer
 * quedan fuera porque un navegador no añade ese header por sí solo.
 */
@RequiredArgsConstructor
public class CsrfCookieFilter extends OncePerRequestFilter {

    public static final String CSRF_COOKIE = "XSRF-TOKEN";
    public static final String CSRF_HEADER = "X-XSRF-TOKEN";

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final AuthCookieProperties cookieProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SAFE_METHODS.contains(request.getMethod())) {
            ensureCsrfCookie(request, response);
            filterChain.doFilter(request, response);
            return;
        }

        boolean cookieAuth = request.getHeader("Authorization") == null
                && cookieValue(request, cookieProperties.getCookieName()) != null;
        if (!cookieAuth) {
            filterChain.doFilter(request, response);
            return;
        }

        String cookieToken = cookieValue(request, CSRF_COOKIE);
        String headerToken = request.getHeader(CSRF_HEADER);
        if (cookieToken == null || headerToken == null || !constantTimeEquals(cookieToken, headerToken)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/problem+json");
            response.getWriter().write("""
                    {"title":"CSRF","status":403,"detail":"Falta el token CSRF o no coincide"}
                    """);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void ensureCsrfCookie(HttpServletRequest request, HttpServletResponse response) {
        if (cookieValue(request, CSRF_COOKIE) != null) {
            return;
        }
        ResponseCookie cookie = ResponseCookie.from(CSRF_COOKIE, UUID.randomUUID().toString())
                .httpOnly(false)
                .secure(cookieProperties.isSecure())
                .sameSite(cookieProperties.getSameSite())
                .path("/")
                .maxAge(Duration.ofHours(8))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private static String cookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static boolean constantTimeEquals(String left, String right) {
        return MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8));
    }
}
