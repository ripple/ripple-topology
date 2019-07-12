package com.ripple.topology.ui.server.config;

import org.slf4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @deprecated Will be removed once this UI is updated to use SpringBoot.
 */
@Deprecated
public abstract class Server {
  private final AtomicBoolean stopping = new AtomicBoolean(false);
  private final Thread shutdownThread = new Thread(() -> {
    this.stop(true);
  });

  public Server() {
  }

  public final Server start() {
    this.getLogger().info("Server starting...");
    this.registerShutdownHook();
    this.onBeginStart();
    this.getLogger().info("Server started successfully.");
    return this;
  }

  public final void stop() {
    this.stop(false);
  }

  private void stop(boolean initiatedFromHook) {
    if (!this.stopping.getAndSet(true)) {
      this.getLogger().info("Server shutting down...");

      try {
        this.onBeginShutdown();
        this.getLogger().info("Server shut down successfully");
        if (!initiatedFromHook) {
          Runtime.getRuntime().removeShutdownHook(this.shutdownThread);
        }

        this.stopping.set(false);
      } catch (Exception var3) {
        throw new RuntimeException("Server failure during shut down", var3);
      }
    }

  }

  protected void onBeginShutdown() {
  }

  protected void onBeginStart() {
  }

  protected abstract Logger getLogger();

  private void registerShutdownHook() {
    Runtime.getRuntime().addShutdownHook(this.shutdownThread);
  }

  static {
    System.setProperty("user.timezone", "UTC");
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }
}
