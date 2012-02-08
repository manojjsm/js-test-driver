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
package com.google.jstestdriver.requesthandlers;

import static com.google.jstestdriver.requesthandlers.HttpMethod.ANY;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.jstestdriver.server.gateway.GatewayRequestHandler;
import com.google.jstestdriver.server.gateway.MockRequestHandler;
import com.google.jstestdriver.server.gateway.MockResponse;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;

/**
 * @author rdionne@google.com (Robert Dionne)
 */
public class GatewayConfigurationTest extends TestCase {

  private static final String CONFIG = "[" +
      "{\"matcher\":\"/asdf\",\"server\":\"http://www.asdf.com\"}," +
      "{\"matcher\":\"*\",\"status\":200}" +
  "]";

  private IMocksControl control;

  private GatewayRequestHandler gateway;
  private MockRequestHandler mock;
  private GatewayRequestHandler.Factory gatewayFactory;
  private MockRequestHandler.Factory mockFactory;

  private GatewayConfiguration configuration;

  @Override
  protected void setUp() throws Exception {
    control = EasyMock.createControl();
    gateway = control.createMock(GatewayRequestHandler.class);
    mock = control.createMock(MockRequestHandler.class);
    gatewayFactory = control.createMock(GatewayRequestHandler.Factory.class);
    mockFactory = control.createMock(MockRequestHandler.Factory.class);
    configuration = new GatewayConfiguration(gatewayFactory, mockFactory);
  }

  public void testEmptyUponInitialization() {
    assertEquals(0, configuration.getMatchers().size());
  }

  public void testUpdateConfiguration() throws Exception {
    EasyMock.expect(gatewayFactory.create("http://www.asdf.com", "/asdf")).andReturn(
        gateway);
    EasyMock.expect(mockFactory.create(EasyMock.isA(MockResponse.class))).andReturn(
        mock);
    control.replay();
    JsonArray jsonConfig = new JsonParser().parse(CONFIG).getAsJsonArray();
    configuration.updateConfiguration(jsonConfig);
    assertEquals(2, configuration.getMatchers().size());
    assertEquals(gateway,
        configuration.getRequestHandler(new RequestMatcher(ANY, "/asdf")));
    assertEquals(mock,
        configuration.getRequestHandler(new RequestMatcher(ANY, "*")));
    control.verify();
  }
}
