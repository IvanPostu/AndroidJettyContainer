package org.mortbay.ijetty.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ObjectMapperProvider {
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        OBJECT_MAPPER = mapper;
    }

    public static ObjectMapper getConfiguredClone() {
        return OBJECT_MAPPER.copy();
    }
}
