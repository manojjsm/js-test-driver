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
var VisualTestReporterTest = TestCase('VisualTestReporterTest');

VisualTestReporterTest.prototype.testParseAsynchError = function() {
  var nodes = [];
  var testCaseName = 'testCaseName';
  var testName = 'testName';
  var result = jstestdriver.TestResult.RESULT.FAILED;
  
  var message = "[{\"message\":[],\"name\":\"AssertError\",\"stack\":\"AssertError    at fail\"}]";
  var log = [];
  var time = -1;
  function createNode(tagName) {
    return new jstestdriver.MockNode(tagName);
  }
  
  function recordNode(node) {
    nodes.push(node);
    return node;
  }
  
  var reporter = new jstestdriver.VisualTestReporter(createNode,
                                                     recordNode,
                                                     jstestdriver.jQuery,
                                                     JSON.parse);
  reporter.renderResult(new jstestdriver.TestResult(testCaseName,
          testName, result, message, log, time), new jstestdriver.MockNode('div'));
}