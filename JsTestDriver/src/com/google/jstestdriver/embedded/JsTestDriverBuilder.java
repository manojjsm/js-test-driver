// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.jstestdriver.embedded;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.jstestdriver.Args4jFlagsParser;
import com.google.jstestdriver.FlagsParser;
import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.PluginLoader;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.config.ConfigurationSource;
import com.google.jstestdriver.config.UserConfigurationSource;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.hooks.PluginInitializer;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.hooks.TestResultListener;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.runner.RunnerMode;

/**
 * @author corbinrsmith@gmail.com (Cory Smith)
 *
 */
public class JsTestDriverBuilder {

  private BasePaths basePaths = new BasePaths();
  private List<Module> pluginModules = Lists.newArrayList();
  private String[] flags = new String[]{};
  private Configuration configuration;
  private final PluginLoader pluginLoader = new PluginLoader();
  private int port = -1;
  private final List<ServerListener> serverListeners = Lists.newArrayList();
  private final List<TestResultListener> testListeners = Lists.newArrayList();
  private RunnerMode runnerMode = RunnerMode.QUIET;
  private String serverAddress;
  private boolean raiseOnFailure = false;
  final private List<Class<? extends PluginInitializer>> pluginInitializers =  Lists.newArrayList();
  private boolean preload = false;
  private FlagsParser flagsParser = new Args4jFlagsParser();


  /**
   * @param configPath
   * @return The builder.
   */
  public JsTestDriverBuilder setDefaultConfiguration(String configPath) {
    setConfigurationSource(new UserConfigurationSource(new File(configPath)));
    return this;
  }
  
  /**
   * @param configuration
   * @return The builder.
   */
  public JsTestDriverBuilder setDefaultConfiguration(Configuration configuration) {
    this.configuration = configuration;
    return this;
  }

  /**
   * @param port
   * @return The builder.
   */
  public JsTestDriverBuilder setPort(int port) {
    this.port = port;
    return this;
  }
  
  /**
   * @deprecated Use the builder to set the options usually dictated by flags.
   */
  @Deprecated
  public JsTestDriverBuilder setFlagsParser(FlagsParser flagsParser) {
    this.flagsParser = flagsParser;
    return this;
  }

  /**
   * @param testServerListener
   * @return The builder.
   */
  public JsTestDriverBuilder addServerListener(ServerListener testServerListener) {
    serverListeners.add(testServerListener);
    return this;
  }

  public JsTestDriverBuilder raiseExceptionOnTestFailure(boolean raiseOnFailure) {
    this.raiseOnFailure = raiseOnFailure;
    return this;
  }
  

  /**
   * 
   */
  public JsTestDriver build() {
    // TODO(corysmith): add check to resolve the serverAddress and port issues.
    List<Module> plugins = Lists.newArrayList(pluginModules);
    plugins.add(new ListenerBindingModule(serverListeners, testListeners));
    List<Module> initializers = Lists.<Module>newArrayList(new PluginInitializerModule(pluginInitializers));
    initializers.addAll(pluginModules);
    
    // merge basepaths
    basePaths.addAll(configuration.getBasePaths());
    configuration.getBasePaths().addAll(basePaths);
    return new JsTestDriver(configuration,
        pluginLoader,
        runnerMode,
        flags,
        port,
        plugins,
        initializers,
        basePaths,
        serverAddress,
        raiseOnFailure,
        preload,
        flagsParser);
  }

  /**
   * @param plugin
   * @return
   */
  public JsTestDriverBuilder withPluginInitializer(
        Class<? extends PluginInitializer> initializer) {
    pluginInitializers.add(initializer);
    return this;
  }

  /**
   * Sets the runner mode for JsTestDriver. The Runner mode is a combination of
   * logging and reporting level indications.
   */
  public JsTestDriverBuilder setRunnerMode(RunnerMode runnerMode) {
    this.runnerMode = runnerMode;
    return this;
  }

  /**
   * @param testTestResultsListener
   * @return
   */
  public JsTestDriverBuilder addTestListener(TestResultListener testResultListener) {
    testListeners.add(testResultListener);
    return this;
  }

  /**
   * @param string
   * @return
   */
  public JsTestDriverBuilder setServer(String serverAddress) {
    this.serverAddress = serverAddress;
    return this;
  }

  /**
   * @param file
   * @return
   */
  public JsTestDriverBuilder addBaseDir(File file) {
    this.basePaths.add(file);
    return this;
  }

  /**
   * @param configurationSource
   */
  public JsTestDriverBuilder setConfigurationSource(ConfigurationSource source) {
    setDefaultConfiguration(source.parse(basePaths, new YamlParser()));
    return this;
  }

  /**
   * Modules for the plugins.
   * @param pluginModules
   * @return 
   * @deprecated In favor of passing in {@link PluginInitializer}s
   */
  public JsTestDriverBuilder addPluginModules(List<Module> pluginModules) {
    this.pluginModules = pluginModules;

    return this;
  }

  /**
   * Don't pass in the commandline flags. This method is heavily used by the 
   * command line JsTD. Currently work to remove it.
   * @param flags
   * @return 
   * @deprecated
   */
  public JsTestDriverBuilder setFlags(String[] flags) {
    this.flags = flags;
    return this;
  }

  private static final class PluginInitializerModule implements Module {
    private final List<Class<? extends PluginInitializer>> initializers;

    public PluginInitializerModule(List<Class<? extends PluginInitializer>> initializers) {
      this.initializers = initializers;
    }

    public void configure(Binder binder) {
      Multibinder<PluginInitializer> setBinder =
          Multibinder.newSetBinder(binder, PluginInitializer.class);
      for (Class<? extends PluginInitializer> initializer : initializers) {
        setBinder.addBinding().to(initializer);
      }
    }
  }

  private static class ListenerBindingModule implements Module {
    private final List<? extends ServerListener> serverListeners;
    private final List<? extends TestResultListener>testListeners;
    ListenerBindingModule(List<? extends ServerListener> serverListeners,
        List<? extends TestResultListener> testListeners) {
      this.serverListeners = serverListeners;
      this.testListeners = testListeners;
    }

    @Override
    public void configure(Binder binder) {
      Multibinder<ServerListener> serverSetBinder =
          Multibinder.newSetBinder(binder, ServerListener.class);
      for (ServerListener listener : serverListeners) {
        serverSetBinder.addBinding().toInstance(listener);
      }
      Multibinder<TestResultListener> testSetBinder =
        Multibinder.newSetBinder(binder, TestResultListener.class);
      for (TestResultListener listener : testListeners) {
        testSetBinder.addBinding().toInstance(listener);
      }
    }
  }

  /**
   * Configures JsTestDriver to load the default configuration files into the browser on capture.
   */
  public JsTestDriverBuilder preloadFiles() {
    preload = true;
    return this;
  }

  /**
   * Adds the basePaths for the JsTestDriver instance to use for path resolution.
   */
  public JsTestDriverBuilder addBasePaths(BasePaths basePaths) {
    this.basePaths.addAll(basePaths);
    return this;
  }

  /**
   * Adds the basePaths for the JsTestDriver instance to use for path resolution.
   */
  public JsTestDriverBuilder addBasePaths(File...basePaths) {
    this.basePaths.addAll(Arrays.asList(basePaths));
    return this;
  }
}
