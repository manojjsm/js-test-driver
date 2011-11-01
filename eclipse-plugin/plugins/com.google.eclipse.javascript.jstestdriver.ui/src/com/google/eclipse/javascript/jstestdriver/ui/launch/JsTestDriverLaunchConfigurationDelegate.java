/*
 * Copyright 2009 Google Inc.
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
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import com.google.eclipse.javascript.jstestdriver.core.JstdLaunchListener;
import com.google.eclipse.javascript.jstestdriver.core.JstdTestRunner;
import com.google.eclipse.javascript.jstestdriver.core.ServerController;
import com.google.eclipse.javascript.jstestdriver.core.ServiceLocator;
import com.google.eclipse.javascript.jstestdriver.core.model.LaunchConfigurationConstants;
import com.google.eclipse.javascript.jstestdriver.ui.Activator;
import com.google.eclipse.javascript.jstestdriver.ui.view.JsTestDriverView;
import com.google.eclipse.javascript.jstestdriver.ui.view.TestResultsPanel;
import com.google.jstestdriver.TestCase;

/**
 * Delegate that launches JSTestDriver run configurations. Sets up the Test
 * Results view for the upcoming run, does a dry run to find the number of tests
 * and then executes them and updates the view as and when results come in.
 * 
 * @author shyamseshadri@gmail.com (Shyam Seshadri)
 */
public class JsTestDriverLaunchConfigurationDelegate extends LaunchConfigurationDelegate {
  private static final Logger logger = Logger.getLogger(JsTestDriverLaunchConfigurationDelegate.class.getName());

  @Override
  public void launch(final ILaunchConfiguration configuration, String mode, final ILaunch launch,
      IProgressMonitor monitor) throws CoreException {
    if (!mode.equals(ILaunchManager.RUN_MODE)) {
      throw new IllegalStateException("Can only launch JS Test Driver configuration from Run mode");
    }
    @SuppressWarnings("unchecked")
    final List<String> testsToRun =
        configuration.getAttribute(LaunchConfigurationConstants.TESTS_TO_RUN,
            new ArrayList<String>());

    notifyAllListeners(configuration);

    Display.getDefault().asyncExec(new Runnable() {
      public void run() {
        IWorkbenchPage page = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getActivePage();
        try {
          JsTestDriverView view = (JsTestDriverView) page
              .showView(JsTestDriverView.ID);
          TestResultsPanel panel = view.getTestResultsPanel();
          JstdTestRunner runner = ServiceLocator.getService(JstdTestRunner.class);
          Collection<TestCase> testCases = runner.getTestCases(configuration);
          panel.addNumberOfTests(testCases.size());
          if (testsToRun.isEmpty()) {
            runner.runAllTests(configuration);
          } else {
            runner.runTests(testsToRun, configuration);
          }
        } catch (CoreException e) {
          logger.log(Level.SEVERE, "", e);
        }
      }
    });
  }

  private void notifyAllListeners(ILaunchConfiguration configuration) throws CoreException {
    IConfigurationElement[] elements =
        Platform.getExtensionRegistry().getConfigurationElementsFor(
            "com.google.eclipse.javascript.jstestdriver.core.jstdLaunchListener");

    for (IConfigurationElement element : elements) {
      JstdLaunchListener launchListener =
          (JstdLaunchListener) element.createExecutableExtension("class");
      launchListener.aboutToLaunch(configuration);
    }
  }

  @Override
  public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
    if (ILaunchManager.RUN_MODE.equals(mode)) {
      return new Launch(configuration, mode, null);
    } else {
      throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
          "Can only launch JS Test Driver configuration from Run mode"));
    }
  }

  @Override
  public boolean preLaunchCheck(ILaunchConfiguration configuration, String mode,
      IProgressMonitor monitor) throws CoreException {
    ServerController controller = ServiceLocator.getService(ServerController.class);
    return super.preLaunchCheck(configuration, mode, monitor)
        && mode.equals(ILaunchManager.RUN_MODE) && !monitor.isCanceled()
        && controller.isServerReady();
  }
}
