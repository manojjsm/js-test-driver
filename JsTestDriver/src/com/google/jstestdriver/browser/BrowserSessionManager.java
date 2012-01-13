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

package com.google.jstestdriver.browser;

import com.google.inject.ImplementedBy;



/**
 * Manages the client portion of session for a given captured browser.
 * @author corysmith@google.com (Cory Smith)
 
 */
@ImplementedBy(BaseBrowserSessionManager.class)
public interface BrowserSessionManager {

  /**
   * Starts the session with the Server.
   * @param browserId The browser to start the session with.
   * @return A string to uniquely identify this session.
   */
  public String startSession(String browserId);

  /**
   * Ends a session with a browser.
   * @param sessionId The unique id for the session.
   * @param browserId The id of the browser to end the session for.
   */
  public void stopSession(String sessionId, String browserId);

}
