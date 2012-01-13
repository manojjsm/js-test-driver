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

package com.google.jstestdriver.server.handlers;

import com.google.inject.Inject;
import com.google.jstestdriver.requesthandlers.RequestHandler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Services test resource requests adding non-caching headers.
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class NonCachingTestResourceHandler implements RequestHandler {

  private static final String TIME_IN_THE_PAST = "Sat, 22 Sep 1984 00:00:00 GMT";

  private final TestResourceHandler handler;
  private final HttpServletRequest request;
  private final HttpServletResponse response;

  @Inject
  public NonCachingTestResourceHandler(TestResourceHandler handler,
      HttpServletRequest request,
      HttpServletResponse response) {
    this.handler = handler;
    this.request = request;
    this.response = response;
  }

  @Override
  public void handleIt() throws IOException {
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Cache-Control", "private, no-cache, no-store, max-age=0, must-revalidate");
    response.setHeader("Expires", TIME_IN_THE_PAST);
    handler.handleIt();
  }
}
