package com.ripple.topology.ui.server.config;

import javax.servlet.Servlet;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class JettyUtils {

  /**
   * Creates a Spring-aware Servlet, bootstrapped with a Spring configuration class.  This results in a Servlet
   * containing it's own private Spring context with the global Application-level context as its parent.
   *
   * @param name servlet name
   * @param servletClass servlet class
   * @param configClass Spring config class
   * @return Jetty ServletHolder
   */
  public static ServletHolder createServlet(String name, Class<? extends Servlet> servletClass, Class<?> configClass) {
    final ServletHolder servletHolder = new ServletHolder();
    servletHolder.setName(name);
    servletHolder.setClassName(servletClass.getCanonicalName());
    servletHolder.setInitParameter("contextClass", AnnotationConfigWebApplicationContext.class.getCanonicalName());
    servletHolder.setInitParameter("contextConfigLocation", configClass.getCanonicalName());
    servletHolder.setInitOrder(0); // Load on Startup
    return servletHolder;
  }

  /**
   * Creates a DispatcherServlet configured with a Configuration class.
   *
   * @param name servlet name
   * @param configClass Spring config class
   * @return Jetty ServletHolder
   */
  public static ServletHolder createServlet(String name, Class<?> configClass) {
    return createServlet(name, DispatcherServlet.class, configClass);
  }

  /**
   * Creates configures an instantiated DispatcherServlet with a Configuration class.
   *
   * @param name servlet name
   * @param servlet DispatcherServlet
   * @param configClass Spring config class
   * @return Jetty ServletHolder
   */
  public static ServletHolder createServlet(String name, DispatcherServlet servlet, Class<?> configClass) {
    final ServletHolder servletHolder = new ServletHolder();
    servletHolder.setName(name);
    servletHolder.setServlet(servlet);
    servlet.setContextClass(AnnotationConfigWebApplicationContext.class);
    servlet.setContextConfigLocation(configClass.getCanonicalName());
    servletHolder.setInitOrder(0); // Load on Startup
    return servletHolder;
  }
}
