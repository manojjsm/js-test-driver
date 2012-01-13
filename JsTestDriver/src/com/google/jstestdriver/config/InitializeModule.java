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

package com.google.jstestdriver.config;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.jstestdriver.FlagsParser;
import com.google.jstestdriver.PluginLoader;
import com.google.jstestdriver.hooks.FileParsePostProcessor;
import com.google.jstestdriver.hooks.PluginInitializer;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.runner.RunnerMode;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

/**
 * The configuration module for initializing jstestdriver. It provides a sane
 * set of defaults, as well as documenting the requirements for the
 * initialize call.
 * 
 * @author corbinrsmith@gmail.com
 *
 */
final public class InitializeModule implements Module {
  private final PluginLoader pluginLoader;
  private final BasePaths basePaths;
  private final FlagsParser flagsParser;
  private final RunnerMode runnerMode;

  public InitializeModule(PluginLoader pluginLoader, BasePaths basePaths, FlagsParser flagsParser,
      RunnerMode runnerMode) {
    this.pluginLoader = pluginLoader;
    this.basePaths = basePaths;
    this.flagsParser = flagsParser;
    this.runnerMode = runnerMode;
  }

  public void configure(Binder binder) {
    Multibinder.newSetBinder(binder, FileParsePostProcessor.class);
    Multibinder.newSetBinder(binder, PluginInitializer.class);

    binder.bind(RunnerMode.class).toInstance(runnerMode);
    binder.bind(FlagsParser.class).toInstance(flagsParser);
    binder.bind(PrintStream.class).annotatedWith(Names.named("outputStream"))
        .toInstance(System.out);
    binder.bind(PluginLoader.class).toInstance(pluginLoader);
    binder.bind(BasePaths.class).toInstance(basePaths);
  }
}