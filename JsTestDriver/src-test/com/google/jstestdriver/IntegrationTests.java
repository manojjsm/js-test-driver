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

import java.io.File;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import junit.framework.TestCase;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.config.ConfigurationException;
import com.google.jstestdriver.config.ConfigurationParser;
import com.google.jstestdriver.config.ConfigurationSource;
import com.google.jstestdriver.embedded.JsTestDriverBuilder;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.servlet.fileset.ListTestCases;
import com.google.jstestdriver.util.NullStopWatch;

/**
 * Test for issue 308.
 * @author Cory Smith (corbinrsmith@gmail.com) 
 */
public class IntegrationTests extends TestCase {

  public void testConfigToUpload() throws Exception {
    final File depOne = File.createTempFile(this.toString(), "depOne.js");
    final File depTwo = File.createTempFile(this.toString(), "depTwo.js");
    final File depThree = File.createTempFile(this.toString(), "depThree.js");
    //final File serve = File.createTempFile(this.toString(), "serve.js");
    final File test = File.createTempFile(this.toString(), "test.js");
    final File basePath = depOne.getParentFile();
    ConfigurationSource configurationSource = new ConfigurationSource() {
      
      @Override
      public Configuration parse(BasePaths basePaths, ConfigurationParser configParser)
          throws ConfigurationException {
        return configParser.parse(new StringReader(
        "serve:\n" +
        "  - " + depThree.getPath() + "\n" +
        "load:\n" +
        "  - " + depThree.getPath() + "\n" +
        "  - " + depTwo.getPath() + "\n" +
        "  - " + depOne.getPath() + "\n" +
        "test:\n" +
        "  - " + test.getPath() + "\n"), basePaths);
      }
      
      @Override
      public File getParentFile() {
        return basePath;
      }
      
      @Override
      public String getName() {
        return null;
      }
    };
    final CountDownLatch latch = new CountDownLatch(1);
    JsTestDriver jstd = new JsTestDriverBuilder()
        .setConfigurationSource(configurationSource)
        .setPort(8080)
        .addBaseDir(basePath)
        .preloadFiles()
        .addServerListener(new ServerListener() {
          
          @Override
          public void serverStopped() {
          }
          
          @Override
          public void serverStarted() {
            latch.countDown();
          }
          
          @Override
          public void browserPanicked(BrowserInfo info) {
            
          }
          
          @Override
          public void browserCaptured(BrowserInfo info) {
          }
        })
        .build();
    jstd.startServer();
    latch.await();
    Map<String, String> params = Maps.newHashMap();
    params.put("action", ListTestCases.ACTION);
    Collection<JstdTestCase> testCases =
        new Gson().fromJson(
            new HttpServer(new NullStopWatch()).post(
                String.format("http://127.0.0.1:%s/fileSet", 8080), params),
            new TypeToken<Collection<JstdTestCase>>() {}.getType());
    for (JstdTestCase jstdTestCase : testCases) {
      List<String> expected = Lists.newArrayList(depThree.getName(), depTwo.getName(), depOne.getName(), test.getName());
      List<String> actual = Lists.transform(Lists.newArrayList(jstdTestCase), new Function<FileInfo, String>() {
        @Override
        public String apply(FileInfo file) {
          return file.getDisplayPath();
        }});
      assertEquals(expected, actual);
    }
  }
}
