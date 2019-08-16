package com.ripple.topology.ui.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Objects;

/**
 * @deprecated Will be removed once this UI is updated to use SpringBoot.
 */
@Deprecated
public class SpringServer extends Server {

  private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

  public SpringServer(Class<?> configuration, Class<?>... configurations) {
    this.context.register(Objects.requireNonNull(configuration));
    for (Class<?> config : configurations) {
      this.context.register(config);
    }
  }

  public ApplicationContext getContext() {
    return context;
  }

  @Override
  protected void onBeginStart() {
    context.refresh();
  }

  @Override
  protected void onBeginShutdown() {
    context.close();
  }

  @Override
  protected Logger getLogger() {
    return LoggerFactory.getLogger(SpringServer.class);
  }
}
