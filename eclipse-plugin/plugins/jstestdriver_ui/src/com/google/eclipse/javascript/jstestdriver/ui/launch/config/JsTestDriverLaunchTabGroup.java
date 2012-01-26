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
package com.google.eclipse.javascript.jstestdriver.ui.launch.config;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;

import com.google.eclipse.javascript.jstestdriver.core.ServiceLocator;
import com.google.eclipse.javascript.jstestdriver.ui.Activator;
import com.google.eclipse.javascript.jstestdriver.core.JsTestDriverConfigurationProvider;

/**
 * Launch Tab Group for JS Test Driver.
 * 
 * @author shyamseshadri@gmail.com (Shyam Seshadri)
 */
public class JsTestDriverLaunchTabGroup extends AbstractLaunchConfigurationTabGroup {


  @Override
  public void createTabs(ILaunchConfigurationDialog dialog, String mode) {
    try {
      setTabs(new ILaunchConfigurationTab[] {
          new JsTestDriverLaunchTab(ServiceLocator.getExtensionPoint(
              JsTestDriverConfigurationProvider.class,
              JsTestDriverConfigurationProvider.class.getName())), new CommonTab()});
    } catch (CoreException e) {
      IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString());
      ErrorDialog.openError(Display.getCurrent().getActiveShell(), "JS Test Driver",
          "JsTestDriver Error", status);
    }
  }
}
