/*
 * Copyright 2010 Google Inc.
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

/**
 * This is a simple logger implementation, that posts messages to the server.
 * Will most likely be expanded later.
 * @param {Function} sendToServer Function to send information to the server,
 *     of signature function(logs):void
 * @param {number} 
 */
jstestdriver.BrowserLogger = function(sendToServer, id) {
  this.sendToServer_ = sendToServer;
  this.id_ = id;
};


/**
 * 
 * @param location String location of the browser.
 * @param ajax jQuery ajax function.
 * @returns jstestdriver.BrowserLogger.
 */
jstestdriver.BrowserLogger.create = function(location, ajax) {
  var id = parseInt(jstestdriver.extractId(location));
  var prefix = location.match(/^(.*)\/(slave|runner|bcr)\//)[1];
  var url = prefix + '/log';
  return new jstestdriver.BrowserLogger(function(logs) {
    ajax({
        'async' : true,
        'data' : 'logs=' + JSON.stringify(logs),
        'type' : 'POST',
        'url' : url
    });
  }, id);
};

jstestdriver.BrowserLogger.prototype.isEnabled_ = function() {
  // TODO(corysmith): Refactor to allow the runConfig to be available before
  // load.
  var enabled = jstestdriver.runConfig && jstestdriver.runConfig.debug;
  this.isEnabled_ = function () {
    return enabled;
  };
  return enabled;
};


/**
 * Logs a message to the server.
 * @param {String} source The source of the log event.
 * @param {jstestdriver.BrowserLogger.LEVEL} level The level of the message.
 * @param {String} message The log message.
 */
jstestdriver.BrowserLogger.prototype.log = function(source, level, message) {
  if (this.isEnabled_()) {
    // TODO(corysmith): replace with a cross browser stack methodology.
    var traceError = new Error();
    var stack = traceError.stack ? traceError.stack.split('\n') : [];

    var smallStack = [];

    for (var i = 0; stack[i]; i++) {
      var end = stack[i].indexOf('(');
      if (end > -1) {
        smallStack.push(stack[i].substr(0,end).trim());
      }
    }
    smallStack = smallStack.length ? smallStack : ['No stack available'];
    this.sendToServer_([
          new jstestdriver.BrowserLog(
              source,
              level,
              encodeURI(message),
              {"id": this.id_},
              encodeURI(smallStack.toString()),
              new Date())
        ]);
  }
};


jstestdriver.BrowserLogger.prototype.debug = function(source, message) {
  this.log(source, jstestdriver.BrowserLogger.LEVEL.DEBUG, message);
};


jstestdriver.BrowserLogger.prototype.info = function(source, message) {
  this.log(source, jstestdriver.BrowserLogger.LEVEL.INFO, message);
};


jstestdriver.BrowserLogger.prototype.warn = function(source, message) {
  this.log(source, jstestdriver.BrowserLogger.LEVEL.WARN, message);
};


jstestdriver.BrowserLogger.prototype.error = function(source, message) {
  this.log(source, jstestdriver.BrowserLogger.LEVEL.ERROR, message);
};


/**
 * Acceptable logging levels.
 * @enum
 */
jstestdriver.BrowserLogger.LEVEL = {
  TRACE : 1,
  DEBUG : 2,
  INFO : 3,
  WARN : 4,
  ERROR : 5
};
