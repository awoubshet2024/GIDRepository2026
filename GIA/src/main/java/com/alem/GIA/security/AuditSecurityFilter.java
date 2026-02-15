package com.alem.GIA.security;

import com.alem.GIA.service.AuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuditSecurityFilter extends OncePerRequestFilter {

    private final AuditService auditService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);

            if (response.getStatus() == HttpServletResponse.SC_FORBIDDEN) {
                auditService.logAccess(
                        "ACCESS_DENIED",
                        request.getRequestURI(),
                        request.getMethod(),
                        "DENIED"
                );
            }

        } catch (Exception ex) {
            auditService.logAccess(
                    "REQUEST_ERROR",
                    request.getRequestURI(),
                    request.getMethod(),
                    "ERROR"
            );
            throw ex;
        }
    }
}
