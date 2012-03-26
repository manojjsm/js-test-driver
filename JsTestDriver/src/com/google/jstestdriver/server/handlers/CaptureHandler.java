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

import static com.google.jstestdriver.runner.RunnerType.CLIENT;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.UserAgentParser;
import com.google.jstestdriver.browser.BrowserHunter;
import com.google.jstestdriver.requesthandlers.RequestHandler;
import com.google.jstestdriver.runner.RunnerType;
import com.google.jstestdriver.server.handlers.pages.SlavePageRequest;
import com.google.jstestdriver.util.ParameterParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

/**
 * "Captures" a browser by redirecting it to RemoteConsoleRunner url, and adds
 * it to the CapturedBrowsers collection.
 * 
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
// TODO(corysmith): refactor the concerns in the handler. It's doing to much again.
public class CaptureHandler implements RequestHandler {

  private static final Logger logger = LoggerFactory.getLogger(CaptureHandler.class);

  public static final String RUNNER_TYPE = "rt";
  private static final String TIMEOUT = "timeout";

  public static final String STRICT = "strict";
  public static final String QUIRKS = "quirks";

  private static final Map<String, Integer> PARAMETERS = ImmutableMap.<String, Integer>builder()
      .put(STRICT, 0)
      .put(QUIRKS, 0)
      .put(RUNNER_TYPE, 1)
      .put(SlavePageRequest.UPLOAD_SIZE, 1)
      .put(SlavePageRequest.MODE, 1)
      .put(SlavePageRequest.ID, 1)
      .put(SlavePageRequest.TIMEOUT, 1)
      .put(SlavePageRequest.TESTCASE_ID, 1)
      .build();

  private static final Set<String> BLACKLIST = ImmutableSet.<String>builder().add("capture")
      .build();

  private final SlavePageRequest request;
  private final HttpServletResponse response;
  private final BrowserHunter browserHunter;

  private final ParameterParser restParser;

  @Inject
  public CaptureHandler(SlavePageRequest request, HttpServletResponse response,
      BrowserHunter browserHunter, ParameterParser restParser) {
    this.browserHunter = browserHunter;
    this.request = request;
    this.response = response;
    this.restParser = restParser;
  }

  @Override
  public void handleIt() throws IOException {
    final Map<String, String> parameterMap = restParser.getParameterMap(PARAMETERS, BLACKLIST);
    String mode = parameterMap.get(STRICT) != null ? STRICT : QUIRKS;
    String id = parameterMap.get(SlavePageRequest.ID);
    RunnerType runnerType = parseRunnerType(parameterMap.get(RUNNER_TYPE));
    Long timeout = parseLong(parameterMap.get(TIMEOUT));
    Integer uploadSize = parseInteger(parameterMap.get(SlavePageRequest.UPLOAD_SIZE));
    String redirect = service(request.getUserAgent(), mode, id, runnerType, timeout, uploadSize);
    logger.debug("Redirecting to {}", redirect);
    response.sendRedirect(redirect);
  }

  private Integer parseInteger(String value) {
    return value == null ? null : Integer.parseInt(value);
  }

  private Long parseLong(final String value) {
    return value == null ? null : Long.parseLong(value);
  }

  private RunnerType parseRunnerType(String runnerType) {
    return runnerType == null ? CLIENT : RunnerType.valueOf(runnerType.toUpperCase());
  }

  public String service(String userAgent, String mode, String id, RunnerType runnerType,
      Long timeout, Integer uploadSize) {
    UserAgentParser parser = new UserAgentParser();

    parser.parse(userAgent);
    SlaveBrowser slaveBrowser =
        browserHunter.captureBrowser(id, parser.getName(), parser.getVersion(), parser.getOs(),
            timeout, mode, runnerType, uploadSize);
    
    return slaveBrowser.getCaptureUrl();
  }
}
