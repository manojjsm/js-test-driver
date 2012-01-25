// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.eclipse.javascript.jstestdriver.core.model;

import com.google.common.collect.Lists;
import com.google.eclipse.javascript.jstestdriver.core.ProjectHelper;
import com.google.jstestdriver.model.BasePaths;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

import java.io.File;
import java.util.List;

/**
 * Wrapper for the {@link ILaunchConfiguration},
 * to make it easy to extract information.
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class JstdLaunchConfiguration {
  
  private final ILaunchConfiguration configuration;
  
  private final ProjectHelper helper = new ProjectHelper();

  public JstdLaunchConfiguration(ILaunchConfiguration configuration) {
    this.configuration = configuration;
  }
  
  public String getName() throws CoreException {
    return configuration.getAttribute(LaunchConfigurationConstants.CONF_FILENAME, "");
  }

  public IProject getProject() throws CoreException {
    return helper.getProject(configuration.getAttribute(LaunchConfigurationConstants.PROJECT_NAME, ""));
  }
  
  public String getConfigurationPath() throws CoreException {
    String path = configuration.getAttribute(LaunchConfigurationConstants.CONF_FULLPATH, "");
    return getProject().getFile(path).getLocation().toOSString();
  }
  
  public BasePaths getBasePaths() throws CoreException {
    List<File> paths = Lists.newArrayList();
    for (int j = 0;; j++) {
      String path = configuration.getAttribute(
          String.format("%s_%s", LaunchConfigurationConstants.BASEPATH, j),
          "NULL");
      if ("NULL".equals(path)) {
        break;
      }
      paths.add(new File(path));
    }
    return new BasePaths(paths);
  }
  
  ILaunchConfiguration toLaunchConfiguration() {
    return configuration;
  }

}
