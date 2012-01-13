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
package com.google.jstestdriver.requesthandlers;

/**
 * An enumeration of supported HTTP methods.
 *
 * See http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html#sec9
 *
* @author rdionne@google.com (Robert Dionne)
*/
public enum HttpMethod {

  /**
   * ANY is a wildcard that matches any other method.
   */
  ANY,
  DELETE,
  GET,
  HEAD,
  OPTIONS,
  POST,
  PUT,
  TRACE
}
