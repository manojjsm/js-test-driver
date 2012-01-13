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

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Services test resource requests adding caching headers.
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class CachingTestResourceHandler implements RequestHandler {
  private static final Logger logger = LoggerFactory.getLogger(CachingTestResourceHandler.class);

  private static final DateFormat RFC1123_PATTERN =
      new SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z", Locale.US);

  private final TestResourceHandler handler;
  private final HttpServletRequest request;
  private final HttpServletResponse response;

  @Inject
  public CachingTestResourceHandler(TestResourceHandler handler,
      HttpServletRequest request,
      HttpServletResponse response) {
    this.handler = handler;
    this.request = request;
    this.response = response;
  }

  @Override
  public void handleIt() throws IOException {
    logger.trace("handling {} with headers {}", request.getPathInfo().substring(1), Collections.list(request.getHeaderNames()));
    // tests requesting the file in short succession
    response.setHeader("Cache-Control", "max-age=1800");
    response.setHeader("Last-Modified",
        RFC1123_PATTERN.format(new DateTime().minusMinutes(1).toDate()));
    handler.handleIt();
  }
}
