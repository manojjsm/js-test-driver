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

goog.provide('jstestdriver.TestRunConfiguration');

goog.require('jstestdriver');
goog.require('jstestdriver.TestCaseInfo');

/**
 * Represents all of the necessary information to run a test case.
 * @param {jstestdriver.TestCaseInfo} testCaseInfo The test case information, containing
 * @param {Array.<string>} tests The names of all the tests to run.
 * @param {Object.<string, *>=} opt_args The arguments for the tests.
 * @constructor
 */
jstestdriver.TestRunConfiguration = function(testCaseInfo, tests, opt_args) {
  /**
   * @type {jstestdriver.TestCaseInfo}
   * @private
   */
  this.testCaseInfo_ = testCaseInfo;
  /**
   * @type {Array.<string>}
   * @private
   */
  this.tests_ = tests;
  /**
   * @type {Object.<string, *>}
   * @private
   */
  this.arguments_ = opt_args ? opt_args : null;
};


jstestdriver.TestRunConfiguration.prototype.getTestCaseInfo = function() {
  return this.testCaseInfo_;
};


jstestdriver.TestRunConfiguration.prototype.getTests = function() {
  return this.tests_;
};


/**
 * @return {Object.<string, *>} the arguments.
 */
jstestdriver.TestRunConfiguration.prototype.getArguments = function() {
  return this.arguments_;
};
