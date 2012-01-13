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

package com.google.jstestdriver.model;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.FileLoader;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents the changed files in a JstdTestCase.
 * 
 * @author corysmith@google.com (Cory Smith)
 */
public class JstdTestCaseDelta {

  private List<FileInfo> dependencies;
  private List<FileInfo> tests;
  private List<FileInfo> plugins;

  public JstdTestCaseDelta() {}
  
  public JstdTestCaseDelta(List<FileInfo> dependencies, List<FileInfo> tests, List<FileInfo> plugins) {
    this.dependencies = dependencies;
    this.tests = tests;
    this.plugins = plugins;
  }

  public List<FileInfo> getTests() {
    return tests;
  }

  public List<FileInfo> getDependencies() {
    return dependencies;
  }

  public List<FileInfo> getPlugins() {
    return plugins;
  }

  public JstdTestCaseDelta loadFiles(FileLoader fileLoader) {
    return new JstdTestCaseDelta(
        fileLoader.loadFiles(dependencies, false),
        fileLoader.loadFiles(tests, false),
        fileLoader.loadFiles(plugins, false));
  }

  @Override
  public String toString() {
    Function<FileInfo, String> fileInfoToPath = new Function<FileInfo, String>() {
      @Override
      public String apply(FileInfo in) {
        return "\n\t\t" + in.getFilePath();
      }
    };

    return String.format("JstdTestCaseDelta(\n\tdependencies[%s],\n\ttests[%s],\n\tplugins[%s])",
        Lists.transform(dependencies, fileInfoToPath),
        Lists.transform(tests, fileInfoToPath),
        Lists.transform(plugins, fileInfoToPath));
  }
}
