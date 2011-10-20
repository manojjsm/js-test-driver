package com.google.jstestdriver.server.handlers.pages;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.jstestdriver.util.HtmlWriter;

import java.io.IOException;

public class HeartbeatPage implements Page {

  private final Boolean debug;

  @Inject
  public HeartbeatPage(@Named("debug") Boolean debug) {
    this.debug = debug;
  }

  @Override
  public void render(HtmlWriter writer, SlavePageRequest request) throws IOException {
    writer.startHead()
      .writeTitle("Heartbeat")
      .writeStyleSheet("/static/heartbeatclient.css")
      .writeExternalScript("/static/lib/json2.js")
      .writeExternalScript("/static/jstestdrivernamespace.js")
      .writeScript("jstestdriver.runConfig = {'debug':" + debug + "};")
      .writeExternalScript("/static/lib/jquery-min.js")
      .writeExternalScript("/static/heartbeatclient.js")
      .writeScript("jstestdriver.heartbeat = jstestdriver.createHeartbeat(\"" +
          request.createCaptureUrl() + "\");" +
          "jstestdriver.jQuery(document).ready(" +
          "function() {jstestdriver.heartbeat.start();});")
      .finishHead()
      .startBody()
      .finishBody()
      .flush();
  }
}
