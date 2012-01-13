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
package com.google.jstestdriver.requesthandlers;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author rdionne@google.com (Robert Dionne)
 */
public class RequestHandlersModuleTest extends TestCase {

  private static class HandlerOne implements RequestHandler {
    @SuppressWarnings("unused")
    public void handleIt() throws IOException {}
  }

  private static class HandlerTwo implements RequestHandler {
    @SuppressWarnings("unused")
    public void handleIt() throws IOException {}
  }

  public void testHandlerPathBindings() throws Exception {
    Injector injector = Guice.createInjector(new RequestHandlersModule() {

      @Override
      protected void configureHandlers() {
        serve(HttpMethod.GET, "/get/*", HandlerOne.class);
        serve(HttpMethod.POST, "/post", HandlerTwo.class);
      }
    });

    List<RequestMatcher> matchers = injector.getInstance(new Key<List<RequestMatcher>>() {});

    assertEquals(2, matchers.size());
    assertTrue(matchers.get(0).uriMatches("/get/something"));
    assertTrue(matchers.get(0).methodMatches(HttpMethod.GET));
    assertTrue(matchers.get(1).uriMatches("/post"));
    assertTrue(matchers.get(1).methodMatches(HttpMethod.POST));

    RequestScope scope = injector.getInstance(RequestScope.class);
    assertNotNull(scope);

    scope.enter();

    Map<RequestMatcher, RequestHandler> handlers =
        injector.getInstance(new Key<Map<RequestMatcher, RequestHandler>>() {});

    assertEquals(2, handlers.size());
    assertTrue(handlers.get(matchers.get(0)) instanceof HandlerOne);
    assertTrue(handlers.get(matchers.get(1)) instanceof HandlerTwo);
    assertNotNull(injector.getInstance(RequestHandlerServlet.class));

    scope.exit();
  }
}
