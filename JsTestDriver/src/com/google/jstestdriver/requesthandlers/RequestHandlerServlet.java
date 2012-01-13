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

import com.google.inject.Inject;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The lone {@link Servlet}, a @Singleton. Sets up the {@link RequestScope} and
 * instantiates a @RequestScoped {@link RequestDispatcher} to dispatch the
 * request. 
 *
 * @author rdionne@google.com (Robert Dionne)
 */
class RequestHandlerServlet extends HttpServlet {

  private static final long serialVersionUID = -186242854065156745L;

  private static ThreadLocal<Context> localContext = new ThreadLocal<Context>();

  private final RequestScope requestScope;
  private final Provider<RequestDispatcher> dispatcherProvider;

  @Inject
  public RequestHandlerServlet(
      RequestScope requestScope,
      Provider<RequestDispatcher> dispatcherProvider) {
    this.requestScope = requestScope;
    this.dispatcherProvider = dispatcherProvider;
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // TODO(rdionne): Wrap request in an HttpServletRequestWrapper that corrects
    // #getPathInfo() before we clean up JsTestDriverServer.
    localContext.set(new Context(request, response));
    requestScope.enter();
    try {
      requestScope.seed(HttpServletRequest.class, request);
      requestScope.seed(HttpServletResponse.class, response);
      dispatcherProvider.get().dispatch();
    } finally {
      localContext.remove();
      requestScope.exit();
    }
  }

  static HttpServletRequest getRequest() {
    return getContext().request;
  }

  static HttpServletResponse getResponse() {
    return getContext().response;
  }

  private static Context getContext() {
    Context context = localContext.get();
    if (context == null) {
      throw new OutOfScopeException("Cannot access scoped object.");
    }
    return context;
  }

  private static class Context {
    
    HttpServletRequest request;
    HttpServletResponse response;

    public Context(HttpServletRequest request, HttpServletResponse response) {
      this.request = request;
      this.response = response;
    }
  }
}
