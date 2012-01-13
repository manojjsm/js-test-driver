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
import com.google.inject.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A @RequestScoped object that dispatches the request to the appropriate
 * {@link RequestHandler} whose {@link RequestMatcher} matches first.
 *
 * @author rdionne@google.com (Robert Dionne)
 */
class RequestDispatcher {
  private static final Logger logger =
      LoggerFactory.getLogger(RequestDispatcher.class);

  private final HttpServletRequest request;
  private final HttpServletResponse response;
  private final List<RequestMatcher> matchers;
  private final Map<RequestMatcher, Provider<RequestHandler>> handlerProviders;
  private GatewayConfiguration gatewayConfiguration;
  private final UnsupportedMethodErrorSender errorSender;

  @Inject
  public RequestDispatcher(
      HttpServletRequest request,
      HttpServletResponse response,
      List<RequestMatcher> matchers,
      Map<RequestMatcher, Provider<RequestHandler>> handlerProviders,
      GatewayConfiguration gatewayConfiguration,
      UnsupportedMethodErrorSender errorSender) {
    this.request = request;
    this.response = response;
    this.matchers = matchers;
    this.handlerProviders = handlerProviders;
    this.gatewayConfiguration = gatewayConfiguration;
    this.errorSender = errorSender;
  }

  /**
   * Dispatches the request to the {@link RequestHandler} associated with the
   * first matching {@link RequestMatcher} based on the request's HTTP method
   * and URI.
   * 
   * @throws IOException
   */
  public void dispatch() throws IOException {
    try {
      HttpMethod method = HttpMethod.valueOf(request.getMethod());
      String uri = request.getRequestURI();
      boolean pathMatched = false;
      
      for (RequestMatcher matcher : matchers) {
        if (matcher.uriMatches(uri)) {
          pathMatched = true;
          if (matcher.methodMatches(method)) {
            logger.trace("handling {} {}", uri, request);
            handlerProviders.get(matcher).get().handleIt();
            return;
          }
        }
      }
      for (RequestMatcher matcher : gatewayConfiguration.getMatchers()) {
        if (matcher.uriMatches(uri)) {
          pathMatched = true;
          if (matcher.methodMatches(method)) {
            logger.trace("gatewaying {} {}", uri, request);
            gatewayConfiguration.getRequestHandler(matcher).handleIt();
            return;
          }
        }
      }
      if (pathMatched) {
        errorSender.methodNotAllowed();
      } else {
        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found.");
      }
    } catch (IllegalArgumentException e) {
      logger.error("Error in request {}", e);
      errorSender.methodNotAllowed();
    } catch (Exception e) {
      logger.error("Error in request {}", e);
    }
  }
}
