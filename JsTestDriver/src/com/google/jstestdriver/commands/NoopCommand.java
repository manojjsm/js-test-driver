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

package com.google.jstestdriver.commands;

import com.google.gson.Gson;
import com.google.jstestdriver.Command;
import com.google.jstestdriver.JsonCommand;

/**
 * A command that does nothing.
 * @author corysmith@google.com (Cory Smith)
 *
 */
public class NoopCommand extends Command {

  public NoopCommand() {
    super(new Gson().toJson(new JsonCommand(JsonCommand.CommandType.NOOP, null)));
  }
}
