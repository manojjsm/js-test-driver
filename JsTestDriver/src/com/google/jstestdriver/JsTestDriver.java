/*
 * Copyright 2012 Google Inc.
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

import com.google.jstestdriver.config.Configuration;

import java.util.List;



/**
 * The interface that governs the interactions with JsTestDriver.
 * @author corysmith@google.com (Cory Smith)
 */
public interface JsTestDriver {

  /**
   * Starts the server on the configured port.
   */
  public void startServer();

  /**
   * Stops the server.
   */
  public void stopServer();

  /**
   * Runs the default configuration with the default flags.
   */
  public List<TestResult> runConfiguration();

  /**
   * Runs all tests on the currently captured browsers.
   * @param path
   * @return
   */
  public List<TestResult> runAllTests(String path);

  /**
   * Runs all tests on the currently captured browsers.
   * @param config
   * @return
   */
  public List<TestResult> runAllTests(Configuration config);

  /**
   * Runs the provided tests on the currently captured browsers.
   * @param path
   * @param tests
   * @return
   */
  public List<TestResult> runTests(String path, List<String> tests);

  /**
   * Runs the provided tests on the currently captured browsers.
   * @param config
   * @param tests
   * @return
   */
  public List<TestResult> runTests(Configuration config, List<String> tests);

  /**
   * Causes all browser to be reloaded.
   */
  public void reset();

  /**
   * Retrieves all the tests found in the current configuration.
   * @param path Path to a configuration file.
   * @return 
   */
  public List<TestCase> getTestCasesFor(String path);

  /**
   * Retrieves all the tests found in the current configuration.
   * @param config A jstd configuration.
   * @return
   */
  public List<TestCase> getTestCasesFor(Configuration config);

  /**
   * Attempts to start a browser and capture.
   * @param browserPath Path to the browser binary.
   */
  public void captureBrowser(String browserPath);

}
