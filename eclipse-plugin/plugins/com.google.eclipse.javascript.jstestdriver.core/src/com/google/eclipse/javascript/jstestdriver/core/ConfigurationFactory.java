// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.eclipse.javascript.jstestdriver.core;

import com.google.jstestdriver.config.Configuration;

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Handles the creation of configurations from the workspace.
 * 
 * @author corbinrsmith@gmail.com (Corbin Smith)
 */
public interface ConfigurationFactory {
  /**
   * Creates a jstd configuration from an ILaunchConfiguration.
   */
  Configuration getConfiguration(ILaunchConfiguration launchConfigurations);
}
