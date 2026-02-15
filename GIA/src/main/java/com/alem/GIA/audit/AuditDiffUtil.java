package com.alem.GIA.audit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class AuditDiffUtil {

    public static Map<String, Object> diff(
            Map<String, Object> before,
            Map<String, Object> after
    ) {
        Map<String, Object> changes = new LinkedHashMap<>();

        for (String key : after.keySet()) {
            Object oldVal = before != null ? before.get(key) : null;
            Object newVal = after.get(key);

            if (!Objects.equals(oldVal, newVal)) {
                changes.put(key, Map.of(
                        "before", oldVal,
                        "after", newVal
                ));
            }
        }
        return changes;
    }
}

