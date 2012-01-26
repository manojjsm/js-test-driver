/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.eclipse.javascript.jstestdriver.ui.launch;

import com.google.eclipse.javascript.jstestdriver.core.ServerController;
import com.google.eclipse.javascript.jstestdriver.core.ServiceLocator;
import com.google.eclipse.javascript.jstestdriver.core.model.JstdLaunchConfiguration;
import com.google.eclipse.javascript.jstestdriver.ui.Activator;
import com.google.eclipse.javascript.jstestdriver.ui.view.JsTestDriverView;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validates that the server is running and browsers are captured before attempting to execute
 * tests.
 *
 * @author corysmith@google.com (Cory Smith)
 */
public class LaunchValidator implements ILaunchValidator {

  private static final String NO_BROWSERS_CAPTURED = "Cannot run tests if no browsers captured";
  private static final String ERROR_MESSAGE = "JS Test Driver Error";
  private static final String ERROR_TITLE = "JS Test Driver";
  private static final String SERVER_NOT_RUNNING = "Cannot run tests if server is not running";

  private final Logger logger = Logger.getLogger(LaunchValidator.class.getName());

  @Override
  public boolean preLaunchCheck(JstdLaunchConfiguration configuration, IProgressMonitor monitor) {
    ServerController controller = ServiceLocator.getService(ServerController.class);

    if (!controller.isServerStarted()) {
      informUser(SERVER_NOT_RUNNING);
      return false;
    }

    if (!controller.isServerReady()) {
      informUser(NO_BROWSERS_CAPTURED);
      return false;
    }

    return true;
  }

  private void informUser(final String statusMessage) {
    // We need asyncExec because this may be called from outside of GUI thread.
    Display.getDefault().asyncExec(new Runnable() {

        @Override
      public void run() {
        IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, statusMessage);
        ErrorDialog.openError(
            Display.getCurrent().getActiveShell(), ERROR_TITLE, ERROR_MESSAGE, status);

        try {
          PlatformUI.getWorkbench()
              .getActiveWorkbenchWindow().getActivePage().showView(JsTestDriverView.ID);
        } catch (PartInitException e) {
          logger.log(Level.SEVERE, "Could not open view " + JsTestDriverView.ID, e);
        }
      }
    });
  }
}
