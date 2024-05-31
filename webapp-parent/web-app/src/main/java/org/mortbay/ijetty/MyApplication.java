package org.mortbay.ijetty;

import javax.ws.rs.core.Application;
import java.lang.invoke.LambdaMetafactory;
import java.util.HashSet;
import java.util.Set;

public class MyApplication extends Application {
    private Set singletons = new HashSet();

    public MyApplication() {
        singletons.add(new HelloWorldResource());
        singletons.add(new CustomStringMessageBodyWriter());
    }

    @Override
    public Set getSingletons() {
        return singletons;
    }
}
