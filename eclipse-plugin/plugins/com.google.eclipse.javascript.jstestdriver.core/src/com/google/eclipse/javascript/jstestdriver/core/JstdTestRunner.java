// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.eclipse.javascript.jstestdriver.core;

import com.google.common.collect.Lists;
import com.google.eclipse.javascript.jstestdriver.core.model.JstdLaunchConfiguration;
import com.google.eclipse.javascript.jstestdriver.core.model.LaunchConfigurationConstants;
import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.TestCase;
import com.google.jstestdriver.embedded.JsTestDriverBuilder;
import com.google.jstestdriver.hooks.TestListener;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.runner.RunnerMode;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * Layer for integration with the JsTestDriver object.
 * 
 * @author corysmith@google.com (Cory Smith)
 */
public class JstdTestRunner {
  ProjectHelper helper = new ProjectHelper();

  private JsTestDriver getJstd(BasePaths basePaths) throws CoreException {
    JsTestDriverBuilder builder = new JsTestDriverBuilder()
        .setServer(ServiceLocator.getService(ServerController.class).getServerUrl())
        .setRunnerMode(RunnerMode.DEBUG)
        .addBasePaths(basePaths)
        .raiseExceptionOnTestFailure(false)
        .setDefaultConfiguration(new EclipseServerConfiguration());

    for (TestListener listener : ServiceLocator.getExtensionPoints(TestListener.class,
        "com.google.jstestdriver.hooks.TestListener")) {
      builder.addTestListener(listener);
    }
    return builder.build();
  }

  public void runAllTests(JstdLaunchConfiguration launchConfiguration) throws CoreException {
    getJstd(launchConfiguration.getBasePaths()).runAllTests(launchConfiguration.getConfigurationPath());
  }

  private BasePaths getBasePaths(ILaunchConfiguration configuration) throws CoreException {
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

  public void runTests(List<String> tests, JstdLaunchConfiguration launchConfiguration) throws CoreException {
    getJstd(launchConfiguration.getBasePaths()).runTests(launchConfiguration.getConfigurationPath(), tests);
  }

  public Collection<TestCase> getTestCases(ILaunchConfiguration configuration) throws CoreException {
    return getJstd(getBasePaths(configuration)).getTestCasesFor(getConfigurationPath(configuration));
  }

  private String getConfigurationPath(ILaunchConfiguration configuration) throws CoreException {
    String path = configuration.getAttribute(LaunchConfigurationConstants.CONF_FULLPATH, "");
    IProject project = helper.getProject(configuration.getAttribute(LaunchConfigurationConstants.PROJECT_NAME, ""));
    return project.getFile(path).getLocation().toOSString();
  }


  public void resetTest(ILaunchConfiguration configuration) throws CoreException {
    getJstd(getBasePaths(configuration)).reset();
  } 
}
