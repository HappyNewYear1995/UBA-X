package com.ubax.common;

import com.ubax.system.AuditService;
import com.ubax.system.OnlineUserRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final OnlineUserRegistry onlineUserRegistry;

    public JwtAuthenticationFilter(JwtService jwtService, OnlineUserRegistry onlineUserRegistry) {
        this.jwtService = jwtService;
        this.onlineUserRegistry = onlineUserRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                TenantContext.Principal principal = jwtService.parse(token);
                TenantContext.set(principal);
                String ip = AuditService.clientIp(request);
                onlineUserRegistry.touch(principal.getUserId(), principal.getUsername(), principal.getTenantId(),
                        ip, AuditService.locate(ip), request.getHeader("User-Agent"));
                List<SimpleGrantedAuthority> authorities = principal.getRoles() == null ? List.of()
                        : principal.getRoles().stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                            .collect(Collectors.toList());
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        principal, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                // invalid token -> remain anonymous
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }
}
