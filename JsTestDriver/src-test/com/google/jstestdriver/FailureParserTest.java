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

package com.google.jstestdriver;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.jstestdriver.model.ConcretePathPrefix;
import com.google.jstestdriver.model.NullPathPrefix;

import static com.google.jstestdriver.FailureParser.Failure;

import java.util.List;

/**
 * Tests for the FailureParser.
 * 
 * @author Cory Smith (corbinrsmith@gmail.com)
 * 
 */
public class FailureParserTest extends junit.framework.TestCase {
  Gson gson = new Gson();

  public void testParse() throws Exception {
    String name1 = "TypeError";
    String message1 = "Whut?";
    List<String> stack1 =
        Lists.newArrayList("http://someweb.com:8080/static/asserts.js",
            "http://someweb.com:8080/test/foo.js");

    Failure failure1 =
        new Failure(String.format("%s: %s", name1, message1), Lists.newArrayList(stack1.get(1)));

    String jsonFailures =
        gson.toJson(Lists.newArrayList(new JsException(name1, message1, "doof.js", 1l, Joiner.on(
            "\n").join(stack1))));
    List<Failure> failures = new FailureParser(new NullPathPrefix()).parse(jsonFailures);
    assertEquals(failure1, failures.get(0));
  }

  public void testParsePrefixedPathStack() throws Exception {
    ConcretePathPrefix prefix = new ConcretePathPrefix("/jstd");
    String name1 = "TypeError";
    String message1 = "Whut?";
    List<String> stack1 =
        Lists.newArrayList(
          "http://someweb.com:8080" + prefix.prefixPath("/static/asserts.js"),
          "http://someweb.com:8080" + prefix.prefixPath("/test/foo.js"));
    Failure failure1 =
        new Failure(String.format("%s: %s", name1, message1), Lists.newArrayList(stack1.get(1)));

    String jsonFailures =
        gson.toJson(Lists.newArrayList(new JsException(name1, message1, "doof.js", 1l, Joiner.on(
            "\n").join(stack1))));
    List<Failure> failures = new FailureParser(prefix).parse(jsonFailures);
    assertEquals(failure1, failures.get(0));
  }

  public void testParseMultipleErrors() throws Exception {
    String name1 = "TypeError";
    String message1 = "Whut?";
    List<String> stack1 =
        Lists.newArrayList("http://someweb.com:8080/static/asserts.js",
            "http://someweb.com:8080//test/foo.js");
    Failure failure1 =
        new Failure(String.format("%s: %s", name1, message1), Lists.newArrayList(stack1.get(1)));

    String name2 = "TypeError";
    String message2 = "Thuw?";
    List<String> stack2 =
        Lists.newArrayList(
          "http://someweb.com:8080/static/asserts.js",
          "http://someweb.com:8080/test/foo.js",
          "http://someweb.com:8080/test/static/foo.js",
          "http://someweb.com:8080/test/baz.js");
    Failure failure2 =
        new Failure(String.format("%s: %s", name2, message2), Lists.newArrayList(stack2.get(1),
            stack2.get(2), stack2.get(3)));

    String jsonFailures =
        gson.toJson(Lists.newArrayList(
            new JsException(name1, message1, "doof.js", 1l, Joiner.on("\n").join(stack1)),
            new JsException(name2, message2, "food.js", 3l, Joiner.on("\n").join(stack2))));

    List<Failure> failures = new FailureParser(new NullPathPrefix()).parse(jsonFailures);

    assertEquals(failure1, failures.get(0));
    assertEquals(failure2, failures.get(1));
  }

  public void testParseUnparsableFailure() throws Exception {
    String failure = "some unformatted failure.";

    List<Failure> failures = new FailureParser(new NullPathPrefix()).parse(failure);

    assertEquals(failure, failures.get(0).getMessage());
  }
}
