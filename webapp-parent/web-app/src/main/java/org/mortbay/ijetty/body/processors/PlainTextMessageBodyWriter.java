package org.mortbay.ijetty.body.processors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public class PlainTextMessageBodyWriter implements MessageBodyWriter<String> {

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return String.class.isAssignableFrom(type) && MediaType.TEXT_PLAIN_TYPE.equals(mediaType);
    }

    @Override
    public long getSize(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(String s, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException {
        entityStream.write(s.getBytes());
    }
}
