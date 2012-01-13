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

package com.google.jstestdriver.html;

import com.google.jstestdriver.token.Node;
import com.google.jstestdriver.token.Token;

import java.io.Writer;
import java.util.List;

/**
 * Contains the html of the htmldoc.
 * 
 * @author corbinrsmith@gmail.com (Cory Smith)
 */
public class HtmlNode implements Node {

  private final List<Token> htmlTokens;
  public HtmlNode(List<Token> htmlTokens) {
    this.htmlTokens = htmlTokens;
  }

  public void write(Writer writer) {
    for (Token token : htmlTokens) {
      token.write(writer);
    }
  }
}
