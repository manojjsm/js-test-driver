// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.eclipse.javascript.jstestdriver.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Provides a single source of services
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class ServiceLocator {
  private static final ConcurrentMap<Class<?>, Object> services = new ConcurrentHashMap<Class<?>, Object>();

  @SuppressWarnings("unchecked")
  public static <T> T getService(Class<T> serviceClass) {
    T service = (T) services.get(serviceClass);
    return service;
  }
  
  public static synchronized <T> void registerServiceInstance(Class<T> serviceClass, T serviceInstance) {
    if (services.containsKey(serviceClass)) {
      throw new IllegalStateException(serviceClass + " already registered to " + services.get(serviceClass));
    }
    services.put(serviceClass, serviceInstance);
  }
  
  public static synchronized <T> T getExtensionPoint(Class<T> clazz, String id) throws CoreException {
    IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(id);
    if (elements.length != 1 ) {
      throw new CoreException(new Status(IStatus.ERROR, JstdCoreActivator.PLUGIN_ID,
        "Incorrect number of " + id + ":" + Arrays.toString(elements)));
    }
    return clazz.cast(elements[0].createExecutableExtension("class"));
  }

  public static synchronized <T> List<T> getExtensionPoints(Class<T> clazz, String id) throws CoreException {
    IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(id);
    List<T> instances = Lists.newArrayList();
    for (IConfigurationElement iConfigurationElement : elements) {
      instances.add(clazz.cast(iConfigurationElement.createExecutableExtension("class")));
    }
    return instances;
  }
}
