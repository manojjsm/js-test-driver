// Copyright 2012 Google Inc. All Rights Reserved.
package com.google.jstestdriver.server.gateway;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author rdionne@google.com (Robert Dionne)
 */
public class StreamsTest extends TestCase {

  public void testCopy() throws Exception {
    ByteArrayInputStream in = new ByteArrayInputStream("hello world".getBytes());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Streams.copy(in, out);
    assertEquals("copied stream should match", "hello world", out.toString());
  }
}
