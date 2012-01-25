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

package com.google.jstestdriver.embedded;

import com.google.inject.Module;
import com.google.inject.internal.Lists;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.FileResult;
import com.google.jstestdriver.Flags;
import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.TestResult;
import com.google.jstestdriver.config.CmdLineFlags;
import com.google.jstestdriver.config.CmdLineFlag;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.hooks.PluginInitializer;
import com.google.jstestdriver.hooks.TestListener;
import com.google.jstestdriver.runner.RunnerMode;
import com.google.jstestdriver.hooks.ServerListener;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileWriter;

/**
 * @author corysmith@google.com (Your Name Here)
 * 
 */
public class JsTestDriverBuilderTest extends TestCase {
  private File tmpDir;

  private final class TestServerListener implements ServerListener {
    public void serverStopped() {
      // TODO Auto-generated method stub

    }

    public void serverStarted() {
      // TODO Auto-generated method stub

    }

    public void browserPanicked(BrowserInfo info) {
      // TODO Auto-generated method stub

    }

    public void browserCaptured(BrowserInfo info) {
      // TODO Auto-generated method stub

    }
  }

  private static final class TestTestResultsListener implements TestListener {

    public void onTestComplete(TestResult testResult) {
      // TODO Auto-generated method stub

    }

    public void onFileLoad(BrowserInfo browser, FileResult fileResult) {
      // TODO Auto-generated method stub

    }

    public void finish() {
      // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.google.jstestdriver.hooks.TestResultListener#onTestRegistered(com.google.jstestdriver.BrowserInfo, com.google.jstestdriver.TestCase)
     */
    @Override
    public void onTestRegistered(BrowserInfo browser, com.google.jstestdriver.TestCase testCase) {
      // TODO Auto-generated method stub
      
    }

  }

  @Override
  protected void setUp() throws Exception {
    tmpDir = new File(System.getProperty("java.io.tmpdir"));
    tmpDir.mkdirs();
    tmpDir.deleteOnExit();
  }

  @Override
  protected void tearDown() throws Exception {
    tmpDir.delete();
  }

  private static class TestInitializer implements PluginInitializer {
    public Module initializeModule(Flags flags, Configuration config) {
      return PluginInitializer.NULL_MODULE;
    }
  }


  public void testBuildServer() throws Exception {
    File configuration = new File(tmpDir, "config.yml");
    FileWriter writer = new FileWriter(configuration);
    writer.append("basepath: " + configuration.getCanonicalPath());
    writer.flush();
    JsTestDriver server =
        new JsTestDriverBuilder()
            .addBaseDir(tmpDir)
            .setFlags(new String[]{"--port", "8080"})
            .setDefaultConfiguration(configuration.getAbsolutePath())
            .setRunnerMode(RunnerMode.QUIET)
            .setPort(8080)
            .addServerListener(new TestServerListener())
            .build();
  }

  public void testBuildClient() throws Exception {
    File configuration = new File(tmpDir, "config.yml");
    FileWriter writer = new FileWriter(configuration);
    writer.append("server: http://localhost:8080");
    writer.flush();
    JsTestDriver client =
        new JsTestDriverBuilder()
            .addBaseDir(tmpDir)
            .setFlags(new String[]{"--tests", "all", "--browser", "foo"})
            .setDefaultConfiguration(configuration.getAbsolutePath())
            .setServer("http://localhost:8080")
            .withPluginInitializer(TestInitializer.class)
            .addTestListener(new TestTestResultsListener())
            .build();
  }
}
