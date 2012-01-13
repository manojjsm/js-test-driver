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

import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.google.jstestdriver.annotations.ResponseWriter;
import com.google.jstestdriver.requesthandlers.GatewayConfiguration;
import com.google.jstestdriver.requesthandlers.HttpMethod;
import com.google.jstestdriver.requesthandlers.RequestHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 * A {@link RequestHandler} that reads a JSON configuration to update the
 * internal {@link GatewayConfiguration}.
 * @author rdionne@google.com (Robert Dionne)
 */
public class GatewayConfigurationHandler implements RequestHandler {

  private static final Logger logger =
      LoggerFactory.getLogger(GatewayConfigurationHandler.class);

  private final HttpMethod method;
  private final HttpServletRequest request;
  private final PrintWriter responseWriter;
  private final JsonParser parser;
  private final GatewayConfiguration gatewayConfiguration;

  @Inject
  public GatewayConfigurationHandler(
      HttpMethod method,
      HttpServletRequest request,
      @ResponseWriter PrintWriter responseWriter,
      JsonParser parser,
      GatewayConfiguration gatewayConfiguration) {
    this.method = method;
    this.request = request;
    this.responseWriter = responseWriter;
    this.parser = parser;
    this.gatewayConfiguration = gatewayConfiguration;
  }

  public void handleIt() throws IOException {
    if (method.equals(HttpMethod.GET)) {
      responseWriter.println(gatewayConfiguration.getGatewayConfig());
    } else {
      try {
        gatewayConfiguration.updateConfiguration(
            parser.parse(request.getReader()).getAsJsonArray());
      } catch (ServletException e) {
        logger.error("Error configuring gateway {}", e);
      }
    }
  }
}
