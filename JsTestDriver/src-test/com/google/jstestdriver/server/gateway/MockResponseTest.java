// Copyright 2012 Google Inc. All Rights Reserved.
package com.google.jstestdriver.server.gateway;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import junit.framework.TestCase;

import org.easymock.EasyMock;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

/**
 * @author rdionne@google.com (Robert Dionne)
 */
public class MockResponseTest extends TestCase {

  private JsonParser parser;

  @Override protected void setUp() throws Exception {
    parser = new JsonParser();
  }

  public void testEntryDescribesMockResponse_status() throws Exception {
    JsonObject entry = parser.parse("{\"status\":0}").getAsJsonObject();
    assertTrue("Should describe mock response",
        MockResponse.entryDescribesMockResponse(entry));
  }

  public void testEntryDescribesMockResponse_responseText() throws Exception {
    JsonObject entry = parser.parse("{\"responseText\":0}").getAsJsonObject();
    assertTrue("Should describe mock response",
        MockResponse.entryDescribesMockResponse(entry));
  }

  public void testEntryDescribesMockResponse_contentType() throws Exception {
    JsonObject entry = parser.parse("{\"contentType\":0}").getAsJsonObject();
    assertTrue("Should describe mock response",
        MockResponse.entryDescribesMockResponse(entry));
  }

  public void testEntryDescribesMockResponse_responseHeaders() throws Exception {
    JsonObject entry = parser.parse("{\"responseHeaders\":{}}").getAsJsonObject();
    assertTrue("Should describe mock response",
        MockResponse.entryDescribesMockResponse(entry));
  }

  public void testEntryDescribesMockResponse_negative() throws Exception {
    JsonObject entry = parser.parse("{\"server\":{}}").getAsJsonObject();
    assertFalse("Should describe mock response",
        MockResponse.entryDescribesMockResponse(entry));
  }

  public void testBuildFromEntry() throws Exception {
    JsonObject entry = parser.parse("{" +
        "\"status\":200," +
        "\"responseHeaders\":{}," +
        "\"contentType\":\"text/html\"," +
        "\"responseText\":\"hello world\"" +
    "}").getAsJsonObject();
    MockResponse mockResponse = MockResponse.buildFromEntry(entry);
    assertEquals(
        "status should match", Integer.valueOf(200), mockResponse.status);
    assertEquals(
        "responseHeaders should match", 0, mockResponse.responseHeaders.size());
    assertEquals(
        "contentType should match", "text/html", mockResponse.contentType);
    assertEquals(
        "repsonseText should match", "hello world", mockResponse.responseText);
  }

  public void testWriteTo() throws Exception {
    HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    PrintWriter writer = new PrintWriter(out);
    MockResponse mockResponse = new MockResponse(
        200, ImmutableMap.of("X-Test", "Blah"), "text/plain", "hello world");
    /* expect */ response.setStatus(200);
    /* expect */ response.setHeader("X-Test", "Blah");
    /* expect */ response.setContentType("text/plain");
    EasyMock.expect(response.getWriter()).andReturn(writer);
    EasyMock.replay(response);
    mockResponse.writeTo(response);
    writer.flush();
    EasyMock.verify(response);
    assertEquals("responseText should match", "hello world", out.toString());
  }
}
