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
package com.google.jstestdriver.servlet.fileset;

import java.util.Collection;

import com.google.inject.Inject;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.model.JstdTestCase;
import com.google.jstestdriver.server.JstdTestCaseStore;

/**
 *
 *
 * @author Cory Smith (corbinrsmith@gmail.com) 
 */
public class ListTestCases implements FileSetRequestHandler<Collection<JstdTestCase>> {
  
  public static final String ACTION = "listCases";
  private final JstdTestCaseStore store;
  
  @Inject
  public ListTestCases(JstdTestCaseStore store) {
    this.store = store;
  }
  

  @Override
  public Collection<JstdTestCase> handle(SlaveBrowser browser, String data) {
    return store.getCases();
  }

  @Override
  public boolean canHandle(String action) {
    return ACTION.equals(action);
  }
}
