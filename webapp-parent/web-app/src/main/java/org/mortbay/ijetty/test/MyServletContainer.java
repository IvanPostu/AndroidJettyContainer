package org.mortbay.ijetty.test;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

public class MyServletContainer extends ServletContainer {

    public MyServletContainer() {
    }

    public MyServletContainer(final ResourceConfig resourceConfig) {
        super(resourceConfig);
    }

}
