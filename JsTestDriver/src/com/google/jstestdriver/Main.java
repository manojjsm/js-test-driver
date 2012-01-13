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
import com.google.jstestdriver.browser.BrowserPanicException;
import com.google.jstestdriver.config.CmdLineFlags;
import com.google.jstestdriver.config.CmdLineFlagsFactory;
import com.google.jstestdriver.config.ConfigurationException;
import com.google.jstestdriver.config.InvalidFlagException;
import com.google.jstestdriver.config.UnreadableFilesException;
import com.google.jstestdriver.embedded.JsTestDriverBuilder;
import com.google.jstestdriver.guice.TestResultPrintingModule.TestResultPrintingInitializer;
import com.google.jstestdriver.util.RetryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.logging.LogManager;

/**
 * Main class to be executed from the command line.
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class Main {

  private static final Logger logger = LoggerFactory.getLogger(JsTestDriver.class);

  public static void main(String[] args) {
    try {
      // pre-parse parsing... These are the flags
      // that must be dealt with before we parse the flags.
      CmdLineFlags cmdLineFlags = new CmdLineFlagsFactory().create(args);
      List<Plugin> cmdLinePlugins = cmdLineFlags.getPlugins();
  
      // configure logging before we start seriously processing.
      LogManager.getLogManager().readConfiguration(cmdLineFlags.getRunnerMode().getLogConfig());
  
  
      final PluginLoader pluginLoader = new PluginLoader();
  
      // load all the command line plugins.
      final List<Module> pluginModules = pluginLoader.load(cmdLinePlugins);
      logger.debug("loaded plugins %s", pluginModules);
  
      JsTestDriverBuilder builder = new JsTestDriverBuilder();
      builder.addBasePaths(cmdLineFlags.getBasePath());
      builder.setConfigurationSource(cmdLineFlags.getConfigurationSource());
      builder.addPluginModules(pluginModules);
      builder.withPluginInitializer(TestResultPrintingInitializer.class);
      builder.setRunnerMode(cmdLineFlags.getRunnerMode());
      builder.setFlags(cmdLineFlags.getUnusedFlagsAsArgs());
      JsTestDriver jstd = builder.build();
      jstd.runConfiguration();
  
      logger.info("Finished action run.");
    } catch (InvalidFlagException e) {
      e.printErrorMessages(System.out);
      CmdLineFlags.printUsage(System.out);
      System.exit(1);
    } catch (UnreadableFilesException e) {
      System.out.println("Configuration Error: \n" + e.getMessage());
      logger.debug("Details: {}", e);
      System.exit(1);
    } catch (ConfigurationException e) {
      System.out.println("Configuration Error: \n" + e.getMessage());
      logger.debug("Details: {}", e);
      System.exit(1);
    } catch (RetryException e) {
      System.out.println("Tests failed due to unexpected environment issue: "
          + e.getCause().getMessage());
      System.exit(1);
    } catch (FailureException e) {
      System.out.println("Tests failed: " + e.getMessage());
      System.exit(1);
    } catch (BrowserPanicException e) {
      System.out.println("Test run failed due to unresponsive browser: " + e);
      System.exit(1);
    } catch (Exception e) {
      logger.debug("Error {}", e);
      e.printStackTrace();
      System.out.println("Unexpected Runner Condition: " + e.getMessage()
          + "\n Use --runnerMode DEBUG for more information.");
      System.exit(1);
    }
  }
}
