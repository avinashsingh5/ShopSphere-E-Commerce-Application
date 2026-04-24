package com.shopsphere.catalog.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Validates the X-Service-Token header for internal service-to-service calls.
 *
 * If the header is present and matches the expected secret, a SecurityContext
 * with ROLE_SERVICE authority is set — allowing the request to pass through
 * Spring Security without user-level authentication.
 *
 * If the header is missing or incorrect, this filter does nothing and the
 * request falls through to the normal RoleHeaderFilter / security chain.
 */
@Component
public class ServiceTokenFilter extends OncePerRequestFilter {

    private static final String SERVICE_TOKEN_HEADER = "X-Service-Token";

    @Value("${service.token.secret}")
    private String expectedToken;

    private final RequestAttributeSecurityContextRepository contextRepository =
            new RequestAttributeSecurityContextRepository();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String tokenHeader = request.getHeader(SERVICE_TOKEN_HEADER);

        if (tokenHeader != null && tokenHeader.equals(expectedToken)) {
            // Token is valid — authenticate as an internal service
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_SERVICE")
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("ORDER-SERVICE", null, authorities);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            contextRepository.saveContext(context, request, response);
        }

        filterChain.doFilter(request, response);
    }
}
