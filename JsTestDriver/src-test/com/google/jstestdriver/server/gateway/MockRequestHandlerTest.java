// Copyright 2012 Google Inc. All Rights Reserved.
package com.google.jstestdriver.server.gateway;

import junit.framework.TestCase;

import org.easymock.EasyMock;

/**
 * @author rdionne@google.com (Robert Dionne)
 */
public class MockRequestHandlerTest extends TestCase {

  public void testHandleIt() throws Exception {
    MockResponse mockResponse = EasyMock.createMock(MockResponse.class);
    MockRequestHandler handler = new MockRequestHandler(null, mockResponse);

    /* expect */ mockResponse.writeTo(null);
    EasyMock.replay();
    handler.handleIt();
    EasyMock.verify();
  }
}
