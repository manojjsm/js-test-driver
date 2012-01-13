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
import com.google.jstestdriver.requesthandlers.RequestHandler;

import org.mortbay.resource.Resource;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * A {@link RequestHandler} that provides browsers with the JSTD favicon.
 *
 * @author rdionne@google.com (Robert Dionne)
 */
class FaviconHandler implements RequestHandler {

  private static final String IMAGE_PNG = "image/png";
  private static final String JS_TEST_DRIVER_PNG = "/com/google/jstestdriver/JsTestDriver.png";
  
  private final HttpServletResponse response;
  private Resource favicon;

  @Inject
  public FaviconHandler(HttpServletResponse response) {
    this.response = response;
  }

  public void handleIt() throws IOException {
    response.setContentType(IMAGE_PNG);
    if (favicon == null) {
      favicon = Resource.newClassPathResource(JS_TEST_DRIVER_PNG);
    }
    favicon.writeTo(response.getOutputStream(), 0, favicon.length());
  }
}
