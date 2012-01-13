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

package com.google.jstestdriver;

import com.google.gson.JsonArray;
import com.google.jstestdriver.browser.DocType;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.config.ConfigurationException;
import com.google.jstestdriver.config.ConfigurationParser;
import com.google.jstestdriver.config.ConfigurationSource;
import com.google.jstestdriver.config.DefaultConfiguration;
import com.google.jstestdriver.config.UserConfigurationSource;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.embedded.JsTestDriverBuilder;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.runner.RunnerMode;

import junit.framework.TestCase;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

/**
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class JsTestDriverTest extends TestCase {

  private final class TestListener implements ServerListener {
    boolean serverStopped;
    boolean serverStarted;
    private final CountDownLatch startLatch;
    private final CountDownLatch stopLatch;

    /**
     * @param startLatch
     * @param stopLatch
     */
    public TestListener(CountDownLatch startLatch, CountDownLatch stopLatch) {
      this.startLatch = startLatch;
      this.stopLatch = stopLatch;
    }

    @Override
    public void serverStopped() {
      serverStopped = true;
      stopLatch.countDown();
    }

    @Override
    public void serverStarted() {
      serverStarted = true;
      startLatch.countDown();
    }

    @Override
    public void browserPanicked(BrowserInfo info) {
      
    }

    @Override
    public void browserCaptured(BrowserInfo info) {
      
    }
  }

  private final class TestConfiguration extends DefaultConfiguration {
    public TestConfiguration(File basePath) {
      super(new BasePaths(basePath));
    }
  }

  public void testServerStartAndStop() throws Exception {
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch stopLatch = new CountDownLatch(1);
    TestListener testServerListener = new TestListener(startLatch, stopLatch);
    JsTestDriver jstd = new JsTestDriverBuilder()
        .setDefaultConfiguration(new TestConfiguration(new File(".")))
        .setRunnerMode(RunnerMode.DEBUG)
        .addServerListener(testServerListener)
        .setPort(8081)
        .build();
    jstd.startServer();
    startLatch.await(10, TimeUnit.SECONDS);
    assertTrue(testServerListener.serverStarted);
    jstd.stopServer();
    stopLatch.await(10, TimeUnit.SECONDS);
    assertTrue(testServerListener.serverStopped);
  }
  
  public void a_testDryRun() throws Exception {
    CountDownLatch startLatch = new CountDownLatch(1);
    CountDownLatch stopLatch = new CountDownLatch(1);
    TestListener testServerListener = new TestListener(startLatch, stopLatch);
    JsTestDriver jstd = new JsTestDriverBuilder()
        .setDefaultConfiguration(new TestConfiguration(new File(".")))
        .setRunnerMode(RunnerMode.DEBUG)
        .addServerListener(testServerListener)
        .setPort(8082)
        .build();
    jstd.startServer();
    startLatch.await(10, TimeUnit.SECONDS);
    assertTrue(testServerListener.serverStarted);
    Configuration configuration = new UserConfigurationSource(new File("./jsTestDriver.conf")).parse(new BasePaths(new File(".")), new YamlParser());
    jstd.getTestCasesFor(configuration);
    jstd.stopServer();
    stopLatch.await(10, TimeUnit.SECONDS);
    assertTrue(testServerListener.serverStopped);
  }
}
