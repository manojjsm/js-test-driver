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

package com.google.jstestdriver.util;

/**
 * Thrown when the number of retries is exhausted.
 * @author corbinrsmith@gmail.com (Cory Smith)
 */
public class RetryException extends RuntimeException {

  private final int tries;

  public RetryException(int tries, String message, Throwable cause) {
    super(message, cause);
    this.tries = tries;
  }

  @Override
  public String toString() {
    return "Tried " + tries + "times: \n" + super.toString();
  }
}
