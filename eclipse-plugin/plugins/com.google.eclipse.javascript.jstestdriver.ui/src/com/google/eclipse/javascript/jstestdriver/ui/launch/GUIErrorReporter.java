/*
 * Copyright 2011 Google Inc.
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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.google.eclipse.javascript.jstestdriver.ui.Activator;
import com.google.eclipse.javascript.jstestdriver.ui.view.JsTestDriverView;

/**
 * Handles the direct logic of reporting an error in the gui.
 * 
 * @author m.jurcovicova
 * @author corysmith@google.com (Cory Smith)
 */
public class GUIErrorReporter {

  private static final String ERROR_MESSAGE = "JS Test Driver Error";
  private static final String ERROR_TITLE = "JS Test Driver";


  public void informUserOfError(final String statusMessage, final Logger clientLogger) {
    // We need asyncExec because this may be called from outside of GUI thread.
    Display.getDefault().asyncExec(new Runnable() {

      @Override
      public void run() {
        IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, statusMessage);
        ErrorDialog.openError(Display.getCurrent().getActiveShell(), ERROR_TITLE, ERROR_MESSAGE,
            status);

        try {
          PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
              .showView(JsTestDriverView.ID);
        } catch (PartInitException e) {
          clientLogger.log(Level.SEVERE, "Could not open view " + JsTestDriverView.ID, e);
        }
      }
    });
  }
}
