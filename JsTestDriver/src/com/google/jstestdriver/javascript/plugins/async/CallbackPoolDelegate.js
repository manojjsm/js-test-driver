/*
 * Copyright 2011 Google Inc.
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
 * @fileoverview Defines the CallbackPoolDelegate class. Encapsulates a
 * CallbackPool behind a narrower interface. Also, validates arguments.
 *
 * @author rdionne@google.com (Robert Dionne)
 */

goog.provide('jstestdriver.plugins.async.CallbackPoolDelegate');

goog.require('jstestdriver');

/**
 * Constructs a CallbackPoolDelegate.
 * @param {jstestdriver.plugins.async.CallbackPool} pool The pool.
 * @constructor
 * @export
 */
jstestdriver.plugins.async.CallbackPoolDelegate = function(pool) {
  this.pool_ = pool;
};


/**
 * Adds a callback to the pool.
 * @param {Object|Function} callback The callback to wrap.
 * @param {number=} opt_n An optional number of times to wait for the callback to
 *     be called.
 * @param {number=} opt_timeout The timeout in milliseconds.
 * @param {string=} opt_description The callback description.
 * @return {Function} The wrapped callback.
 * @export
 */
jstestdriver.plugins.async.CallbackPoolDelegate.prototype.addCallback = function(
    callback, opt_n, opt_timeout, opt_description) {
  if (typeof callback == 'object') {
    var params = callback;
    callback = params['callback'];
    opt_n = params['invocations'];
    opt_timeout = params['timeout'] ? params['timeout'] * 1000 : undefined;
    opt_description = params['description'];
  }

  if (typeof callback == 'function' && callback) {
    return this.pool_.addCallback(
        callback, opt_n, opt_timeout, opt_description);
  }

  return null;
};


/**
 * @return {Function} An errback function to attach to an asynchronous system so
 *     that the test runner can be notified in the event of error.
 * @param {string} message A message to report to the user upon error.
 * @export
 */
jstestdriver.plugins.async.CallbackPoolDelegate.prototype.addErrback = function(
    message) {
  return this.pool_.addErrback(message);
};


/**
 * Adds a callback to the pool.
 * @param {Object|Function} callback The callback to wrap.
 * @param {number=} opt_n An optional number of times to wait for the callback to
 *     be called.
 * @param {number=} opt_timeout The timeout in milliseconds.
 * @param {string=} opt_description The callback description.
 * @return {Function} The wrapped callback.
 * @export
 * @deprecated Use addCallback().
 */
jstestdriver.plugins.async.CallbackPoolDelegate.prototype.add =
    jstestdriver.plugins.async.CallbackPoolDelegate.prototype.addCallback;


/**
 * A no-op callback that's useful for waiting until an asynchronous operation
 * completes without performing any action.
 * @param {Object|number=} opt_n An optional number of times to wait for the
 *     callback to be called.
 * @param {number=} opt_timeout The timeout in milliseconds.
 * @param {string=} opt_description The description.
 * @return {Function} A noop callback.
 * @export
 * @deprecated Use wait().
 */
jstestdriver.plugins.async.CallbackPoolDelegate.prototype.noop = function(
    opt_n, opt_timeout, opt_description) {
  if (typeof opt_n == 'object') {
    var params = opt_n;
    opt_timeout = params['timeout'] ? params['timeout'] * 1000 : undefined;
    opt_description = params['description'];
    opt_n = params['invocations'];
  }
  return this.pool_.addCallback(
      jstestdriver.EMPTY_FUNC, opt_n, opt_timeout, opt_description);
};


/**
 * Adds an empty callback to the queue.
 * @return {Function} The callback.
 * @export
 */
jstestdriver.plugins.async.CallbackPoolDelegate.prototype.wait =
    jstestdriver.plugins.async.CallbackPoolDelegate.prototype.noop;
