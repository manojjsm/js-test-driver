/*
* Copyright 2009 Google Inc.
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

package com.google.eclipse.javascript.jstestdriver.core;

import com.google.eclipse.javascript.jstestdriver.core.model.JstdServerListener;

import java.util.Observer;

/**
 * Controls the server. There can be only one!
 * @author shyamseshadri@gmail.com (Shyam Seshadri)
 *
 */
public class ServerController {
  private final JstdServerListener listener;
  private final PortSupplier portSupplier;

  public ServerController(JstdServerListener listener, PortSupplier portSupplier) {
    this.listener = listener;
    this.portSupplier = portSupplier;
  }

  public void startServer() {
    Server.getInstance(portSupplier.getPort(), listener).start();
  }

  public void stopServer() {
    if (Server.getInstance() == null) {
      return;
    }
    Server.getInstance().stop();
  }

  public boolean isServerReady() {
    return listener.hasSlaves();
  }

  public boolean isServerStarted() {
    return listener.getServerState() == Server.State.STARTED;
  }

  public synchronized void connectObservers(Observer... observers) {
    for (Observer observer : observers) {
      listener.addObserver(observer);
    }
  }

  public synchronized void disconnectObservers(Observer... observers) {
    for (Observer observer : observers) {
      listener.deleteObserver(observer);
    }
  }

  public String getCaptureUrl() {
    /*if (Server.getInstance() == null) {
      return null;
    }*/
    return Server.getInstance().getCaptureUrl();
  }

  public String getServerUrl() {
    if (Server.getInstance() == null) {
      return null;
    }
    return Server.getInstance().getServerUrl();
  }
}
