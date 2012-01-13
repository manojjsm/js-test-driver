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

package com.google.jstestdriver.hooks;

/**
 * Provides a destination address in the form of a URL {@link String}, e.g.
 * "http://localhost:55555", for the JsTestDriver server to forward unhandled
 * requests.  Useful for system tests that interact with a server environment.
 *
 * @author rdionne@google.com (Robert Dionne)
 * @deprecated Please update your jsTestDriver.conf with a proxy spec instead.
 */
public interface ProxyDestination {

  /**
   * @return the address of the server-under-test
   */
  String getDestinationAddress();
}
