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
import com.google.inject.servlet.RequestParameters;
import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.annotations.ResponseWriter;
import com.google.jstestdriver.requesthandlers.RequestHandler;

import org.mortbay.jetty.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Used by the browser to report if it is still alive.
 *
 * @author rdionne@google.com (Robert Dionne)
 */
class HeartbeatPostHandler implements RequestHandler {

  private static final Logger logger = LoggerFactory.getLogger(
      HeartbeatPostHandler.class);

  private final CapturedBrowsers capturedBrowsers;
  private final Map<String, String[]> parameters;
  private final PrintWriter writer;

  private final HttpServletResponse response;

  @Inject
  public HeartbeatPostHandler(
      CapturedBrowsers capturedBrowsers,
      @RequestParameters Map<String, String[]> parameters,
      @ResponseWriter PrintWriter writer,
      HttpServletResponse response) {
    this.capturedBrowsers = capturedBrowsers;
    this.parameters = parameters;
    this.writer = writer;
    this.response = response;
  }

  public void handleIt() throws IOException {
    response.setContentType(MimeTypes.TEXT_PLAIN_UTF_8);
    String[] ids = parameters.get("id");
    if (ids != null && ids[0] != null) {
      String id = ids[0];
      SlaveBrowser browser = capturedBrowsers.getBrowser(id);

      if (browser != null) {
        browser.heartBeat();
        logger.trace("browser heartbeat {}", browser);
        if (browser.getCommandRunning() == null) {
          writer.write("Waiting...");
        } else {
          writer.write("Running: " + browser.getCommandRunning());
        }
      } else {
        writer.write("UNKNOWN");
      }
      writer.flush();
    }
  }
}