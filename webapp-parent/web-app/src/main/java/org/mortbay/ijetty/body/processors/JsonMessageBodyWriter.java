package org.mortbay.ijetty.body.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mortbay.ijetty.config.ObjectMapperProvider;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class JsonMessageBodyWriter implements MessageBodyWriter<Object> {
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperProvider.getConfiguredClone();

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return MediaType.APPLICATION_JSON_TYPE.equals(mediaType);
    }

    @Override
    public long getSize(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException {
        entityStream.write(OBJECT_MAPPER.writeValueAsBytes(o));
    }
}
