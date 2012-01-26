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
package com.google.eclipse.javascript.jstestdriver.core;

import com.google.jstestdriver.model.BasePaths;

/**
 * Represents an entry for a configuration file in the workspace.
 * @author corysmith@google.com (Cory Smith)
 *
 */
public final class ConfigurationData {
  private final String configurationPath;
  private final BasePaths basePaths;
  private final String name;

  public ConfigurationData(String name, String configurationPath, BasePaths basePaths) {
    this.name = name;
    this.configurationPath = configurationPath;
    this.basePaths = basePaths;
  }

  /**
   * @return the configurationPath
   */
  public String getConfigurationPath() {
    return configurationPath;
  }

  /**
   * @return the basePaths
   */
  public BasePaths getBasePaths() {
    return basePaths;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }
}
