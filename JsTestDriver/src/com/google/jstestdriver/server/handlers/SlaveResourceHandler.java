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
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.requesthandlers.RequestHandler;
import com.google.jstestdriver.server.handlers.pages.Page;
import com.google.jstestdriver.server.handlers.pages.PageType;
import com.google.jstestdriver.server.handlers.pages.SlavePageRequest;
import com.google.jstestdriver.util.HtmlWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

/**
 * @author rdionne@google.com (Robert Dionne)
 */
class SlaveResourceHandler implements RequestHandler {
  private static final Logger logger =
      LoggerFactory.getLogger(SlaveResourceHandler.class);

  private final Map<PageType, Page> pages;

  private final SlavePageRequest request;

  private final HandlerPathPrefix prefix;

  private final HttpServletResponse response;


  @Inject
  public SlaveResourceHandler(
      HttpServletResponse response,
      Map<PageType, Page> pages,
      SlavePageRequest request,
      HandlerPathPrefix prefix) {
    this.response = response;
    this.pages = pages;
    this.request = request;
    this.prefix = prefix;
  }

  public void handleIt() throws IOException {
    logger.debug("Handling " + request);
    final HtmlWriter writer = new HtmlWriter(response.getWriter(), prefix);
    request.writeDTD(writer);
    pages.get(request.getPageType()).render(writer, request);
  }
}
