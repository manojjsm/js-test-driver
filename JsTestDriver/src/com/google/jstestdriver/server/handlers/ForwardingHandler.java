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
package com.google.jstestdriver.server.handlers;

import com.google.inject.Inject;
import com.google.jstestdriver.ForwardingMapper;
import com.google.jstestdriver.ForwardingServlet;
import com.google.jstestdriver.requesthandlers.RequestHandler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A {@link RequestHandler} that forwards requests to "/forward/*" according
 * to the {@link ForwardingMapper} mappings between referrer and destination.
 * Relies upon {@link ForwardingServlet} for this logic.
 *
 * @author rdionne@google.com (Robert Dionne)
 */
class ForwardingHandler implements RequestHandler {

  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final ForwardingServlet servlet;

  @Inject
  public ForwardingHandler(
      HttpServletRequest request,
      HttpServletResponse response,
      ForwardingServlet servlet) {
    this.request = request;
    this.response = response;
    this.servlet = servlet;
  }

  public void handleIt() throws IOException {
    try {
      servlet.service(request, response);
    } catch (ServletException e) {
      throw new RuntimeException(e);
    }
  }
}
