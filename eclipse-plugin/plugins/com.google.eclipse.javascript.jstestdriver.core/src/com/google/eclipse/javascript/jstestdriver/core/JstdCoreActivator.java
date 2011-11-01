// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.eclipse.javascript.jstestdriver.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.google.eclipse.javascript.jstestdriver.core.model.JstdServerListener;

/**
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class JstdCoreActivator extends Plugin {

  public static final String PLUGIN_ID = "com.google.eclipse.javascript.jstestdriver.core";

  @Override
  public void start(BundleContext context) throws Exception {
    JstdServerListener jstdServerListener = new JstdServerListener();
    PortSupplier supplier =
        ServiceLocator.getExtensionPoint(PortSupplier.class,
            "com.google.eclipse.javascript.jstestdriver.core.PortSupplier");
    ServerController serverController = new ServerController(jstdServerListener, supplier);
    ServiceLocator.registerServiceInstance(ServerController.class,
        serverController);

    ServiceLocator.registerServiceInstance(JstdTestRunner.class,
        new JstdTestRunner());
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    
  }
}
