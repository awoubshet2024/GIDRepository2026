package com.alem.GIA.audit;


import com.alem.GIA.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminAuditInterceptor implements HandlerInterceptor {

    private final AuditService auditService;

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) return;

        // Only log admin endpoints
        if (!request.getRequestURI().startsWith("/api/admin")) return;

        // Only successful responses
        if (response.getStatus() >= 400) return;

        auditService.logAccess(
                "ADMIN_ACCESS",
                request.getRequestURI(),
                request.getMethod(),
                "SUCCESS"
        );
    }
}
