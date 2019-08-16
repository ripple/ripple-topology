package com.ripple.topology.ui.server.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * This {@link ServletContextListener} is provided for use with embedded web servers, such as Jetty, in Java main
 * projects where Spring-based servlets are being used.
 * <p>
 * Spring-based servlets such as DispatcherServlet or Jersey's SpringServlet expect to find a {@link WebApplicationContext}
 * registered in the Servlet Context in order to work properly.  In a typical Spring war, a WebApplicationContext is
 * created by registering a {@link ContextLoaderListener} with the Spring context file to load in the web.xml.  While
 * this is still possible to do with an embedded web server when configuring the server with Spring, the
 * WebApplicationContext will be a new context independent of the one starting the web server, and will not have access
 * to any of the Spring beans provided in the main Spring context.  This may be OK in some circumstances, but prevents
 * the possibility of having two jetty instances running with the same shared beans.
 * </p>
 * <p>
 * When wired up as a ServletContextLister with a Jetty instance wired together using Spring, this class will create a
 * new {@link WebApplicationContext} and set the main {@link ApplicationContext} as its parent, and then register the
 * new web context within the Servlet Context.  All Spring servlets, provided with their own configuration contexts, will
 * now find the requisite WebApplicationContext with access to all beans defined in the main application context.
 * </p>
 *
 */
public class EmbeddedServerServletContextListener extends ContextLoader implements ServletContextListener, ApplicationContextAware {

  private ApplicationContext parent;

  public EmbeddedServerServletContextListener() {
    super(new GenericWebApplicationContext());
  }

  @Override
  public void contextInitialized(final ServletContextEvent servletContextEvent) {
    initWebApplicationContext(servletContextEvent.getServletContext());
  }

  @Override
  public void contextDestroyed(final ServletContextEvent servletContextEvent) {
    closeWebApplicationContext(servletContextEvent.getServletContext());
  }

  @Override
  public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
    this.parent = applicationContext;
  }

  @Override
  protected ApplicationContext loadParentContext(final ServletContext servletContext) {
    return parent;
  }
}
