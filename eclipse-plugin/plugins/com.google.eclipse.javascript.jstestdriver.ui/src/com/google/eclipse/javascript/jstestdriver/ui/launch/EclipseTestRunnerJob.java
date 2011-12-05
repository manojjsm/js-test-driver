/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.eclipse.javascript.jstestdriver.ui.launch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.google.eclipse.javascript.jstestdriver.core.JstdTestRunner;
import com.google.eclipse.javascript.jstestdriver.core.ServiceLocator;
import com.google.eclipse.javascript.jstestdriver.ui.Activator;
import com.google.eclipse.javascript.jstestdriver.ui.view.JsTestDriverView;
import com.google.eclipse.javascript.jstestdriver.ui.view.TestResultsPanel;
import com.google.jstestdriver.TestCase;

/**
 * Handles the execution of jstd tests into a background thread.
 * 
 * @author m.jurcovicova
 */
public class EclipseTestRunnerJob extends Job {

  private static final String ERROR_MESSAGE = "Exception while running tests.";

  private static final Logger logger = Logger.getLogger(EclipseTestRunnerJob.class.getName());

  private final JstdTestRunner runner;
  public static final String JOB_NAME = "Run js-test-driver Tests Job";
  private final ILaunchConfiguration configuration;
  private final List<String> testsToRun;

  public EclipseTestRunnerJob(ILaunchConfiguration configuration) {
    this(configuration, new ArrayList<String>());
  }

  public EclipseTestRunnerJob(ILaunchConfiguration configuration, List<String> testsToRun) {
    super(JOB_NAME);
    this.configuration = configuration;
    this.testsToRun = testsToRun;

    runner = ServiceLocator.getService(JstdTestRunner.class);
  }

  @Override
  protected IStatus run(IProgressMonitor monitor) {
    try {
      // initialize JsTestDriverView; this needs to be done in UI thread
      Display.getDefault().asyncExec(
          new BeforeTestsViewInitialization(getTestsNumber(), configuration, logger));

      if (testsToRun.isEmpty()) {
        runner.runAllTests(configuration);
      } else {
        runner.runTests(testsToRun, configuration);
      }

    } catch (CoreException e) {
      logger.log(Level.SEVERE, "", e);
      return new Status(Status.ERROR, Activator.PLUGIN_ID, ERROR_MESSAGE, e);
    }

    return Status.OK_STATUS;
  }

  private int getTestsNumber() throws CoreException {
    if (testsToRun.isEmpty()) {
      Collection<TestCase> testCases = runner.getTestCases(configuration);
      return calcTestsNumber(testCases);
    }

    return testsToRun.size();
  }

  private int calcTestsNumber(Collection<TestCase> testCases) {
    int result = 0;
    for (TestCase testCase : testCases) {
      result += testCase.getTests().size();
    }
    return result;
  }


  static class BeforeTestsViewInitialization implements Runnable {
    private final Logger logger;
    private final int testsNumber;
    private final ILaunchConfiguration configuration;

    BeforeTestsViewInitialization(int testsNumber, ILaunchConfiguration configuration, Logger logger) {
      this.logger = logger;
      this.configuration = configuration;
      this.testsNumber = testsNumber;
    }

    public void run() {
      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      try {
        JsTestDriverView view = (JsTestDriverView) page.showView(JsTestDriverView.ID);
        TestResultsPanel panel = view.getTestResultsPanel();
        panel.setupForNextTestRun(configuration);
        panel.addNumberOfTests(testsNumber);
      } catch (CoreException e) {
        logger.log(Level.SEVERE, "", e);
      }
    }
  }
}
