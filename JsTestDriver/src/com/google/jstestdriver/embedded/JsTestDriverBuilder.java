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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.jstestdriver.ActionRunner;
import com.google.jstestdriver.Args4jFlagsParser;
import com.google.jstestdriver.FlagsParser;
import com.google.jstestdriver.JsTestDriver;
import com.google.jstestdriver.PluginLoader;
import com.google.jstestdriver.ResponseStreamFactory;
import com.google.jstestdriver.config.Configuration;
import com.google.jstestdriver.config.ConfigurationException;
import com.google.jstestdriver.config.ConfigurationSource;
import com.google.jstestdriver.config.UserConfigurationSource;
import com.google.jstestdriver.config.YamlParser;
import com.google.jstestdriver.hooks.ActionListProcessor;
import com.google.jstestdriver.hooks.FileLoadPostProcessor;
import com.google.jstestdriver.hooks.JsTestDriverValidator;
import com.google.jstestdriver.hooks.PluginInitializer;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.hooks.TestListener;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.runner.RunnerMode;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author corbinrsmith@gmail.com (Cory Smith)
 *
 */
public class JsTestDriverBuilder {
  private static final List<JsTestDriverValidator> DEFAULT_VALIDATORS = 
      Lists.<JsTestDriverValidator>newArrayList(new JsTestDriverValidator() {
        @Override
        public void validate(Injector injector) throws AssertionError {
          Preconditions.checkArgument(injector.getInstance(Key.get(new TypeLiteral<Set<ResponseStreamFactory>>() {}))
              .size() > 0);
          Preconditions.checkArgument(
              injector.getInstance(Key.get(new TypeLiteral<Set<ActionListProcessor>>() {})).size() > 0);
          Preconditions.checkArgument(
              injector.getInstance(Key.get(new TypeLiteral<Set<FileLoadPostProcessor>>() {}))
                  .size() > 0);
          injector.getInstance(ActionRunner.class);
        }
      });

  private BasePaths basePaths = new BasePaths();
  private List<Module> pluginModules = Lists.newArrayList();
  private String[] flags = new String[]{};
  private Configuration configuration;
  private final PluginLoader pluginLoader = new PluginLoader();
  private int port = -1;
  private final List<ServerListener> serverListeners = Lists.newArrayList();
  private final List<TestListener> testListeners = Lists.newArrayList();
  private RunnerMode runnerMode = RunnerMode.QUIET;
  private String serverAddress;
  private final List<JsTestDriverValidator> validators = Lists.newArrayList();
  private boolean raiseOnFailure = false;
  private boolean preload = false;
  private final List<Class<? extends PluginInitializer>> pluginInitializers =  Lists.newArrayList();
  private final List<PluginInitializer> pluginInitializersInstances =  Lists.newArrayList();
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
   * Forces JsTestDriver to validate the runtime configuration. Used to ensure
   * plugins are properly configured, and find early indications of problems.
   * @param validate
   * @return The builder.
   */
  public JsTestDriverBuilder shouldValidate(boolean validate, JsTestDriverValidator... extraValidators) {
    if (validate) {
      validators.addAll(DEFAULT_VALIDATORS);
      for (JsTestDriverValidator jsTestDriverValidator : extraValidators) {
        validators.add(jsTestDriverValidator);
      }
    } else {
      validators.clear();
    }
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
   * Builds a configured JsTestDriver instance, and possibly validates the 
   * configuration.
   */
  public JsTestDriver build() throws AssertionError {
    if (configuration == null) {
      throw new ConfigurationException("A default configuration is required.");
    }
    // TODO(corysmith): add check to resolve the serverAddress and port issues.
    List<Module> plugins = Lists.newArrayList(pluginModules);
    plugins.add(new ListenerBindingModule(serverListeners, testListeners));
    List<Module> initializers =
        Lists.<Module>newArrayList(new PluginInitializerModule(pluginInitializers,
            pluginInitializersInstances));
    initializers.addAll(pluginModules);

    // merge basepaths
    basePaths.addAll(configuration.getBasePaths());
    configuration.getBasePaths().addAll(basePaths);
    JsTestDriverImpl jsTestDriver = new JsTestDriverImpl(configuration,
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
    if (!validators.isEmpty()) {
      jsTestDriver.validate(
          validators.toArray(new JsTestDriverValidator[validators.size()]));
    }
    return jsTestDriver;
  }

  /**
   * @param initializer The {@link PluginInitializer} class, used during initialization
   *   to instal a plugin into the jstd system.
   * @return The build instance.
   */
  public JsTestDriverBuilder withPluginInitializer(
        Class<? extends PluginInitializer> initializer) {
    pluginInitializers.add(initializer);
    return this;
  }
  
  /**
   * @param plugin An instance of the initializer.
   * @return The builder instance.
   */
  public JsTestDriverBuilder withPluginInitializer(
      PluginInitializer initializer) {
    pluginInitializersInstances.add(initializer);
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
  public JsTestDriverBuilder addTestListener(TestListener testResultListener) {
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
    private final List<PluginInitializer> instances;

    public PluginInitializerModule(List<Class<? extends PluginInitializer>> initializers,
        List<PluginInitializer> instances) {
      this.initializers = initializers;
      this.instances = instances;
    }

    @Override
    public void configure(Binder binder) {
      Multibinder<PluginInitializer> setBinder =
          Multibinder.newSetBinder(binder, PluginInitializer.class);
      for (Class<? extends PluginInitializer> initializer : initializers) {
        setBinder.addBinding().to(initializer);
      }
      for (PluginInitializer initializer : instances) {
        setBinder.addBinding().toInstance(initializer);
      }
    }
  }

  private static class ListenerBindingModule implements Module {
    private final List<? extends ServerListener> serverListeners;
    private final List<? extends TestListener>testListeners;
    ListenerBindingModule(List<? extends ServerListener> serverListeners,
        List<? extends TestListener> testListeners) {
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
      Multibinder<TestListener> testSetBinder =
        Multibinder.newSetBinder(binder, TestListener.class);
      for (TestListener listener : testListeners) {
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
