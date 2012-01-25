// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.eclipse.javascript.jstestdriver.ui.launch;

import com.google.eclipse.javascript.jstestdriver.ui.view.JsTestDriverView;
import com.google.eclipse.javascript.jstestdriver.ui.view.TestResultsPanel;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import java.util.logging.Level;
import java.util.logging.Logger;

class BeforeTestsViewInitialization implements Runnable {
  private final Logger logger;
  private final int testsNumber;
  private final ILaunchConfiguration configuration;

  BeforeTestsViewInitialization(int testsNumber, ILaunchConfiguration configuration, Logger logger) {
    this.logger = logger;
    this.configuration = configuration;
    this.testsNumber = testsNumber;
  }

  @Override
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