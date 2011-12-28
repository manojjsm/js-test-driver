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
package com.google.jstestdriver;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.jstestdriver.browser.BrowserPanicException;
import com.google.jstestdriver.config.CmdLineFlags;
import com.google.jstestdriver.config.CmdLineFlag;
import com.google.jstestdriver.config.CmdLineFlagsFactory;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.config.ConfigurationException;
import com.google.jstestdriver.config.InitializeModule;
import com.google.jstestdriver.config.Initializer;
import com.google.jstestdriver.config.InvalidFlagException;
import com.google.jstestdriver.config.UnreadableFilesException;
import com.google.jstestdriver.config.UserConfigurationSource;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.embedded.JsTestDriverBuilder;
import com.google.jstestdriver.guice.TestResultPrintingModule.TestResultPrintingInitializer;
import com.google.jstestdriver.hooks.TestResultListener;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.model.ConcretePathPrefix;
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.runner.RunnerMode;
import com.google.jstestdriver.util.NullStopWatch;
import com.google.jstestdriver.util.RetryException;

public class JsTestDriver {

  /**
   * Used to bind a {@link TestResultListener} into the JsTestDriver framework.
   *
   * @author Cory Smith (corbinrsmith@gmail.com)
   */
  private static final class TestListenerModule implements Module {
    private final TestResultListener listener;

    /**
     * @param listener An instance of a {@link TestResultListener} to collect
     *    results during a run.
     */
    private TestListenerModule(TestResultListener listener) {
      this.listener = listener;
    }

    @Override
    public void configure(Binder binder) {
      Multibinder.newSetBinder(binder, TestResultListener.class).addBinding()
          .toInstance(listener);
    }
  }

  private static final class TestCaseCollector implements TestResultListener {
    private final List<TestCase> testCases = Lists.newLinkedList();
    
    @Override
    public void onTestRegistered(BrowserInfo browser, TestCase testCase) {
      testCases.add(testCase);
    }

    @Override
    public void onTestComplete(TestResult testResult) {
    }

    @Override
    public void onFileLoad(BrowserInfo browserInfo, FileResult fileResult) {
    }

    @Override
    public void finish() {
    }
    
    /**
     * Returns a list of collected testcases from a run.
     */
    public List<TestCase> getTestCases() {
      return testCases;
    }
  }

  private static final class TestResultCollector implements TestResultListener {

    private final List<TestResult> results = Lists.newLinkedList();

    @Override
    public void onTestRegistered(BrowserInfo browser, TestCase testCase) {
    }

    @Override
    public void onTestComplete(TestResult testResult) {
      results.add(testResult);
    }

    @Override
    public void onFileLoad(BrowserInfo browserInfo, FileResult fileResult) {
    }

    @Override
    public void finish() {
    }

    /**
     * @return the results
     */
    public List<TestResult> getResults() {
      return results;
    }
  }

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

  private static final Logger logger = LoggerFactory.getLogger(JsTestDriver.class);
  private final Configuration defaultConfiguration;
  private final PluginLoader pluginLoader;
  private final List<Module> initializerModules;
  private final String[] defaultFlags;
  private final RunnerMode runnerMode;
  private final int port;
  private final List<Module> pluginModules;
  private final BasePaths basePaths;
  private final String serverAddress;
  private final boolean raiseOnFailure;
  private final boolean preload;

  /**
   * @param configuration
   * @param pluginLoader
   * @param runnerMode
   * @param flags
   * @param pluginModules
   * @param baseDirs
   * @param serverAddress
   * @param raiseOnFailure TODO
   * @param preload 
   */
  public JsTestDriver(
      Configuration configuration,
      PluginLoader pluginLoader,
      RunnerMode runnerMode,
      String[] flags,
      int port,
      List<Module> pluginModules,
      List<Module> initializerModules,
      BasePaths basePaths,
      String serverAddress,
      boolean raiseOnFailure,
      boolean preload) {
    this.defaultConfiguration = configuration;
    this.pluginLoader = pluginLoader;
    this.initializerModules = initializerModules;
    this.runnerMode = runnerMode;
    this.defaultFlags = flags;
    this.port = port;
    this.pluginModules = pluginModules;
    this.basePaths = basePaths;
    this.serverAddress = serverAddress;
    this.raiseOnFailure = raiseOnFailure;
    this.preload = preload;
  }

  public void startServer() {
    // TODO(corysmith): refactor these to do less hacking.
    if (port == -1) {
      throw new ConfigurationException("Port not defined, cannot start local server.");
    }
    if (preload) {
      runConfigurationWithFlags(defaultConfiguration,
        createFlagsArray("--port", String.valueOf(port), "--preloadFiles"));
    } else {
      runConfigurationWithFlags(defaultConfiguration,
        createFlagsArray("--port", String.valueOf(port)));
    }
  }

  public void stopServer() {
    // TODO(corysmith): refactor these to do less hacking.
    CmdLineFlag handlerPrefixFlag = findServerHandlerPrefixFlag();
    HandlerPathPrefix prefix = new NullPathPrefix();
    if (handlerPrefixFlag != null) {
      prefix = new ConcretePathPrefix(handlerPrefixFlag.value);
    }
    StringBuilder urlBuilder =
        new StringBuilder(defaultConfiguration.getServer(serverAddress, port, prefix));
    urlBuilder.append("/");
    urlBuilder.append("quit");
    new HttpServer(new NullStopWatch()).ping(urlBuilder.toString());

  }

  /**
   * Runs the default configuration with the default flags.
   */
  public List<TestResult> runConfiguration() {
    final List<TestResult> results = Lists.newArrayList();
    runConfigurationWithFlags(defaultConfiguration, defaultFlags);
    return results;
  }

  public List<TestResult> runAllTests(String path) {
    return runAllTests(parseConfiguration(path));
  }

  public List<TestResult> runAllTests(Configuration config) {
    // TODO(corysmith): resolve how this should interact. It seems odd to resolve the server address twice, but we want to respect the configuration values.
    TestResultCollector testResultCollector = new TestResultCollector();
    String server = config.getServer(serverAddress, port, new NullPathPrefix());
    runConfigurationWithFlags(config, createFlagsArray("--tests", "all", "--server", server),
      new TestListenerModule(testResultCollector));
    return testResultCollector.getResults();
  }

  public List<TestResult> runTests(String path, List<String> tests) {
      return runTests(parseConfiguration(path), tests);
  }

  public List<TestResult> runTests(Configuration config, List<String> tests) {
    // TODO(corysmith): Refactor to avoid passing string flags.
    TestResultCollector testResultCollector = new TestResultCollector();
    String server = config.getServer(serverAddress, port, new NullPathPrefix());
    runConfigurationWithFlags(config,
      createFlagsArray("--server", server, "--tests", Joiner.on(",").join(tests)),
      new TestListenerModule(testResultCollector));
    return testResultCollector.getResults();
  }

  public void reset() {
    String server = defaultConfiguration.getServer(serverAddress, port, new NullPathPrefix());
    // TODO(corysmith): Refactor to avoid passing string flags.
    runConfigurationWithFlags(defaultConfiguration, createFlagsArray("--reset",  "--server", server));
  }

  public List<TestCase> getTestCasesFor(String path) {
    return getTestCasesFor(parseConfiguration(path));
  }

  public List<TestCase> getTestCasesFor(Configuration config) {
    TestCaseCollector testCaseCollector = new TestCaseCollector();
    String server = config.getServer(serverAddress, port, new NullPathPrefix());
    // TODO(corysmith): Refactor to avoid passing string flags.
    runConfigurationWithFlags(config, createFlagsArray("--dryRunFor", "all", "--server", server), new TestListenerModule(testCaseCollector));
    return testCaseCollector.getTestCases();
  }

  public void captureBrowser(String browserPath) {
    throw new RuntimeException("not implemented");
  }

  private String[] createFlagsArray(String...coreflags) {
    List<String> flags = Lists.newArrayList(coreflags);
    CmdLineFlag handlerPrefixFlag = findServerHandlerPrefixFlag();
    if (handlerPrefixFlag != null) {
      handlerPrefixFlag.addToArgs(flags);
    }
    flags.add("--raiseOnFailure");
    flags.add(Boolean.toString(raiseOnFailure));
    String[] flagsArray = flags.toArray(new String[flags.size()]);
    return flagsArray;
  }

  private Configuration parseConfiguration(String path) {
    File configFile = new File(path);
    if (!configFile.exists()) {
      throw new ConfigurationException("Could not find " + configFile);
    }
    UserConfigurationSource userConfigurationSource = new UserConfigurationSource(configFile);
    return userConfigurationSource.parse(basePaths, new YamlParser());
  }

  private void runConfigurationWithFlags(Configuration config,
                                         String[] flags,
                                         Module... additionalRunTimeModules) {
    if (config == null) {
      throw new ConfigurationException("Configuration cannot be null.");
    }
    List<Module> initializeModules = Lists.newArrayList(initializerModules);
    BasePaths basePaths;
    try {
      // configure logging before we start seriously processing.
      LogManager.getLogManager().readConfiguration(runnerMode.getLogConfig());
      basePaths = getPathResolver(config);
      initializeModules.add(new InitializeModule(pluginLoader, basePaths, new Args4jFlagsParser(),
          runnerMode));
    } catch (IOException e) {
      throw new ConfigurationException("Could not find " + config.getBasePaths(), e);
    }
    Injector initializeInjector = Guice.createInjector(initializeModules);

    List<Module> actionRunnerModules;
    actionRunnerModules =
        initializeInjector.getInstance(Initializer.class).initialize(pluginModules,
            config, runnerMode, flags);
    actionRunnerModules.addAll(Lists.newArrayList(additionalRunTimeModules));
    Injector injector = Guice.createInjector(actionRunnerModules);
    injector.getInstance(ActionRunner.class).runActions();
  }

  private CmdLineFlag findServerHandlerPrefixFlag() {
    // TODO(corysmith): refactor these to do less hacking.
    for (int i = 0; i < defaultFlags.length; i++) {
      if (defaultFlags[i].startsWith("--serverHandlerPrefix")) {
        String flag = defaultFlags[i];
        String value = "";
        if (i + 1 < defaultFlags.length) {
          value = defaultFlags[i + 1];
        }
        return new CmdLineFlag(flag, value);
      }
    }
    return null;
  }

  // TODO(corysmith): make this go away by resolving the multiple basePath issue.
  private BasePaths getPathResolver(Configuration config) {
    for (int i = 0; i < defaultFlags.length; i++) {
      if ("--basePath".equals(defaultFlags[i])) {
        if (i < defaultFlags.length) {
          return new BasePaths(new File(defaultFlags[i + 1]));
        }
        break;
      }
    }
    if (basePaths != null) {
      return basePaths;
    }
    return config.getBasePaths();
  }
}
