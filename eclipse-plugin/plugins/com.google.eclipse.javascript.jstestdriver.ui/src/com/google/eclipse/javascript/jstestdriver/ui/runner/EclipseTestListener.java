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
package com.google.eclipse.javascript.jstestdriver.ui.runner;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;
import com.google.eclipse.javascript.jstestdriver.core.ServiceLocator;
import com.google.eclipse.javascript.jstestdriver.core.model.LoadedSourceFileLibrary;
import com.google.eclipse.javascript.jstestdriver.ui.view.JsTestDriverView;
import com.google.eclipse.javascript.jstestdriver.ui.view.TestResultsPanel;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.FileResult;
import com.google.jstestdriver.TestCase;
import com.google.jstestdriver.TestResult;
import com.google.jstestdriver.hooks.TestListener;

/**
 * Listens for test results and updates the UI appropriately.
 *
 * @author Cory Smith (corbinrsmith@gmail.com)
 */
public class EclipseTestListener implements TestListener {
  private static final Logger logger = Logger.getLogger(EclipseTestListener.class.getName());

  /**
   * {@inheritDoc}
   */
  @Override
  public void onTestComplete(final TestResult testResult) {
    Display.getDefault().asyncExec(new Runnable() {
        @Override
      public void run() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
          JsTestDriverView view = (JsTestDriverView) page.showView(JsTestDriverView.ID);
          TestResultsPanel panel = view.getTestResultsPanel();
          panel.addTestResults(Lists.newArrayList(testResult));
        } catch (PartInitException e) {
          logger.log(Level.SEVERE, "", e);
        }
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void finish() {
    // NOOP
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onFileLoad(BrowserInfo browserInfo, FileResult fileResult) {
    logger.info(fileResult.toString());
    LoadedSourceFileLibrary library = ServiceLocator.getService(LoadedSourceFileLibrary.class);
    library.addTestCaseSource(fileResult.getFileSource());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onTestRegistered(BrowserInfo browser, TestCase testCase) {
    logger.info(testCase.toString());
  }
}
