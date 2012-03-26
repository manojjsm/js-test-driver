/*
 * Copyright 2011 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the 'License'); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */



jstestdriver.ManualScriptLoader = function(win, testCaseManager, now) {
  this.win_ = win;
  this.testCaseManager_ = testCaseManager;
  this.now_ = now;
  this.onFileLoaded_ = null;
  this.started_ = -1;
  this.file_ = null;
  this.fileMap_ = {};
  this.errorHandler_ = this.createErrorHandler();
};


jstestdriver.ManualScriptLoader.prototype.beginLoad = function(file, onFileLoaded) {
  this.fileMap_[file.fileSrc] = file;
  this.testCaseManager_.removeTestCaseForFilename(file.fileSrc);
  this.file_ = file;
  this.win_.onerror = this.errorHandler_;
  this.started_ = this.now_();
  this.onFileLoaded_ = onFileLoaded;
  jstestdriver.log('loading ' + file.fileSrc);
};


jstestdriver.ManualScriptLoader.prototype.endLoad = function(file) {
  var elapsed = this.now_() - this.started_;
  if (elapsed > 50) {
    jstestdriver.log('slow load ' + this.file_.fileSrc + ' in ' + elapsed);
  }
  this.testCaseManager_.updateLatestTestCase(file.fileSrc);
  var result = new jstestdriver.FileResult(file,
                                           true,
                                           '',
                                           this.now_() - this.started_);
  this.onFileLoaded_(result);
};


jstestdriver.ManualScriptLoader.prototype.createErrorHandler = function() {
  var self = this;
  return function (msg, url, line) {
    var offset = url.indexOf('/test/')
    var fileSrc = offset > -1 ? url.substr(offset, url.length - offset) : url;
    var loadingFile = self.fileMap_[fileSrc];
    jstestdriver.log('failed load ' + fileSrc + ' in ' +
        (self.now_() - self.started_));
    var started = self.started_;
    self.started_ = -1;
    var loadMsg = 'error loading file: ' + fileSrc;

    if (line != undefined && line != null) {
      loadMsg += ':' + line;
    }

    if (msg != undefined && msg != null) {
      loadMsg += ': ' + msg;
    }
    self.win_.onerror = jstestdriver.EMPTY_FUNC;
    self.onFileLoaded_(new jstestdriver.FileResult(loadingFile, false, loadMsg, self.now_() - started));
  }
};
