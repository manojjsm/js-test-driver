// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.eclipse.javascript.jstestdriver.ui.view;

import com.google.eclipse.javascript.jstestdriver.core.PortSupplier;
import com.google.eclipse.javascript.jstestdriver.ui.Activator;
import com.google.eclipse.javascript.jstestdriver.ui.prefs.WorkbenchPreferencePage;

public class PreferenceStorePortSupplier implements PortSupplier {
  @Override
  public int getPort() {
    int port = Activator.getDefault().getPreferenceStore().getInt(
      WorkbenchPreferencePage.PREFERRED_SERVER_PORT);
    return port;
  }
}
