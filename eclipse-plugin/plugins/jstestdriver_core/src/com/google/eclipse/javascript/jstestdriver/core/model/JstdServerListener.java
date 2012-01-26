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
package com.google.eclipse.javascript.jstestdriver.core.model;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.google.eclipse.javascript.jstestdriver.core.Server;
import com.google.eclipse.javascript.jstestdriver.core.Server.State;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.hooks.ServerListener;

import java.util.Collection;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Root object to hold information about all the slave browsers. Is registered
 * as a listener so that it knows whenever a slave browser is captured.
 *
 * @author shyamseshadri@gmail.com (Shyam Seshadri)
 */
public class JstdServerListener extends Observable implements ServerListener {

  private Server.State serverState = Server.State.STOPPED;

  private final Multimap<Browser, BrowserInfo> slaves = Multimaps.newSetMultimap(
      new ConcurrentHashMap<Browser, Collection<BrowserInfo>>(), new Supplier<Set<BrowserInfo>>() {
        @Override
        public Set<BrowserInfo> get() {
          return Sets.newHashSet();
        }
      });

  /**
   * @param browser the browser
   * @return All the slave browsers captured by the js test driver server for the particular browser
   */
  public Collection<BrowserInfo> getSlaves(Browser browser) {
    return slaves.get(browser);
  }

  /**
   * @return the total number of slave browsers
   */
  public int getNumberOfSlaves() {
    return slaves.values().size();
  }

  /**
   * Clears all the slave browsers
   */
  public void clear() {
    slaves.clear();
    setChanged();
    notifyObservers(this);
  }

  /**
   * @return true if any slave browsers are captured
   */
  public boolean hasSlaves() {
    return getNumberOfSlaves() > 0;
  }

  @Override
  public String toString() {
    return "Browsers [" + slaves + "]";
  }


  @Override
  public void browserCaptured(BrowserInfo browserInfo) {
    if (browserInfo.getName().contains("Firefox")) {
      addBrowser(Browser.FIREFOX, browserInfo);
    } else if (browserInfo.getName().contains("Chrome")) {
      addBrowser(Browser.CHROME, browserInfo);
    } else if (browserInfo.getName().contains("Safari")) {
      addBrowser(Browser.SAFARI, browserInfo);
    } else if (browserInfo.getName().contains("Microsoft Internet Explorer")) {
      addBrowser(Browser.IE, browserInfo);
    } else if (browserInfo.getName().contains("Opera")) {
      addBrowser(Browser.OPERA, browserInfo);
    }
    setChanged();
    notifyObservers(this);
  }

  private void addBrowser(Browser browser, BrowserInfo browserInfo) {
    slaves.get(browser).add(browserInfo);
  }

  @Override
  public void browserPanicked(BrowserInfo browserInfo) {
    if (browserInfo.getName().contains("Firefox")) {
      removeBrowser(Browser.FIREFOX, browserInfo);
    } else if (browserInfo.getName().contains("Chrome")) {
      removeBrowser(Browser.CHROME, browserInfo);
    } else if (browserInfo.getName().contains("Safari")) {
      removeBrowser(Browser.SAFARI, browserInfo);
    } else if (browserInfo.getName().contains("Microsoft Internet Explorer")) {
      removeBrowser(Browser.IE, browserInfo);
    } else if (browserInfo.getName().contains("Opera")) {
      removeBrowser(Browser.OPERA, browserInfo);
    }
    setChanged();
    notifyObservers(this);
  }

  /**
   * @param firefox
   * @param browserInfo
   */
  private void removeBrowser(Browser browser, BrowserInfo browserInfo) {
    slaves.get(browser).remove(browserInfo);
  }

  @Override
  public void serverStarted() {
    serverState = State.STARTED;
  }

  @Override
  public void serverStopped() {
    slaves.clear();
    serverState = State.STOPPED;
  }

  public State getServerState() {
    return serverState;
  }
}
