package com.alem.GIA.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class SnapshotUtil {

    private final ObjectMapper mapper = new ObjectMapper();

    public <T> T deepCopy(T source, Class<T> clazz) {
        try {
            return mapper.readValue(
                    mapper.writeValueAsString(source),
                    clazz
            );
        } catch (Exception e) {
            return null;
        }
    }
}


