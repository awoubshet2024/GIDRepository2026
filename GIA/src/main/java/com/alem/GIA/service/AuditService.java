package com.alem.GIA.service;

import com.alem.GIA.audit.AuditLog;
import com.alem.GIA.audit.AuditLogSpecification;
import com.alem.GIA.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;


    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public List<AuditLog> search(
            String username,
            String action,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return auditLogRepository.findAll(
                AuditLogSpecification.filter(username, action, from, to)
        );
    }


    private String toJson(Object obj) {
        if (obj == null) return null;

        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }


    /* ==========================
       ACCESS / SECURITY AUDIT
       ========================== */
    public void logAccess(
            String action,
            String resource,
            String httpMethod,
            String status
    ) {
        AuditLog log = new AuditLog();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            log.setUsername(auth.getName());
        }

        log.setAction(action);
        log.setResource(resource);
        log.setHttpMethod(httpMethod);
        log.setStatus(status);

        auditLogRepository.save(log);
    }

    /* ==========================
       ENTITY CHANGE AUDIT
       (already used by Aspect)
       ========================== */
    public void logChange(
            String action,
            String entity,
            String entityId,
            Object before,
            Object after
    ) {
        if (entity == null || entity.isBlank()) {
            return; // 🚫 do NOT create invalid entity logs
        }

        AuditLog log = new AuditLog();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            log.setUsername(auth.getName());
        }


        log.setAction(action);
        log.setEntityName(entity);
        log.setEntityId(entityId);
        log.setBeforeData(toJson(before));
        log.setAfterData(toJson(after));
        log.setStatus("SUCCESS");

        auditLogRepository.save(log);
    }

}




