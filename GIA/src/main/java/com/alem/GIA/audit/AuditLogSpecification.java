package com.alem.GIA.audit;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogSpecification {

    public static Specification<AuditLog> filter(
            String username,
            String action,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (username != null && !username.isBlank()) {
                predicates.add(cb.equal(root.get("username"), username));
            }

            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(root.get("action"), action));
            }

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("timestamp"), from
                ));
            }

            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("timestamp"), to
                ));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}


