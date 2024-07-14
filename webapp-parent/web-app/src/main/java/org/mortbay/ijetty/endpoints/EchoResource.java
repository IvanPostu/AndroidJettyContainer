package org.mortbay.ijetty.endpoints;


import org.jboss.resteasy.core.interception.ServerReaderInterceptorContext;
import org.jboss.resteasy.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/echo")
public class EchoResource {
    private static final org.jboss.resteasy.logging.Logger logger = Logger.getLogger(ServerReaderInterceptorContext.class);

    @Path("/my")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public MyEntity echo1(MyEntity entity) {
        logger.info(entity.toString());
        return entity;
    }

    @Path("/polymorphic")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> echo2(Map<String, Object> entity) {
        logger.info(entity.toString());
        return entity;
    }
}