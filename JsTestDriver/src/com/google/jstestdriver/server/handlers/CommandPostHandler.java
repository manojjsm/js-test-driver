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
package com.google.jstestdriver.server.handlers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.FileSource;
import com.google.jstestdriver.JsonCommand;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.requesthandlers.RequestHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
class CommandPostHandler implements RequestHandler {

  private static final Logger logger = LoggerFactory
      .getLogger(CommandPostHandler.class);

  private final HttpServletRequest request;
  private final Gson gson;
  private final CapturedBrowsers capturedBrowsers;

  @Inject
  public CommandPostHandler(HttpServletRequest request, Gson gson,
      CapturedBrowsers capturedBrowsers) {
    this.request = request;
    this.gson = gson;
    this.capturedBrowsers = capturedBrowsers;
  }

  public void handleIt() throws IOException {
    service(request.getParameter("id"), request.getParameter("data"));
  }

  public void service(String id, String data) {
    SlaveBrowser browser = capturedBrowsers.getBrowser(id);

    data = translateUrls(data);
    logger.trace("Adding command to browser queue: {}", data);
    browser.createCommand(data);
  }

  // TODO(corysmith): figure out what this does.
  private String translateUrls(String data) {
    JsonCommand command = gson.fromJson(data, JsonCommand.class);

    if (command.getCommand().equals(
        JsonCommand.CommandType.LOADTEST.getCommand())) {
      List<String> parameters = command.getParameters();
      String fileSourcesList = parameters.get(0);
      List<FileSource> fileSources = gson.fromJson(fileSourcesList,
          new TypeToken<List<FileSource>>() {
          }.getType());
      parameters.remove(0);
      parameters.add(0, gson.toJson(fileSources));
      return gson.toJson(command);
    }
    return data;
  }

}