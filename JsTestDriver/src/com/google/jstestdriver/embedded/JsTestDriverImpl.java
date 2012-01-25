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
package com.google.jstestdriver.embedded;


import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.jstestdriver.ActionRunner;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.FileResult;
import com.google.jstestdriver.FlagsParser;
import com.google.jstestdriver.HttpServer;
import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.PluginLoader;
import com.google.jstestdriver.TestCase;
import com.google.jstestdriver.TestResult;
import com.google.jstestdriver.config.CmdLineFlag;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.config.ConfigurationException;
import com.google.jstestdriver.config.InitializeModule;
import com.google.jstestdriver.config.Initializer;
import com.google.jstestdriver.config.UserConfigurationSource;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.hooks.JsTestDriverValidator;
import com.google.jstestdriver.hooks.TestListener;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.model.ConcretePathPrefix;
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.runner.RunnerMode;
import com.google.jstestdriver.util.NullStopWatch;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.LogManager;

public class JsTestDriverImpl implements JsTestDriver {

  /**
   * Used to bind a {@link TestListener} into the JsTestDriver framework.
   *
   * @author Cory Smith (corbinrsmith@gmail.com)
   */
  private static final class TestListenerModule implements Module {
    private final TestListener listener;

    /**
     * @param listener An instance of a {@link TestListener} to collect
     *    results during a run.
     */
    private TestListenerModule(TestListener listener) {
      this.listener = listener;
    }

    @Override
    public void configure(Binder binder) {
      Multibinder.newSetBinder(binder, TestListener.class).addBinding()
          .toInstance(listener);
    }
  }

  private static final class TestCaseCollector implements TestListener {
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

  private static final class TestResultCollector implements TestListener {

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

  private Configuration defaultConfiguration;
  private PluginLoader pluginLoader;
  private List<Module> initializerModules;
  private RunnerMode runnerMode;
  private String[] defaultFlags;
  private int port;
  private List<Module> pluginModules;
  private BasePaths basePaths;
  private String serverAddress;
  private boolean raiseOnFailure;
  private boolean preload;
  private FlagsParser flagsParser;

  /**
   * @param configuration
   * @param pluginLoader
   * @param runnerMode
   * @param flags
   * @param pluginModules
   * @param basePaths
   * @param serverAddress
   * @param raiseOnFailure TODO
   * @param preload 
   * @param flagsParser 
   */
  public JsTestDriverImpl(
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
      boolean preload,
      FlagsParser flagsParser) {
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
    this.flagsParser = flagsParser;
  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.IJsTestDriver#startServer()
   */
  @Override
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

  /* (non-Javadoc)
   * @see com.google.jstestdriver.IJsTestDriver#stopServer()
   */
  @Override
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

  /* (non-Javadoc)
   * @see com.google.jstestdriver.IJsTestDriver#runConfiguration()
   */
  @Override
  public List<TestResult> runConfiguration() {
    final List<TestResult> results = Lists.newArrayList();
    runConfigurationWithFlags(defaultConfiguration, defaultFlags);
    return results;
  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.IJsTestDriver#runAllTests(java.lang.String)
   */
  @Override
  public List<TestResult> runAllTests(String path) {
    return runAllTests(parseConfiguration(path));
  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.IJsTestDriver#runAllTests(com.google.jstestdriver.config.Configuration)
   */
  @Override
  public List<TestResult> runAllTests(Configuration config) {
    // TODO(corysmith): resolve how this should interact. It seems odd to resolve the server address twice, but we want to respect the configuration values.
    TestResultCollector testResultCollector = new TestResultCollector();
    String server = config.getServer(serverAddress, port, new NullPathPrefix());
    runConfigurationWithFlags(config, createFlagsArray("--tests", "all", "--server", server),
      new TestListenerModule(testResultCollector));
    return testResultCollector.getResults();
  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.IJsTestDriver#runTests(java.lang.String, java.util.List)
   */
  @Override
  public List<TestResult> runTests(String path, List<String> tests) {
      return runTests(parseConfiguration(path), tests);
  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.IJsTestDriver#runTests(com.google.jstestdriver.config.Configuration, java.util.List)
   */
  @Override
  public List<TestResult> runTests(Configuration config, List<String> tests) {
    // TODO(corysmith): Refactor to avoid passing string flags.
    TestResultCollector testResultCollector = new TestResultCollector();
    String server = config.getServer(serverAddress, port, new NullPathPrefix());
    runConfigurationWithFlags(config,
      createFlagsArray("--server", server, "--tests", Joiner.on(",").join(tests)),
      new TestListenerModule(testResultCollector));
    return testResultCollector.getResults();
  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.IJsTestDriver#reset()
   */
  @Override
  public void reset() {
    String server = defaultConfiguration.getServer(serverAddress, port, new NullPathPrefix());
    // TODO(corysmith): Refactor to avoid passing string flags.
    runConfigurationWithFlags(defaultConfiguration, createFlagsArray("--reset",  "--server", server));
  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.IJsTestDriver#getTestCasesFor(java.lang.String)
   */
  @Override
  public List<TestCase> getTestCasesFor(String path) {
    return getTestCasesFor(parseConfiguration(path));
  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.IJsTestDriver#getTestCasesFor(com.google.jstestdriver.config.Configuration)
   */
  @Override
  public List<TestCase> getTestCasesFor(Configuration config) {
    TestCaseCollector testCaseCollector = new TestCaseCollector();
    String server = config.getServer(serverAddress, port, new NullPathPrefix());
    // TODO(corysmith): Refactor to avoid passing string flags.
    runConfigurationWithFlags(config, createFlagsArray("--dryRunFor", "all", "--server", server), new TestListenerModule(testCaseCollector));
    return testCaseCollector.getTestCases();
  }

  /* (non-Javadoc)
   * @see com.google.jstestdriver.IJsTestDriver#captureBrowser(java.lang.String)
   */
  @Override
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
    createRunnerInjector(
        config,
        flags,
        additionalRunTimeModules).getInstance(ActionRunner.class).runActions();
  }

  private Injector createRunnerInjector(
      Configuration config, String[] flags, Module... additionalRunTimeModules) {
    if (config == null) {
      throw new ConfigurationException("Configuration cannot be null.");
    }
    List<Module> initializeModules = Lists.newArrayList(initializerModules);
    BasePaths basePaths;
    try {
      // configure logging before we start seriously processing.
      LogManager.getLogManager().readConfiguration(runnerMode.getLogConfig());
      System.out.println("setting runnermode " + runnerMode);
      basePaths = getPathResolver(config);
      initializeModules.add(new InitializeModule(pluginLoader, basePaths, flagsParser,
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
    return injector;
  }

  /**
   * Validates the current configuration.
   * @param validators A list of validations with direct access to the runtime
   *     injector.
   */
  void validate(JsTestDriverValidator... validatiors) {
    Injector injector = createRunnerInjector(this.defaultConfiguration, createFlagsArray());
    for (JsTestDriverValidator validator : validatiors) {
      validator.validate(injector);
    }
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
    BasePaths mergedPaths = new BasePaths();
    
    for (int i = 0; i < defaultFlags.length; i++) {
      if ("--basePath".equals(defaultFlags[i])) {
        if (i + 1 < defaultFlags.length) {
          for (String path : defaultFlags[i + 1].split(",")) {
            mergedPaths.add(new File(path));
          }
        }
        break;
      }
    }
    mergedPaths = mergedPaths.merge(basePaths);
    return mergedPaths.merge(config.getBasePaths());
  }
}
