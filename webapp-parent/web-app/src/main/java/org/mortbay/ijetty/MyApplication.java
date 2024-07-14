package org.mortbay.ijetty;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.mortbay.ijetty.body.processors.PlainTextMessageBodyWriter;
import org.mortbay.ijetty.body.processors.JsonMessageBodyReader;
import org.mortbay.ijetty.body.processors.JsonMessageBodyWriter;
import org.mortbay.ijetty.endpoints.EchoResource;
import org.mortbay.ijetty.endpoints.HelloWorldResource;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class MyApplication extends Application {
    private final Set<Object> singletons = new HashSet<>();

    public MyApplication() {
        singletons.add(new PlainTextMessageBodyWriter());
        singletons.add(new JsonMessageBodyReader());
        singletons.add(new JsonMessageBodyWriter());

        singletons.add(new HelloWorldResource());
        singletons.add(new EchoResource());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
