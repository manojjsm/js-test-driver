// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.eclipse.javascript.jstestdriver.core;

import com.google.eclipse.javascript.jstestdriver.core.model.LaunchConfigurationConstants;
import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.TestCase;
import com.google.jstestdriver.embedded.JsTestDriverBuilder;
import com.google.jstestdriver.hooks.TestResultListener;
import com.google.jstestdriver.runner.RunnerMode;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

import java.util.Collection;
import java.util.List;

/**
 * Layer for integration with the JsTestDriver object.
 * 
 * @author corysmith@google.com (Cory Smith)
 */
public class JstdTestRunner {

  private JsTestDriver getJstd() throws CoreException {
    
    JsTestDriverBuilder builder = new JsTestDriverBuilder()
        .setServer(ServiceLocator.getService(ServerController.class).getServerUrl())
        .setRunnerMode(RunnerMode.DEBUG)
        .raiseExceptionOnTestFailure(false)
        .setDefaultConfiguration(new EclipseServerConfiguration());

    for (TestResultListener listener : ServiceLocator.getExtensionPoints(TestResultListener.class,
        "com.google.jstestdriver.output.TestResultListener")) {
      builder.addTestListener(listener);
    }
    return builder.build();
  }

  public void runAllTests(ILaunchConfiguration configuration) throws CoreException {
    getJstd().runAllTests(getConfigurationPath(configuration));
  }

  public void runTests(List<String> tests, ILaunchConfiguration configuration) throws CoreException {
    getJstd().runTests(getConfigurationPath(configuration), tests);
  }

  public Collection<TestCase> getTestCases(ILaunchConfiguration configuration) throws CoreException {
    return getJstd().getTestCasesFor(getConfigurationPath(configuration));
  }

  /**
   * @param configuration
   * @return
   * @throws CoreException
   */
  private String getConfigurationPath(ILaunchConfiguration configuration) throws CoreException {
    return configuration.getAttribute(LaunchConfigurationConstants.CONF_FULLPATH, "");
  }

  /**
   * @param lastLaunchConfiguration
   */
  public void resetTest(ILaunchConfiguration configuration) throws CoreException {
    getJstd().reset();
  } 
}
