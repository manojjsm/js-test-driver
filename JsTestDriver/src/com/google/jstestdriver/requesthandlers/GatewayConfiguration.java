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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.jstestdriver.server.gateway.GatewayRequestHandler;
import com.google.jstestdriver.server.gateway.MockRequestHandler;
import com.google.jstestdriver.server.gateway.MockResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

/**
 * An object that maintains a mapping between {@link RequestMatcher}s and
 * initialized {@link GatewayRequestHandler}s pointing to various hosts so that the
 * {@link RequestDispatcher} may instantiate {@link GatewayRequestHandler}s on
 * demand.
 *
 * @author rdionne@google.com (Robert Dionne)
 */
public class GatewayConfiguration {

  private static final Logger logger =
      LoggerFactory.getLogger(GatewayConfiguration.class);

  /**
   * JSON key identifying the {@link RequestMatcher} pattern.
   */
  public static final String MATCHER = "matcher";

  /**
   * JSON key identifying the host path.
   */
  public static final String SERVER = "server";

  private final GatewayRequestHandler.Factory gatewayFactory;
  private final MockRequestHandler.Factory mockFactory;

  private JsonArray gatewayConfig = new JsonArray();
  private List<RequestMatcher> matchers;
  private Map<RequestMatcher, MockResponse> mockResponses;
  private Map<RequestMatcher, String> destinations;

  /**
   * Constructs a {@link GatewayConfiguration}. {@link GatewayConfiguration} is
   * bound to the {@link Singleton} scope.
   * @param gatewayFactory A Guice {@link Provider} of {@link GatewayRequestHandler}s.
   */
  @Inject
  public GatewayConfiguration(
      GatewayRequestHandler.Factory gatewayFactory,
      MockRequestHandler.Factory mockFactory) {
    this.gatewayFactory = gatewayFactory;
    this.mockFactory = mockFactory;
    clearConfiguration();
  }

  /**
   * @return A {@link List} of {@link RequestMatcher}s to hand off to the
   * {@link RequestDispatcher} for gatewaying matching requests to various hosts.
   */
  public synchronized List<RequestMatcher> getMatchers() {
    return matchers;
  }

  /**
   * Instantiates a {@link GatewayRequestHandler} to gateway the current request
   * along to the matching host.
   * @param matcher The {@link RequestMatcher} that matches the current request.
   * @return A suitable {@link RequestHandler}.
   */
  public synchronized RequestHandler getRequestHandler(RequestMatcher matcher) {
    MockResponse mockResponse = mockResponses.get(matcher);

    if (mockResponse != null) {
      return mockFactory.create(mockResponse);
    }

    String destination = destinations.get(matcher);

    if (destination != null) {
      return gatewayFactory.create(destination, matcher.getPrefix());
    }

    return new NullRequestHandler();
  }

  public synchronized JsonArray getGatewayConfig() {
    return gatewayConfig;
  }

  /**
   * Updates this {@link GatewayConfiguration} given the new {@code configuration}
   * encoded as a {@link JsonObject} by discarding previously initialized
   * {@link GatewayRequestHandler}s and instantiating new ones with new hosts.
   * @param configuration A {@link JsonObject} specifying a new configuration.
   * @throws ServletException If the new servlets fail to initialize.
   */
  public synchronized void updateConfiguration(JsonArray configuration)
      throws ServletException {
    gatewayConfig = configuration;
    ImmutableList.Builder<RequestMatcher> matchersBuilder = ImmutableList.builder();
    ImmutableMap.Builder<RequestMatcher, MockResponse> mockResponsesBuilder = ImmutableMap.builder();
    ImmutableMap.Builder<RequestMatcher, String> destinationsBuilder = ImmutableMap.builder();
    for (JsonElement element : configuration) {
      JsonObject entry = element.getAsJsonObject();
      RequestMatcher matcher =
          new RequestMatcher(ANY, entry.get(MATCHER).getAsString());
      matchersBuilder.add(matcher);
      if (MockResponse.entryDescribesMockResponse(entry)) {
        mockResponsesBuilder.put(matcher, MockResponse.buildFromEntry(entry));
      } else {
        destinationsBuilder.put(matcher, entry.get(SERVER).getAsString());
      }
    }
    this.matchers = matchersBuilder.build();
    this.mockResponses = mockResponsesBuilder.build();
    this.destinations = destinationsBuilder.build();
  }

  /**
   * Empties the mapping of {@link RequestMatcher}s to
   * {@link GatewayRequestHandler}s.
   */
  public synchronized void clearConfiguration() {
    this.matchers = ImmutableList.of();
    this.mockResponses = ImmutableMap.of();
    this.destinations = ImmutableMap.of();
  }
}
