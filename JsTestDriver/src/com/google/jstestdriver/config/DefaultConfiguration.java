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
package com.google.jstestdriver.config;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.Flags;
import com.google.jstestdriver.PathResolver;
import com.google.jstestdriver.Plugin;
import com.google.jstestdriver.browser.DocType;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.model.HandlerPathPrefix;

/**
 * A simple empty configuration. Commonly used for starting the server.
 * @author corysmith@google.com (Cory Smith)
 */
public class DefaultConfiguration implements Configuration{

  public static final long DEFAULT_TEST_TIMEOUT = 2 * 60 * 60;
  private final BasePaths basePaths;

  public DefaultConfiguration(BasePaths basePaths) {
    this.basePaths = basePaths;
  }

  @Override
  public Set<FileInfo> getFilesList() {
    return Collections.<FileInfo>emptySet();
  }

  @Override
  public List<Plugin> getPlugins() {
    return Collections.<Plugin>emptyList();
  }

  @Override
  public String getServer(String flagValue, int port, HandlerPathPrefix handlerPrefix) {
    if (flagValue != null && !flagValue.isEmpty()) {
      return handlerPrefix.suffixServer(flagValue);
    }

    if (port == -1) {
      throw new RuntimeException("Oh Snap! No server defined!");
    }
    return handlerPrefix.suffixServer(String.format("http://%s:%d", "127.0.0.1", port));
  }

  @Override
  public String getCaptureAddress(String server, String captureAddress,
      HandlerPathPrefix prefix) {
    if (captureAddress != null && !captureAddress.isEmpty()) {
      return prefix.suffixServer(captureAddress);
    } else {
      return server;
    }
  }

  @Override
  public Configuration resolvePaths(PathResolver resolver, Flags flags) {
    return this;
  }

  @Override
  public long getTestSuiteTimeout() {
    return DEFAULT_TEST_TIMEOUT; // two hours. Should be enough to debug.
  }

  @Override
  public List<FileInfo> getTests() {
    return Collections.<FileInfo>emptyList();
  }

  @Override
  public BasePaths getBasePaths() {
    return basePaths;
  }

  @Override
  public JsonArray getGatewayConfiguration() {
    return new JsonArray();
  }

  @Override
  public DocType getDocType() {
    // TODO Auto-generated method stub
    return null;
  }
}
