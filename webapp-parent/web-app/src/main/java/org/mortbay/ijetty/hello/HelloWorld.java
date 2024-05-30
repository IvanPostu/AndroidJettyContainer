package org.mortbay.ijetty.hello;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.mortbay.ijetty.resources.HelloResource;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/* ------------------------------------------------------------ */

/**
 * Hello Servlet
 */
public class HelloWorld extends HttpServlet {
    private ServletContainer servletContainer;
    String proofOfLife = null;

    /* ------------------------------------------------------------ */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        //to demonstrate it is possible
        Object o = config.getServletContext().getAttribute("com.ipostu.androidjettycontainer.contentResolver");
        android.content.ContentResolver resolver = (android.content.ContentResolver) o;
        android.content.Context androidContext = (android.content.Context) config.getServletContext().getAttribute("com.ipostu.androidjettycontainer.context");
        proofOfLife = androidContext.getApplicationInfo().packageName;
        org.glassfish.hk2.utilities.Binder A;

        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(HelloResource.class);
        servletContainer = new ServletContainer(resourceConfig);
    }

    /* ------------------------------------------------------------ */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    /* ------------------------------------------------------------ */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        servletContainer.service(request, response);

//        response.setContentType("text/html");
//        ServletOutputStream out = response.getOutputStream();
//        out.println("<html>");
//        out.println("<h1>Hello 3 From Servlet Land! " + StringUtils.capitalize("hi") + "</h1>");
//        out.println("Brought to you by: " + proofOfLife);
//        out.println("</html>");
//        out.flush();
    }

}
