package org.mortbay.ijetty.dispatcher;

import jakarta.servlet.annotation.WebServlet;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.jersey.internal.AbstractRuntimeDelegate;
import org.glassfish.jersey.message.internal.MessagingBinders;
import org.glassfish.jersey.server.ContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.jvnet.hk2.external.generator.ServiceLocatorGeneratorImpl;
import org.mortbay.ijetty.resources.HelloResource;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Application;
import javax.ws.rs.ext.RuntimeDelegate;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

@WebServlet(urlPatterns = "/*")
public class ApplicationDispatcherServlet extends HttpServlet {
    private ServletContainer servletContainer;

    private static class CustomRuntimeDelegate extends AbstractRuntimeDelegate {
        protected CustomRuntimeDelegate(ServiceLocator serviceLocator) {
//            super(Injections.createLocator("my-jersey-common-rd-locator", new MessagingBinders.HeaderDelegateProviders()));
            super(serviceLocator);
        }

        @Override
        public <T> T createEndpoint(Application application, Class<T> endpointType)
                throws IllegalArgumentException, UnsupportedOperationException {
            if (application == null) {
                throw new IllegalArgumentException("application is null.");
            }
            return ContainerFactory.createContainer(endpointType, application);
        }
    }


    @Override
    public void init() throws ServletException {
        super.init();

        ServiceLocatorFactory factory = ServiceLocatorFactory.getInstance();
        ServiceLocator locator = factory.create("my-jersey-common-rd-locator", null, new ServiceLocatorGeneratorImpl(),
                ServiceLocatorFactory.CreatePolicy.DESTROY);
        locator.setNeutralContextClassLoader(false);
        ServiceLocatorUtilities.enablePerThreadScope(locator);
        bind(locator, new MessagingBinders.HeaderDelegateProviders());

        RuntimeDelegate delegate = new CustomRuntimeDelegate(locator);
        RuntimeDelegate.setInstance(delegate);

        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(HelloResource.class);
        servletContainer = new ServletContainer(resourceConfig);

        ServletConfig servletConfig = new ServletConfig() {
            @Override
            public String getServletName() {
                return "ApplicationDispatcherServlet";
            }

            @Override
            public ServletContext getServletContext() {
                return ApplicationDispatcherServlet.this.getServletContext();
            }

            @Override
            public String getInitParameter(String name) {
                return null;
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return Collections.emptyEnumeration();
            }
        };
        servletContainer.init(servletConfig);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        servletContainer.service(request, response);
//        response.setContentType("text/html");
//        ServletOutputStream out = response.getOutputStream();
//        out.println("<html>");
//        out.println("<h1>Hello 3 From Servlet Land! " + StringUtils.capitalize("hi") + "</h1>");
//        out.println("</html>");
//        out.flush();
    }

    private static void bind(final ServiceLocator locator, final Binder binder) {
        final DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        final DynamicConfiguration dc = dcs.createDynamicConfiguration();

        locator.inject(binder);
        binder.bind(dc);

        dc.commit();
    }
}
