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

import com.google.jstestdriver.FailureException;
import com.google.jstestdriver.TestErrors;
import com.google.jstestdriver.browser.BrowserPanicException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the reporting of errors during test runs.
 * @author m.jurcovicova
 * @author corbinrsmith@gmail.com (Cory Smith)
 */
public class TestErrorReporter {
  private static final String BROWSER_PANIC =
      "The server lost contact with one or more browsers. \n\nOriginal server's message is:\n";
  private final GUIErrorReporter guiReporter;

  public TestErrorReporter(GUIErrorReporter guiReporter) {
    this.guiReporter = guiReporter;
  }

  public IStatus report(TestErrors allErrors, Logger logger) {
    List<Throwable> causes = allErrors.getCauses();
    // Other errors (such as browser panic exception) can bring up the
    // view and should report an error - a real error happened.

    // log all of them, there might be some important runtime exception
    // we do not want to accidently swallow null pointer exception
    logger.log(Level.WARNING, "Errors reported during test run", causes);

    // Show error message to the user.
    String errorMessage = createErrorMessage(causes);
    guiReporter.informUserOfError(errorMessage, logger);

    return Status.OK_STATUS;
  }

  private String createErrorMessage(List<Throwable> causes) {
    StringBuilder resultBuilder = new StringBuilder();
    for (Throwable th : causes) {
      if (!(th instanceof FailureException)) {
        resultBuilder.append(getMessage(th)).append("\n");
      }
    }

    String result = resultBuilder.toString();
    if (result.equals("")) return null;

    return result;
  }

  private String getMessage(Throwable th) {
    if (th instanceof BrowserPanicException) {
      return BROWSER_PANIC + th.getMessage();

    }
    return th.getMessage();
  }
}
