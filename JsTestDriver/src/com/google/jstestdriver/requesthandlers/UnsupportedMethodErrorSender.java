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

import com.google.inject.Inject;
import com.google.jstestdriver.annotations.RequestProtocol;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

/**
 * An object that sends the appropriate HTTP error response when a client
 * requests a given resource using the incorrect HTTP method.
 *
 * Modeled after what {@link HttpServlet} does.
 *
 * @author rdionne@google.com (Robert Dionne)
 */
class UnsupportedMethodErrorSender {

  private static final String MESSAGE = "HTTP method %s is not supported by this URL";

  private static final String ONE_ONE = "1.1";

  private final HttpMethod requestMethod;
  private final String requestProtocol;
  private final HttpServletResponse response;

  @Inject
  public UnsupportedMethodErrorSender(
      HttpMethod requestMethod, @RequestProtocol String requestProtocol, HttpServletResponse response) {
    this.requestMethod = requestMethod;
    this.requestProtocol = requestProtocol;
    this.response = response;
  }

  /**
   * Send the appropriate error code based on the client's HTTP version.
   * @throws IOException
   */
  public void methodNotAllowed() throws IOException {
    if (requestProtocol.endsWith(ONE_ONE)) {
      sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    } else {
      sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
  }

  private void sendError(int errorCode) throws IOException {
    response.sendError(errorCode, String.format(MESSAGE, requestMethod));
  }
}
