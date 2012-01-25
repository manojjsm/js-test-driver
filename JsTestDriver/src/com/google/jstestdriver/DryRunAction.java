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

import com.google.gson.Gson;
import com.google.jstestdriver.browser.BrowserPanicException;
import com.google.jstestdriver.hooks.TestListener;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.model.RunData;
import com.google.jstestdriver.protocol.BrowserLog;

import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.List;

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
    private final TestListener listener;

    public DryRunActionResponseStream(PrintStream out, TestListener testResultListener) {
      this.out = out;
      this.listener = testResultListener;
    }

    @Override
    public void finish() {
    }

    @Override
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
        case LOG:
          BrowserLog log = gson.fromJson(response.getResponse(), response.getGsonType());
          LoggerFactory.getLogger(log.getSource()).debug(log.getMessage());
          break;
      }
    }
  }

  public DryRunAction(ResponseStreamFactory responseStreamFactory, List<String> expressions) {
    this.responseStreamFactory = responseStreamFactory;
    this.expressions = expressions;
  }

  @Override
  public ResponseStream run(String id, JsTestDriverClient client, RunData runData, JstdTestCase testCase) {
    final ResponseStream responseStream = responseStreamFactory.getDryRunActionResponseStream();
    client.dryRunFor(id, responseStream, expressions, testCase);
    return responseStream;
  }
}
