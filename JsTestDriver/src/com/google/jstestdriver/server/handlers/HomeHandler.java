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

import com.google.inject.Inject;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.CapturedBrowsers;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.SlaveBrowser;
import com.google.jstestdriver.annotations.ResponseWriter;
import com.google.jstestdriver.requesthandlers.RequestHandler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

/**
 * Need to make it nicer, maybe use a template system...
 *
 * TODO(rdionne): Pull in Soy.  Will be non-trivial due to common deps
 * with potentially different versions.
 *
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
class HomeHandler implements RequestHandler {

  private final CapturedBrowsers capturedBrowsers;
  private final HttpServletResponse response;
  private final PrintWriter writer;

  @Inject
  public HomeHandler(
      CapturedBrowsers capturedBrowsers,
      HttpServletResponse response,
      @ResponseWriter PrintWriter writer) {
    this.capturedBrowsers = capturedBrowsers;
    this.response = response;
    this.writer = writer;
  }

  public void handleIt() throws IOException {
    response.setContentType("text/html");
    writer.write("<html><head><title>JsTestDriver</title>");
    writer.write("<script>");
    writer.write("function getEl(id){return document.getElementById(id);}");
    writer.write("function toggle(id) {\n");
    writer.write("if (getEl(id).style.display=='block') {");
    writer.write("getEl(id).style.display='none';");
    writer.write("} else {");
    writer.write("getEl(id).style.display='block';}");
    writer.write("}</script>");
    writer.write("</head><body>");
    writer.write("<a href=\"/capture\">Capture This Browser</a><br/>");
    writer.write("<a href=\"/capture?strict\">Capture This Browser in strict mode</a><br/>");
    writer.write("<p><strong>Captured Browsers: (");
    writer.write(String.valueOf(capturedBrowsers.getSlaveBrowsers().size()));
    writer.write(")</strong></p>");
    for (SlaveBrowser browser : capturedBrowsers.getSlaveBrowsers()) {
      writer.write("<div>");
      BrowserInfo info = browser.getBrowserInfo();
      writer.write("Id: " + info.getId() + "<br/>");
      writer.write("Name: " + info.getName() + "<br/>");
      writer.write("Version: " + info.getVersion() + "<br/>");
      writer.write("Operating System: " + info.getOs() + "<br/>");
      writer.write(browser.inUse() ? "In use.<br/>" : "Not in use.<br/>");
      writer.write(String.format("RunnerType %s <br/>", browser.getRunnerType()));
      if (browser.getCommandRunning() != null) {
        writer.write("Currently running " + browser.getCommandRunning() + "<br/>");
      } else {
        writer.write("Currently waiting...<br/>");
      }
      writer.write("<input type='button' value='List Files' onclick=\"toggle('f" + browser.getId() + "')\"/>");
      writer.write("<ul style='display:none' id='f" + browser.getId() + "'>");
      for (FileInfo fileInfo : browser.getFileSet()) {
        writer.write("<li>");
        writer.write(fileInfo.getDisplayPath());
        writer.write("</li>");
      }
      writer.write("</ul>");
      writer.write("<input type='button' value='Show Responses' onclick=\"toggle('r" + browser.getId() + "')\"/>");
      writer.write("<pre id='r" + browser.getId() + "' style='display:none'>");
      writer.write(browser.viewResponses());
      writer.write("</pre>");
      writer.write("</div>");
      writer.flush();
    }
    writer.write("</body></html>");
    writer.flush();
  }
}
