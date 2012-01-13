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

import static com.google.jstestdriver.requesthandlers.HttpMethod.GET;
import static com.google.jstestdriver.requesthandlers.HttpMethod.POST;
import junit.framework.TestCase;

/**
 * @author rdionne@google.com (Robert Dionne)
 */
public class RequestMatcherTest extends TestCase {

  public void testPrefixMatcher() {
    RequestMatcher prefix = new RequestMatcher(GET, "*.mp3");

    assertTrue(prefix.uriMatches(".mp3"));
    assertTrue(prefix.uriMatches("/home.mp3"));
    
    assertFalse(prefix.uriMatches("/home.mp3/trailing"));
    assertFalse(prefix.uriMatches("/home"));
  }

  public void testSuffixMatcher() {
    RequestMatcher suffix = new RequestMatcher(POST, "/context/servlet/*");

    assertTrue(suffix.uriMatches("/context/servlet/"));
    assertTrue(suffix.uriMatches("/context/servlet/directories"));

    assertFalse(suffix.uriMatches("/context/servlet"));
    assertFalse(suffix.uriMatches("prefix/context/servlet/directories"));
    assertFalse(suffix.uriMatches("/context/infix/servlet/directories"));
  }

  public void testLiteralMatcher() {
    RequestMatcher literal = new RequestMatcher(POST, "/something.txt");

    assertTrue(literal.uriMatches("/something.txt"));

    assertFalse(literal.uriMatches("a/something.txt"));
    assertFalse(literal.uriMatches("/something.txt/a"));
  }

  public void testMethodMatcher() {
    RequestMatcher method = new RequestMatcher(POST, "asdf");

    assertTrue(method.methodMatches(POST));
    assertFalse(method.methodMatches(GET));
  }
}
