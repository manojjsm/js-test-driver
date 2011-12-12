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

goog.provide('jstestdriver.TestCaseInfo');

goog.require('jstestdriver');
goog.require('jstestdriver.TestRunFilter');

/**
 * @param {string} testCaseName
 * @param {Function} template
 * @param {string} opt_type
 * @param {string} opt_fileName
 * @constructor
 */
jstestdriver.TestCaseInfo = function(testCaseName,
                                     template,
                                     opt_type,
                                     opt_fileName) {

  this.testCaseName_ = testCaseName;
  this.template_ = template;
  this.type_ = opt_type || jstestdriver.TestCaseInfo.DEFAULT_TYPE;
  this.fileName_ = opt_fileName || '';
};


jstestdriver.TestCaseInfo.DEFAULT_TYPE = 'default';


jstestdriver.TestCaseInfo.ASYNC_TYPE = 'async';


/**
 * @private
 * @type {string}
 */
jstestdriver.TestCaseInfo.prototype.testCaseName_;


/**
 * @private
 * @type {Function}
 */
jstestdriver.TestCaseInfo.prototype.template_;


/**
 * @private
 * @type {string}
 */
jstestdriver.TestCaseInfo.prototype.type_;


/**
 * @private
 * @type {string}
 */
jstestdriver.TestCaseInfo.prototype.fileName_;


/**
 * @return {string}
 */
jstestdriver.TestCaseInfo.prototype.getType = function() {
  return this.type_;
};


/**
 * @returns {string}
 */
jstestdriver.TestCaseInfo.prototype.getFileName = function() {
  return this.fileName_;
};


/**
 * @param {string} fileName
 */
jstestdriver.TestCaseInfo.prototype.setFileName = function(fileName) {
  this.fileName_ = fileName;
};


/**
 * @returns {string}
 */
jstestdriver.TestCaseInfo.prototype.getTestCaseName = function() {
  return this.testCaseName_;
};


/**
 * @returns {Function}
 */
jstestdriver.TestCaseInfo.prototype.getTemplate = function() {
  return this.template_;
};


/**
 * @returns {Array.<string>}
 */
jstestdriver.TestCaseInfo.prototype.getTestNames = function() {
  var testNames = [];

  for (var property in this.template_.prototype) {
    if (property.indexOf('test') == 0) {
      testNames.push(property);
    }
  }
  return testNames;
};


/**
 * @returns {jstestdriver.TestRunConfiguration}
 */
jstestdriver.TestCaseInfo.prototype.getDefaultTestRunConfiguration = function() {
  return new jstestdriver.TestRunFilter(this).getDefaultTestRunConfiguration();
};


/**
 * Includes and excludes tests based on the given expressions. Expressions are
 * of the form:
 *
 * Expr:
 *   "all" | RegExp | -RegExp
 *
 * RegExp:
 *   A JavaScript regular expression without the quoting characters.
 *
 * @param expressions {Array.<string>} The expression strings.
 */
jstestdriver.TestCaseInfo.prototype.getTestRunConfigurationFor = function(expressions) {
  return new jstestdriver.TestRunFilter(this).getTestRunConfigurationFor(expressions);
};

/**
 * @param {Object} obj
 * @returns {boolean}
 */
jstestdriver.TestCaseInfo.prototype.equals = function(obj) {
  return (!!obj) && typeof obj.getTestCaseName != 'undefined'
      && obj.getTestCaseName() == this.testCaseName_;
};


/**
 * @returns {string}
 */
jstestdriver.TestCaseInfo.prototype.toString = function() {
  return "TestCaseInfo(" +
    this.testCaseName_ +
    "," + this.template_ +
    "," + this.type_ + ")";
};
