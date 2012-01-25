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

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class RunTestsActionResponseStream implements ResponseStream {
  private static final Logger logger = LoggerFactory.getLogger(RunTestsActionResponseStream.class);

  private final TestResultGenerator testResultGenerator;
  private final TestListener listener;
  private final FailureAccumulator accumulator;
  private final Gson gson = new Gson();

  public RunTestsActionResponseStream(TestResultGenerator testResultGenerator,
      TestListener listener, FailureAccumulator accumulator) {
    this.testResultGenerator = testResultGenerator;
    this.listener = listener;
    this.accumulator = accumulator;
    logger.debug("listener for tests " + listener);
  }

  public void stream(Response response) {
    switch(response.getResponseType()) {
      case TEST_RESULT:
        Collection<TestResult> testResults =
            testResultGenerator.getTestResults(response);
        for (TestResult result : testResults) {
          if (result.getResult() == TestResult.Result.failed
              || result.getResult() == TestResult.Result.error) {
            accumulator.add();
          }
          listener.onTestComplete(result);
        }
        break;
      case FILE_LOAD_RESULT:
        LoadedFiles files = gson.fromJson(response.getResponse(),
                                          response.getGsonType());
        for (FileResult result : files.getLoadedFiles()) {
          listener.onFileLoad(response.getBrowser(), result);
          if (!result.isSuccess()) {
            accumulator.add();
          }
        }
        break;
      case BROWSER_PANIC:
        BrowserPanic panic = gson.fromJson(response.getResponse(), response.getGsonType());
        throw new BrowserPanicException(panic.getBrowserInfo(), panic.getCause());
    }
  }

  public void finish() {
    listener.finish();
  }
}
