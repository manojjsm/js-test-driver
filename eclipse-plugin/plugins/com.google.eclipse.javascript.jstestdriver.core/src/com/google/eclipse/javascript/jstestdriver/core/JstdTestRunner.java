// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.eclipse.javascript.jstestdriver.core;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.google.eclipse.javascript.jstestdriver.core.model.LaunchConfigurationConstants;
import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.TestCase;
import com.google.jstestdriver.embedded.JsTestDriverBuilder;
import com.google.jstestdriver.output.TestResultListener;
import com.google.jstestdriver.runner.RunnerMode;

/**
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class JstdTestRunner {

  private JsTestDriver getJstd() throws CoreException {
    JsTestDriverBuilder builder = new JsTestDriverBuilder()
        .setServer(ServiceLocator.getService(ServerController.class).getServerUrl())
        .setBaseDir(new File("."))
        .setDefaultConfiguration(new EclipseServerConfiguration());
    
    for (TestResultListener listener : ServiceLocator.getExtensionPoints(TestResultListener.class,
        "com.google.jstestdriver.output.TestResultListener")) {
      builder.addTestListener(listener);
    }
    return builder.build();
  }

  public void runAllTests(ILaunchConfiguration configuration) throws CoreException {
    File path = new File(new File("."), configuration.getAttribute(LaunchConfigurationConstants.CONF_FILENAME, ""));
    System.out.println(path);
    getJstd().runAllTests(configuration.getAttribute(LaunchConfigurationConstants.CONF_FILENAME, ""));
  }
  
  public void runTests(List<String> tests, ILaunchConfiguration configuration) throws CoreException {
    throw new CoreException(new Status(IStatus.ERROR, JstdCoreActivator.PLUGIN_ID, "not implemented"));
  }
  
  public Collection<TestCase> getTestCases(ILaunchConfiguration configuration) throws CoreException {
    return getJstd().getTestCasesFor(configuration.getAttribute(LaunchConfigurationConstants.CONF_FULLPATH, ""));
  }
  
  /*} catch (FileNotFoundException e) {
    throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
        "Config file for JSTestDriver not found", e));
  } catch (final RuntimeException e) {
    throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
        "Runtime exception happened in JS Test Driver", e));
  }*/

  /**
   * @param lastLaunchConfiguration
   */
  public void resetTest(ILaunchConfiguration lastLaunchConfiguration) throws CoreException {
    getJstd().reset();
  } 
}
