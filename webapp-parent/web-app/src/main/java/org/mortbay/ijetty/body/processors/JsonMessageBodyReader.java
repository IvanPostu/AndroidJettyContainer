package org.mortbay.ijetty.body.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mortbay.ijetty.config.ObjectMapperProvider;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class JsonMessageBodyReader implements MessageBodyReader<Object> {
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperProvider.getConfiguredClone();

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return MediaType.APPLICATION_JSON_TYPE.equals(mediaType);
    }

    @Override
    public Object readFrom(Class<Object> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
        return OBJECT_MAPPER.readValue(inputStream, aClass);
    }
}
