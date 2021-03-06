/*
 * Copyright 2009 Google Inc.
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
import com.google.jstestdriver.action.ConfigureGatewayAction;
import com.google.jstestdriver.action.ConfigureGatewayAction.Factory;
import com.google.jstestdriver.action.UploadAction;
import com.google.jstestdriver.browser.BrowserActionExecutorAction;
import com.google.jstestdriver.browser.BrowserIdStrategy;
import com.google.jstestdriver.hooks.TestsPreProcessor;
import com.google.jstestdriver.util.NullStopWatch;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActionSequenceBuilderTest extends TestCase {

  ActionFactory actionFactory =
      new ActionFactory(null, Collections.<TestsPreProcessor>emptySet(), false,
          null, null, new NullStopWatch());

  public void testAddTestsWithRemoteServerAddress() throws Exception {
    List<String> tests = tests();
    ActionSequenceBuilder builder =
        new ActionSequenceBuilder(actionFactory, new BrowserActionExecutorAction(null, null, null, null, null, 0, null, null, null), new FailureCheckerAction(null, null),
            new UploadAction(null),
            new CapturedBrowsers(new BrowserIdStrategy(new MockTime(0))),
            null,
            newConfigureGatewayActionFactory(),
            null, null);

    List<Class<? extends Action>> expectedActions = new ArrayList<Class<? extends Action>>();
    expectedActions.add(ConfigureGatewayAction.class);
    expectedActions.add(UploadAction.class);
    expectedActions.add(BrowserActionExecutorAction.class);

    List<Action> sequence = builder.addTests(tests).build();

    assertSequence(expectedActions, sequence);
  }

  private Factory newConfigureGatewayActionFactory() {
    return new Factory() {
      @Override
      public ConfigureGatewayAction create(JsonArray gatewayConfig) {
        return new ConfigureGatewayAction(null, null, null, null, gatewayConfig);
      }
    };
  }

  public void testAddTestsWithLocalServer() throws Exception {
    List<String> tests = tests();
    ActionSequenceBuilder builder = new ActionSequenceBuilder(
        new ActionFactory(
            null,
            Collections.<TestsPreProcessor> emptySet(),
            false,
            null,
            null,
            new NullStopWatch()),
            new BrowserActionExecutorAction(null, null, null, null, null, 0, null, null, null), new FailureCheckerAction(null, null), new UploadAction(null),
        new CapturedBrowsers(new BrowserIdStrategy(new MockTime(0))),
        null,
        newConfigureGatewayActionFactory(),
            null, null);

    List<Class<? extends Action>> expectedActions = new ArrayList<Class<? extends Action>>();
    expectedActions.add(ServerStartupAction.class);
    expectedActions.add(ConfigureGatewayAction.class);
    expectedActions.add(UploadAction.class);
    expectedActions.add(BrowserActionExecutorAction.class);
    expectedActions.add(ServerShutdownAction.class);
    builder.withLocalServerPort(1001);

    List<Action> sequence = builder.addTests(tests).build();

    assertSequence(expectedActions, sequence);
  }
  
  public void testAddTestsAndExitOnFailureWithLocalServer() throws Exception {
    List<String> tests = tests();
    ActionSequenceBuilder builder = new ActionSequenceBuilder(
        new ActionFactory(
            null,
            Collections.<TestsPreProcessor> emptySet(),
            false,
            null,
            null,
            new NullStopWatch()),
            new BrowserActionExecutorAction(null, null, null, null, null, 0, null, null, null), new FailureCheckerAction(null, null), new UploadAction(null),
            new CapturedBrowsers(new BrowserIdStrategy(new MockTime(0))),
            null,
            newConfigureGatewayActionFactory(),
            null,
            null);
    
    List<Class<? extends Action>> expectedActions = new ArrayList<Class<? extends Action>>();
    expectedActions.add(ServerStartupAction.class);
    expectedActions.add(ConfigureGatewayAction.class);
    expectedActions.add(UploadAction.class);
    expectedActions.add(BrowserActionExecutorAction.class);
    expectedActions.add(ServerShutdownAction.class);
    expectedActions.add(FailureCheckerAction.class);
    builder.withLocalServerPort(1001);
    
    List<Action> sequence = builder.addTests(tests).raiseOnFailure().build();
    
    assertSequence(expectedActions, sequence);
  }

  public void testNoBrowsers() throws Exception {
    List<String> tests = tests();
    ActionSequenceBuilder builder =
        new ActionSequenceBuilder(
            new ActionFactory(
                null,
                Collections.<TestsPreProcessor>emptySet(),
                false,
                null,
                null,
                new NullStopWatch()),
            new BrowserActionExecutorAction(
                null, null, null, null, null, 0, null, null, null), new FailureCheckerAction(null, null), new UploadAction(null),
            new CapturedBrowsers(new BrowserIdStrategy(new MockTime(0))),
            null,
            newConfigureGatewayActionFactory(),
            null, null);

    List<Action> actions = builder.addTests(tests).withLocalServerPort(999).build();
    List<Class<? extends Action>> expectedActions = new ArrayList<Class<? extends Action>>();
    expectedActions.add(ServerStartupAction.class);
    expectedActions.add(ConfigureGatewayAction.class);
    expectedActions.add(UploadAction.class);
    expectedActions.add(BrowserActionExecutorAction.class);
    expectedActions.add(ServerShutdownAction.class);
    this.<Action>assertSequence(expectedActions, actions);
  }

  private List<String> tests() {
    List<String> tests = new ArrayList<String>();
    tests.add("test.testFoo");
    return tests;
  }

  private <T> void assertSequence(List<Class<? extends T>> expectedActions, List<T> actions) {
    List<Class<?>> actual = new ArrayList<Class<?>>();
    for (T action : actions) {
      actual.add(action != null ? action.getClass() : null);
    }
    assertEquals(expectedActions, actual);
  }
}
