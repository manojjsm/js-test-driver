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
package com.google.jstestdriver.server.handlers.pages;

import static com.google.jstestdriver.server.handlers.CaptureHandler.RUNNER_TYPE;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.runner.RunnerType;
import com.google.jstestdriver.server.handlers.CaptureHandler;
import com.google.jstestdriver.util.HtmlWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Wrapper for the HttpServletRequest object with Page specific logic.
 *
 * @author corbinrsmith@gmail.com (Cory Smith)
 */
public class SlavePageRequest {

  public static final String PAGE = "page";
  public static final String ID = "id";
  public static final String MODE = "mode";
  public static final String TIMEOUT = "timeout";
  public static final String UPLOAD_SIZE = "upload_size";
  public static final String LOAD_TYPE = "load_type";
  public static final String TESTCASE_ID = "testcase_id";
  public static final String REFRESH = "refresh";

  private static final Logger logger =
      LoggerFactory.getLogger(SlavePageRequest.class);

  private final Map<String, String> parameters;
  private final HttpServletRequest request;
  private final HandlerPathPrefix prefix;
  private final CapturedBrowsers browsers;

  public SlavePageRequest(Map<String, String> parameters,
      HttpServletRequest request,
      HandlerPathPrefix prefix,
      CapturedBrowsers browsers) {
    this.parameters = parameters;
    this.request = request;
    this.prefix = prefix;
    this.browsers = browsers;

  }

  public HtmlWriter writeDTD(HtmlWriter writer) throws IOException {
    if ("strict".equals(parameters.get(MODE))) {
      writer.writeStrictDtd();
    } else {
      writer.writeQuirksDtd();
    }
    return writer;
  }
  
  public String createCaptureUrl(RunnerType type) {
    List<String> urlParts = Lists.newArrayList("/capture");
    addParameterToUrlParts(MODE, urlParts);
    addParameterToUrlParts(UPLOAD_SIZE, urlParts);
    addParameterToUrlParts(RUNNER_TYPE, urlParts);
    addParameterToUrlParts(TESTCASE_ID, urlParts);
    final String url = Joiner.on("/").join(urlParts);
    return prefix.prefixPath(Joiner.on("/").join(urlParts));
  }

  public String createCaptureUrl() {
    try {
      return createCaptureUrl(
          RunnerType.valueOf(
              parameters.get(CaptureHandler.RUNNER_TYPE).toUpperCase()));
    } catch (NullPointerException e) {
      logger.error("Invalid/No runner type specified: {} falling back to BROWSER",
          parameters.get(CaptureHandler.RUNNER_TYPE));
    }
    return createCaptureUrl(RunnerType.BROWSER);
  }

  public String createPageUrl(PageType page) {
    List<String> urlParts = Lists.<String>newArrayList("/slave");
    addToUrlParts(PAGE, page.name(), urlParts);
    addParameterToUrlParts(ID, urlParts);
    addParameterToUrlParts(MODE, urlParts);
    addParameterToUrlParts(UPLOAD_SIZE, urlParts);
    addParameterToUrlParts(TESTCASE_ID, urlParts);
    final String url = Joiner.on("/").join(urlParts);
    logger.trace("creating new url: {} for {}", url, page);
    return prefix.prefixPath(url);
  }
  
  private void addParameterToUrlParts(String key, List<String> urlParts) {
    addToUrlParts(key, parameters.get(key), urlParts);
  }

  private void addToUrlParts(String key, String value, List<String> urlParts) {
    if (value == null || value.isEmpty()) {
      return;
    }
    urlParts.add(key);
    urlParts.add(value);
  }

  public SlaveBrowser getBrowser() {
    String id = parameters.get(ID);
    if (id == null) {
      return null;
    }
    return browsers.getBrowser(id);
  }
  
  public PageType getPageType() {
    return PageType.valueOf(parameters.get(PAGE).toUpperCase());
  }

  public String getUserAgent() {
    return request.getHeader("User-Agent");
  }

  public String getTestCaseId() {
    return getParameter(TESTCASE_ID);
  }

  @Override
  public String toString() {
    return "SlavePageRequest [parameters=" + parameters + "]";
  }

  public String getParameter(String key) {
    return parameters.get(key);
  }
}
