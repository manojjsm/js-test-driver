// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.jstestdriver.server.gateway;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.jstestdriver.requesthandlers.RequestHandler;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * An HTTP request handler that returns a pre-specified response.
 * @author rdionne@google.com (Robert Dionne)
 */
public class MockRequestHandler implements RequestHandler {

  public interface Factory {
    MockRequestHandler create(MockResponse mockResponse);
  }

  private final HttpServletResponse response;
  private final MockResponse mockResponse;

  @Inject public MockRequestHandler (
      final HttpServletResponse response,
      @Assisted final MockResponse mockResponse) {
    this.response = response;
    this.mockResponse = mockResponse;
  }

  @Override public void handleIt() throws IOException {
    mockResponse.writeTo(response);
  }
}
