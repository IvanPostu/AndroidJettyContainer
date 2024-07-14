package org.mortbay.ijetty.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class ToString {
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperProvider.getConfiguredClone();

    private ToString() {
        throw new IllegalStateException();
    }

    public static String create(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Should never happen", e);
        }
    }

}
