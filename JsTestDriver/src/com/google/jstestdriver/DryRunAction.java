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
package com.google.jstestdriver;

import java.io.PrintStream;
import java.util.List;

import com.google.gson.Gson;
import com.google.jstestdriver.browser.BrowserPanicException;
import com.google.jstestdriver.hooks.TestResultListener;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.model.RunData;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class DryRunAction implements BrowserAction {

  private final List<String> expressions;
  private final ResponseStreamFactory responseStreamFactory;

  // TODO(corysmith): Solve/remove the response stream issue. Currently,
  //    they make little sense, as custom built listeners provide more useful stability.
  public static class DryRunActionResponseStream implements ResponseStream {

    private final Gson gson = new Gson();
    private final PrintStream out;
    private final TestResultListener listener;

    public DryRunActionResponseStream(PrintStream out, TestResultListener testResultListener) {
      this.out = out;
      this.listener = testResultListener;
    }

    public void finish() {
    }

    public void stream(Response response) {
      BrowserInfo browser = response.getBrowser();
      switch(response.getResponseType()) {
        case TEST_QUERY_RESULT:
          DryRunInfo dryRunInfo = gson.fromJson(response.getResponse(), DryRunInfo.class);

          for (TestCase testCase : dryRunInfo.getTestCases()) {
            listener.onTestRegistered(browser, testCase);
          }

          out.println(String.format("%s %s: %s tests %s", browser.getName(), browser
              .getVersion(), dryRunInfo.getNumTests(), dryRunInfo.getTestNames()));
          break;
        case FILE_LOAD_RESULT:
          LoadedFiles files = gson.fromJson(response.getResponse(),
                                            response.getGsonType());
          for (FileResult result : files.getLoadedFiles()) {
            listener.onFileLoad(response.getBrowser(), result);
          }
          break;
        case BROWSER_PANIC:
          BrowserPanic panic = gson.fromJson(response.getResponse(), response.getGsonType());
          throw new BrowserPanicException(panic.getBrowserInfo(), panic.getCause());
      }
    }
  }

  public DryRunAction(ResponseStreamFactory responseStreamFactory, List<String> expressions) {
    this.responseStreamFactory = responseStreamFactory;
    this.expressions = expressions;
  }

  public ResponseStream run(String id, JsTestDriverClient client, RunData runData, JstdTestCase testCase) {
    final ResponseStream responseStream = responseStreamFactory.getDryRunActionResponseStream();
    if (expressions.size() == 1 && expressions.get(0).equals("all")) {
      client.dryRun(id, responseStream, testCase);
    } else {
      client.dryRunFor(id, responseStream, expressions, testCase);
    }
    return responseStream;
  }
}
