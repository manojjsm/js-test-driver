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

goog.provide('jstestdriver.TestRunner.TestCaseMap');
goog.provide('jstestdriver.TestRunner');

/**
 * @param pluginRegistrar
 * @constructor
 */
jstestdriver.TestRunner = function(pluginRegistrar) {
  this.pluginRegistrar_ = pluginRegistrar;

  this.boundRunNextConfiguration_ =
      jstestdriver.bind(this, this.runNextConfiguration_);
};


/**
 * Runs all TestRunConfigurations.
 * @param {Array.<jstestdriver.TestRunConfiguration>} testRunsConfiguration Configurations to
 *      run. This array willl be modified...
 * @param {function(jstestdriver.TestResult):null} onTestDone
 * 
 */
jstestdriver.TestRunner.prototype.runTests = function(testRunsConfiguration,
                                                      onTestDone,
                                                      onComplete,
                                                      captureConsole) {
  this.pluginRegistrar_.onTestsStart();
  this.testRunsConfiguration_ = testRunsConfiguration;
  this.onTestDone_ = onTestDone;
  this.onComplete_ = onComplete;
  this.captureConsole_ = captureConsole;
  this.runNextConfiguration_();
};


jstestdriver.TestRunner.prototype.finish_ = function() {
  var onComplete = this.onComplete_;
  this.pluginRegistrar_.onTestsFinish();
  this.testRunsConfiguration_ = null;
  this.onTestDone_ = null;
  this.onComplete_ = null;
  this.captureConsole_ = false;
  onComplete();
};


jstestdriver.TestRunner.prototype.runNextConfiguration_ = function() {
  if (this.testRunsConfiguration_.length == 0) {
    this.finish_();
    return;
  }
  this.runConfiguration(
      this.testRunsConfiguration_.shift(),
      this.onTestDone_,
      this.boundRunNextConfiguration_);
}


/**
 * Runs a test configuration.
 * @param {jstestdriver.TestRunConfiguration} config
 * @param {function(jstestdriver.TestResult):null} onTestDone
 *     Function to be called when test is done.
 * @param {Function} onComplete Function to be called when all tests are done.
 */
jstestdriver.TestRunner.prototype.runConfiguration = function(config,
                                                              onTestDone,
                                                              onComplete) {
  var self = this;
  if (self.captureConsole_) {
    self.overrideConsole_();
  }

  jstestdriver.log("running configuration " + config);
  this.pluginRegistrar_.runTestConfiguration(
      config,
      onTestDone,
      function() {
        if (self.captureConsole_) {
          self.resetConsole_();
        }
        onComplete.apply(this, arguments);
      }
  );
};


jstestdriver.TestRunner.prototype.overrideConsole_ = function() {
  this.logMethod_ = window.console.log;
  this.logDebug_ = window.console.debug;
  this.logInfo_ = window.console.info;
  this.logWarn_ =  window.console.warn;
  this.logError_ =  window.console.error;
  window.console.log = function() { jstestdriver.console.log.apply(jstestdriver.console, arguments); };
  window.console.debug = function() { jstestdriver.console.debug.apply(jstestdriver.console, arguments); };
  window.console.info = function() { jstestdriver.console.info.apply(jstestdriver.console, arguments); };
  window.console.warn = function() { jstestdriver.console.warn.apply(jstestdriver.console, arguments); };
  window.console.error = function() { jstestdriver.console.error.apply(jstestdriver.console, arguments); };
};


jstestdriver.TestRunner.prototype.resetConsole_ = function() {
  window.console.log = this.logMethod_;
  window.console.debug = this.logDebug_;
  window.console.info = this.logInfo_;
  window.console.warn = this.logWarn_;
  window.console.error = this.logError_;
};



/**
 * A map to manage the state of running TestCases.
 * @constructor
 */
jstestdriver.TestRunner.TestCaseMap = function() {
  this.testCases_ = {};
};


/**
 * Start a TestCase.
 * @param {String} testCaseName The name of the test case to start.
 */
jstestdriver.TestRunner.TestCaseMap.prototype.startCase = function(testCaseName) {
  this.testCases_[testCaseName] = true;
};


/**
 * Stops a TestCase.
 * @param {String} testCaseName The name of the test case to stop.
 */
jstestdriver.TestRunner.TestCaseMap.prototype.stopCase = function(testCaseName) {
  this.testCases_[testCaseName] = false;
};


/**
 * Indicates if there are still cases running.
 */
jstestdriver.TestRunner.TestCaseMap.prototype.hasActiveCases = function() {
  for (var testCase in this.testCases_) {
    if (this.testCases_.hasOwnProperty(testCase) && this.testCases_[testCase]) {
      return true;
    }
  }
  return false;
};
