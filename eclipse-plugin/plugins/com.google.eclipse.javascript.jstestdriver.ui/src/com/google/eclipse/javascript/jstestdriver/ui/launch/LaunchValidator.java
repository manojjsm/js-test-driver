// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.eclipse.javascript.jstestdriver.ui.launch;

import com.google.eclipse.javascript.jstestdriver.core.ServerController;
import com.google.eclipse.javascript.jstestdriver.core.ServiceLocator;
import com.google.eclipse.javascript.jstestdriver.ui.Activator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;

/**
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class LaunchValidator {

  public boolean preLaunchCheck() {
    ServerController controller = ServiceLocator.getService(ServerController.class);
    if (!controller.isServerStarted()) {
      IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
          "Cannot run tests if server is not running");
      ErrorDialog.openError(Display.getCurrent().getActiveShell(),
          "JS Test Driver", "JS Test Driver Error", status);
      return false;
    } else if (!controller.isServerReady()) {
      IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
          "Cannot run tests if no browsers captured");
      ErrorDialog.openError(Display.getCurrent().getActiveShell(),
          "JS Test Driver", "JS Test Driver Error", status);
      return false;
    }
    return true;
  }
}
