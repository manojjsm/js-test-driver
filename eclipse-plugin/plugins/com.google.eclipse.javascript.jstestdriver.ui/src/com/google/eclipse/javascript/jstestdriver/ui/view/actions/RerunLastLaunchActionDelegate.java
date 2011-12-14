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
package com.google.eclipse.javascript.jstestdriver.ui.view.actions;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import com.google.eclipse.javascript.jstestdriver.ui.view.JsTestDriverView;
import com.google.eclipse.javascript.jstestdriver.ui.view.TestResultsPanel;

/**
 * @author shyamseshadri@gmail.com (Shyam Seshadri)
 * 
 */
public class RerunLastLaunchActionDelegate implements IViewActionDelegate {

  private TestResultsPanel view;
  private final Logger logger = Logger.getLogger(RerunLastLaunchActionDelegate.class.getCanonicalName());

  @Override
  public void init(IViewPart view) {
    if (view instanceof JsTestDriverView) {
      this.view = ((JsTestDriverView) view).getTestResultsPanel();
    }
  }

  @Override
  public void run(IAction action) {
    final ILaunchConfiguration lastLaunch = view.getLastLaunchConfiguration();
    if (lastLaunch != null) {
      try {
        //Launch configuration launch tend to be called outside of GUI
        //thread, there is not reason to make this call GUI-safe.
        //
        //Both initialization and validation are now inside launch
        //configuration.
        lastLaunch.launch(ILaunchManager.RUN_MODE, null);
      } catch (CoreException e) {
        logger.log(Level.SEVERE, "", e);
      }
    }
  }

  @Override
  public void selectionChanged(IAction action, ISelection selection) {
  }

}
