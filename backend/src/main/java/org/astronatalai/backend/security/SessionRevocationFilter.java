package org.astronatalai.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.astronatalai.backend.repository.AuthSessionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Un token firmado sigue siendo válido hasta expirar; esta comprobación añade revocación
 * por sesión (logout o cierre desde el servidor).
 */
@RequiredArgsConstructor
public class SessionRevocationFilter extends OncePerRequestFilter {

    private final AuthSessionRepository authSessionRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/auth/csrf")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            String tokenId = jwtAuthentication.getToken().getId();
            boolean active = tokenId != null && authSessionRepository
                    .existsByTokenIdAndRevokedAtIsNullAndExpiresAtAfter(tokenId, Instant.now());
            if (!active) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/problem+json");
                response.getWriter().write("""
                        {"title":"No autorizado","status":401,"detail":"La sesión fue revocada o expiró"}
                        """);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
