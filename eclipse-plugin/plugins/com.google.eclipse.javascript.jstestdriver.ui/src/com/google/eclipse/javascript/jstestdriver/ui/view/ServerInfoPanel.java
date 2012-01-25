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
package com.google.eclipse.javascript.jstestdriver.ui.view;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import com.google.eclipse.javascript.jstestdriver.core.model.JstdServerListener;

/**
 * Panel which displays info about the server, including status, capture url and
 * browsers captured.
 *
 * @author shyamseshadri@gmail.com (Shyam Seshadri)
 */
public class ServerInfoPanel extends Composite implements Observer {


  private static final Color NOT_RUNNING = new Color(Display.getCurrent(), 255, 102, 102);
  private static final Color NO_BROWSERS = new Color(Display.getCurrent(), 255, 255, 102);
  private static final Color READY = new Color(Display.getCurrent(), 102, 204, 102);
  private static final String SERVER_DOWN = "NOT RUNNING";

  private final Text serverUrlText;
  private final BrowserButtonPanel browserButtonPanel;

  public ServerInfoPanel(Composite parent, int style) {
    super(parent, style);
    setLayout(new GridLayout(1, true));
    GridData layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.horizontalAlignment = SWT.FILL;
    setLayoutData(layoutData);

    GridData textGridData = new GridData();
    textGridData.grabExcessHorizontalSpace = true;
    textGridData.horizontalAlignment = SWT.FILL;
    serverUrlText = new Text(this, SWT.CENTER);
    serverUrlText.setBackground(NOT_RUNNING);
    serverUrlText.setText(SERVER_DOWN);
    serverUrlText.setLayoutData(textGridData);
    serverUrlText.setEditable(false);
    // select all on click and focus
    serverUrlText.addMouseListener(new MouseListener() {
      @Override
      public void mouseUp(MouseEvent e) {
        ((Text) e.getSource()).selectAll();
      }

      @Override
      public void mouseDown(MouseEvent e) {
      }

      @Override
      public void mouseDoubleClick(MouseEvent e) {
      }
    });
    serverUrlText.addFocusListener(new FocusListener() {
      @Override
      public void focusLost(FocusEvent e) {
      }
      @Override
      public void focusGained(FocusEvent e) {
        ((Text) e.getSource()).setFocus();
      }
    });
    serverUrlText.setOrientation(SWT.HORIZONTAL);
    browserButtonPanel = new BrowserButtonPanel(this, SWT.NONE);
  }

  /**
   * Notified when a browser has been captured. Updates the Server url text background color
   * to green to signify that we are now ready to run tests. The browser capture url still remains
   * the same and is not to be changed.
   */
  @Override
  public void update(Observable o, Object arg) {
    final JstdServerListener data = (JstdServerListener) arg;
    if (data.hasSlaves()) {
      setServerUrl(null, READY);
    } else {
      setServerUrl(null, NO_BROWSERS);
    }
    browserButtonPanel.update(o, data);
  }

  /**
   * Gets the browser button panel.
   * @return the browser button panel
   */
  public BrowserButtonPanel getBrowserButtonPanel() {
    return browserButtonPanel;
  }

  /**
   * Sets the state of the server info panel to "Server Started". This means that the color is
   * set to Yellow while waiting for browsers to capture, and the text displayed is the Server
   * capture url that can be copy pasted into a browser.
   * @param serverUrl the url to be used to capture a browser
   */
  public void setServerStartedAndWaitingForBrowsers(String serverUrl) {
    setServerUrl(serverUrl, NO_BROWSERS);
  }

  /**
   * @param serverUrl
   * @param color 
   */
  private void setServerUrl(final String serverUrl, final Color color) {
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        serverUrlText.setRedraw(true);
        if (serverUrl != null) {
          serverUrlText.setText(serverUrl);
        }
        serverUrlText.setBackground(color);
        // workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=294318
        if (serverUrlText.isFocusControl()) {
          browserButtonPanel.setFocus(); // unfocus
          serverUrlText.setFocus(); // refocus
        }
      }
    });
  }

  /**
   * Sets the state of the server info panel to stopped.
   */
  public void setServerStopped() {
    setServerUrl(SERVER_DOWN, NOT_RUNNING);
  }
}