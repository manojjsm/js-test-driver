/*
 * Copyright 2012 Google Inc.
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

package com.google.jstestdriver;

import com.google.inject.Module;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.runner.RunnerMode;

import java.util.List;

/**
 * @author  corysmith@google.com (Cory Smith)
 */
public class JsTestDriverData {
  /**
   * 
   */
  public Configuration defaultConfiguration;
  /**
   * 
   */
  public PluginLoader pluginLoader;
  /**
   * 
   */
  public List<Module> initializerModules;
  /**
   * 
   */
  public String[] defaultFlags;
  /**
   * 
   */
  public RunnerMode runnerMode;
  /**
   * 
   */
  public int port;
  /**
   * 
   */
  public List<Module> pluginModules;
  /**
   * 
   */
  public BasePaths basePaths;
  /**
   * 
   */
  public String serverAddress;
  /**
   * 
   */
  public boolean raiseOnFailure;
  /**
   * 
   */
  public boolean preload;
  /**
   * 
   */
  public FlagsParser flagsParser;

  /**
   * 
   */
  public JsTestDriverData() {
  }
}