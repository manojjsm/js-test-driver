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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.model.JstdTestCaseDelta;
import com.google.jstestdriver.server.JstdTestCaseStore;

import java.util.Collection;

/**
 * Handles the uploading of a {@link JstdTestCaseDelta} to the {@link JstdTestCaseStore}.
 * 
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class DeltaUpload implements FileSetRequestHandler<String> {
  
  public static final String ACTION = "deltaUpdate";
  private final JstdTestCaseStore store;
  private final Gson gson;
  
  @Inject
  public DeltaUpload(JstdTestCaseStore store, Gson gson) {
    this.store = store;
    this.gson = gson;
  }
  
  @Override
  public String handle(SlaveBrowser browser, String data) {
    Collection<JstdTestCaseDelta> deltas = deserialize(data);
    for (JstdTestCaseDelta delta : deltas) {
      store.applyDelta(delta);
    }
    return "{\"ok\":1}";
  }
  

  private Collection<JstdTestCaseDelta> deserialize(String data) {
    return gson.fromJson(data, new TypeToken<Collection<JstdTestCaseDelta>>() {}.getType());
  }

  @Override
  public boolean canHandle(String action) {
    return ACTION.equals(action);
  }

}
