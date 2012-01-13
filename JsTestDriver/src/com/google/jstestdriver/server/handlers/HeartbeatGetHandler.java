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

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestParameters;
import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.requesthandlers.RequestHandler;

import org.mortbay.jetty.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * Used by the client to know if the browser is alive.
 *
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
class HeartbeatGetHandler implements RequestHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(
      HeartbeatGetHandler.class);

  private final CapturedBrowsers capturedBrowsers;
  private final Map<String, String[]> parameters;
  private final Gson gson;

  private final HttpServletResponse response;

  @Inject
  public HeartbeatGetHandler(
      CapturedBrowsers capturedBrowsers,
      @RequestParameters Map<String, String[]> parameters,
      Gson gson,
      HttpServletResponse response) {
    this.capturedBrowsers = capturedBrowsers;
    this.parameters = parameters;
    this.gson = gson;
    this.response = response;
  }

  public void handleIt() throws IOException {
    response.setContentType(MimeTypes.TEXT_PLAIN_UTF_8);
    String[] ids = parameters.get("id");
    final PrintWriter writer = response.getWriter();
    if (ids != null && ids[0] != null) {
      String id = ids[0];
      SlaveBrowser browser = capturedBrowsers.getBrowser(id);
      if (browser != null) {
        LOGGER.debug("requesting " + browser);
        if (!browser.isAlive()) {
          capturedBrowsers.removeSlave(id);
          writer.write("DEAD:" + gson.toJson(browser.getBrowserInfo()));
        } else {
          writer.write("OK");
        }
      } else {
        LOGGER.debug("heartbeat " + id + "with no browser.");
        writer.write("DEAD: can't find browser.");
      }
    } else {
      LOGGER.debug("no heartbeat, no browser.");
      writer.write("DEAD");
    }
    writer.flush();
  }
}
