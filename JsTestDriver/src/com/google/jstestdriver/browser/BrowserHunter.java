/*
 * Copyright 2009 Google Inc.
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
package com.google.jstestdriver.browser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.Time;
import com.google.jstestdriver.annotations.BrowserTimeout;
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.runner.RunnerType;
import com.google.jstestdriver.server.handlers.CaptureHandler;

/**
 * Creates SlaveBrowsers.
 * 
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class BrowserHunter {

  private static final Logger logger = LoggerFactory.getLogger(BrowserHunter.class.getName());

  private final CapturedBrowsers capturedBrowsers;

  private final long browserTimeout;

  private final HandlerPathPrefix prefix;

  private final Time time;

  @Inject
  public BrowserHunter(CapturedBrowsers capturedBrowsers, @BrowserTimeout long browserTimeout,
      HandlerPathPrefix prefix, Time time) {
    this.capturedBrowsers = capturedBrowsers;
    this.browserTimeout = browserTimeout;
    this.prefix = prefix;
    this.time = time;
  }

  public SlaveBrowser captureBrowser(String name, String version, String os) {
    return captureBrowser(capturedBrowsers.getUniqueId(), name, version, os, browserTimeout,
        CaptureHandler.QUIRKS, RunnerType.CLIENT);
  }

  /**
   * Begins tracking a browser in the CapturedBrowsers collection.
   * 
   * @param rawId
   * @param name
   * @param version
   * @param os
   * @param mode
   * @return A slave browser
   */
  public SlaveBrowser captureBrowser(String rawId, String name, String version, String os) {
    return captureBrowser(rawId, name, version, os, browserTimeout, CaptureHandler.QUIRKS,
        RunnerType.CLIENT);
  }

  public SlaveBrowser captureBrowser(String rawId, String name, String version, String os,
      Long browserTimeout, String mode, RunnerType type) {
    BrowserInfo browserInfo = new BrowserInfo();

    final Integer id = parseBrowserId(rawId);
    browserInfo.setId(id);
    browserInfo.setName(name);
    browserInfo.setVersion(version);
    browserInfo.setOs(os);
    // TODO(corysmith):move all browser timeout configuration to the capture
    // servlet.
    long computedBrowserTimeout = computeTimeout(browserTimeout);
    SlaveBrowser slave =
        new SlaveBrowser(time, id.toString(), browserInfo, computedBrowserTimeout, prefix, mode,
            type);

    capturedBrowsers.addSlave(slave);
    logger.debug("Browser Captured: {}", slave);
    logger.info("Browser Captured: {} version {} ({})", new Object[] {name, version, id});
    return slave;
  }

  private long computeTimeout(Long browserTimeout) {
    return browserTimeout == null ? this.browserTimeout : browserTimeout;
  }

  private Integer parseBrowserId(String id) {
    if (id == null) {
      return Integer.parseInt(capturedBrowsers.getUniqueId());
    }
    return Integer.valueOf(id);
  }

  public boolean isBrowserCaptured(String id) {
    return capturedBrowsers.getBrowser(id) != null;
  }

  public void freeBrowser(String id) {
    capturedBrowsers.removeSlave(id);
  }
}