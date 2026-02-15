package com.alem.GIA.aop;

import com.alem.GIA.annotation.Auditable;
import com.alem.GIA.audit.AuditDiffUtil;
import com.alem.GIA.model.MemberResponse;
import com.alem.GIA.service.AuditService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Map;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;
    private final ObjectMapper objectMapper; // Injected Spring bean

    // Reusable ObjectMapper for audit snapshots
    private final ObjectMapper auditMapper = configureAuditMapper();

    @Around("@annotation(auditable)")
    public Object auditAround(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {

        Object result;
        Map<String, Object> beforeMap = null;
        Map<String, Object> afterMap = null;
        Map<String, Object> diff;

        // 1. CAPTURE BEFORE
        try {
            if (auditable.captureBefore()) {
                Object entityArg = extractEntity(joinPoint.getArgs());
                if (entityArg != null) {
                    beforeMap = deepCopy(entityArg);
                }
            }
        } catch (Exception e) {
            log.error("Audit 'before' failed: {}", e.getMessage());
        }

        // 2. EXECUTE BUSINESS METHOD
        result = joinPoint.proceed();

        // 3. CAPTURE AFTER & LOG CHANGE
        try {
            Object actualBody = unwrapResult(result);

            if (auditable.captureAfter() && actualBody != null) {
                afterMap = deepCopy(actualBody);
            }

            String entityId = extractEntityId(actualBody, auditable.idField());

            String resolvedAction = resolveAction(beforeMap, afterMap, auditable.entity());
            if (resolvedAction == null) return result;

            Map<String, Object> dataToStore;

            if (resolvedAction.startsWith("CREATE")) {
                if (afterMap == null || afterMap.isEmpty()) return result;
                dataToStore = afterMap;
            } else { // UPDATE or DELETE
                if (beforeMap == null || afterMap == null) return result;
                diff = AuditDiffUtil.diff(beforeMap, afterMap);
                if (diff == null || diff.isEmpty()) return result;
                dataToStore = diff;
            }

            auditService.logChange(
                    resolvedAction,
                    auditable.entity(),
                    entityId,
                    resolvedAction.startsWith("CREATE") ? null : beforeMap,
                    dataToStore
            );

        } catch (Exception e) {
            log.error("Audit logging failed after successful transaction", e);
        }

        return result;
    }

    // --- Helpers ---

    // Extract the main entity from method arguments
    private Object extractEntity(Object[] args) {
        for (Object arg : args) {
            if (arg != null && arg.getClass().getSimpleName().equals("Member")) {
                return arg;
            }
        }
        return null;
    }

    // Unwrap ResponseEntity or MemberResponse
    private Object unwrapResult(Object result) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            result = responseEntity.getBody();
        }
        if (result instanceof MemberResponse res) {
            result = res.getMember();
        }
        return result;
    }

    // Resolve action type based on before/after snapshots
    private String resolveAction(Map<String, Object> beforeMap, Map<String, Object> afterMap, String entityName) {
        if (beforeMap == null && afterMap != null) return "CREATE_" + entityName.toUpperCase();
        if (beforeMap != null && afterMap != null) return "UPDATE_" + entityName.toUpperCase();
        if (beforeMap != null) return "DELETE_" + entityName.toUpperCase();
        return null; // nothing to log
    }

    // Extract entity ID safely using reflection
    private String extractEntityId(Object entity, String fieldName) {
        if (entity == null || fieldName == null || fieldName.isBlank()) return null;

        try {
            if (entity instanceof MemberResponse res && res.getMember() != null) {
                return String.valueOf(res.getMember().getMemberId());
            }

            Field field = findField(entity.getClass(), fieldName);
            if (field != null) {
                field.setAccessible(true);
                Object value = field.get(entity);
                return value != null ? value.toString() : null;
            }
        } catch (Exception e) {
            log.warn("Could not extract entity ID for auditing: {}", e.getMessage());
        }
        return null;
    }

    // Find field in class hierarchy
    private Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), fieldName);
            }
            return null;
        }
    }

    // Create a snapshot copy of object as Map
    private Map<String, Object> deepCopy(Object source) {
        if (source == null) return null;
        try {
            return auditMapper.convertValue(source, Map.class);
        } catch (Exception e) {
            log.error("Audit snapshot failed: {}", e.getMessage());
            return null;
        }
    }

    // Configure reusable ObjectMapper for audit
    private static ObjectMapper configureAuditMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        Hibernate5JakartaModule module = new Hibernate5JakartaModule();
        module.configure(Hibernate5JakartaModule.Feature.FORCE_LAZY_LOADING, false);
        mapper.registerModule(module);

        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }
}
