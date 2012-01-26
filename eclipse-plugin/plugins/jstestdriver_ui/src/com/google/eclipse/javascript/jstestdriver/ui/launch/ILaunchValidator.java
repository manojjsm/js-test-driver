// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.eclipse.javascript.jstestdriver.ui.launch;

import com.google.eclipse.javascript.jstestdriver.core.model.JstdLaunchConfiguration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface for validating launch configurations and workspace state before
 * launch.
 * 
 * @author corysmith@google.com (Cory Smith)
 *
 */
public interface ILaunchValidator {

  /**
   * Checks to see if the launch can be run based on the current workspace state.
   * @param configuration The configuration to validate for launching.
   * @param monitor Allows the validator to update on the validation progress.
   * @return Boolean to indicate if the launch should continue.
   */
  boolean preLaunchCheck(JstdLaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException;

}
