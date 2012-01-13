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
package com.google.jstestdriver.server.gateway;

import junit.framework.TestCase;

import org.apache.commons.httpclient.methods.EntityEnclosingMethod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author rdionne@google.com (Robert Dionne)
 */
public class GatewayEntityMethodTest extends TestCase {

  public void testGetNameGetUriAndGetRequestEntity() throws Exception {
    ByteArrayInputStream in = new ByteArrayInputStream("ASDF".getBytes());
    EntityEnclosingMethod method = new GatewayEntityMethod("POST", "http://www.google.com/search", in);
    assertEquals("POST", method.getName());
    assertEquals("http://www.google.com/search", method.getURI().toString());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    method.getRequestEntity().writeRequest(out);
    assertEquals("ASDF", out.toString());
  }
}
