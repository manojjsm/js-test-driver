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
package com.google.eclipse.javascript.jstestdriver.ui.runner;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;
import com.google.eclipse.javascript.jstestdriver.ui.view.JsTestDriverView;
import com.google.eclipse.javascript.jstestdriver.ui.view.TestResultsPanel;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.FileResult;
import com.google.jstestdriver.TestCase;
import com.google.jstestdriver.TestResult;
import com.google.jstestdriver.hooks.TestResultListener;

/**
 *
 *
 * @author Cory Smith (corbinrsmith@gmail.com) 
 */
public class EclipseTestResultListener implements TestResultListener {
  private static final Logger logger = Logger.getLogger(EclipseTestResultListener.class.getName());

  @Override
  public void onTestComplete(final TestResult testResult) {
    System.out.println(testResult.toString());
    Display.getDefault().asyncExec(new Runnable() {

      public void run() {
        IWorkbenchPage page = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getActivePage();
        try {
          JsTestDriverView view = (JsTestDriverView) page
              .showView(JsTestDriverView.ID);
          TestResultsPanel panel = view.getTestResultsPanel();
          panel.addTestResults(Lists.newArrayList(testResult));
        } catch (PartInitException e) {
          logger.log(Level.SEVERE, "", e);
        }
      }
    });
  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.output.TestResultListener#finish()
   */
  @Override
  public void finish() {
    // TODO Auto-generated method stub

  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.hooks.TestResultListener#onFileLoad(com.google.jstestdriver.BrowserInfo, com.google.jstestdriver.FileResult)
   */
  @Override
  public void onFileLoad(BrowserInfo browserInfo, FileResult fileResult) {
    logger.info(fileResult.toString());
  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.hooks.TestResultListener#onTestRegistered(com.google.jstestdriver.BrowserInfo, com.google.jstestdriver.TestCase)
   */
  @Override
  public void onTestRegistered(BrowserInfo browser, TestCase testCase) {
    logger.info(testCase.toString());
  }

}
