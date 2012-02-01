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
package com.google.eclipse.javascript.jstestdriver.ui.launch.config;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.eclipse.javascript.jstestdriver.core.ConfigurationData;
import com.google.eclipse.javascript.jstestdriver.core.JsTestDriverConfigurationProvider;
import com.google.eclipse.javascript.jstestdriver.core.ProjectHelper;
import com.google.eclipse.javascript.jstestdriver.core.model.LaunchConfigurationConstants;
import com.google.eclipse.javascript.jstestdriver.ui.Activator;
import com.google.eclipse.javascript.jstestdriver.ui.launch.JavascriptLaunchConfigurationHelper;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UI elements for the Js Test Driver Launch Configuration Tab, along with information on what 
 * information to set on the launch configuration and retrieve from it.
 * 
 * @author shyamseshadri@gmail.com (Shyam Seshadri)
 */
public class JsTestDriverLaunchTab extends AbstractLaunchConfigurationTab {

  private final class ProjectToName implements Function<IProject, String> {
    @Override
    public String apply(IProject project) {
      return project.getName();
    }
  }

  private static final String EXPECTED_FILENAME = "JsTestdriver.conf";
  private final Logger logger =
      Logger.getLogger(JsTestDriverLaunchTab.class.getName());
  private Combo projectCombo;
  private Combo confFileCombo;
  private Button runOnEverySaveCheckbox;
  private JavascriptLaunchConfigurationHelper configurationHelper =
      new JavascriptLaunchConfigurationHelper();

  private final Map<String, ConfigurationData> projectPathToAbsolutePath = Maps.newHashMap();
  private final JsTestDriverConfigurationProvider configurationProvider;
  private final ProjectHelper projectHelper = new ProjectHelper();

  /**
   * 
   */
  public JsTestDriverLaunchTab(JsTestDriverConfigurationProvider configurationProvider) {
    this.configurationProvider = configurationProvider;
  }

  @Override
  public void createControl(Composite parent) {
    Composite control = new Composite(parent, SWT.NONE);
    control.setLayout(new GridLayout(1, false));
    super.setControl(control);

    Group jstdPropertiesControl = new Group(control, SWT.NONE);
    jstdPropertiesControl.setLayout(new GridLayout(2, false));
    jstdPropertiesControl.setText("JSTD:");
    GridData jstdGridData = new GridData(GridData.FILL_HORIZONTAL);
    jstdPropertiesControl.setLayoutData(jstdGridData);

    createJstdPropreties(jstdPropertiesControl);
    setUpProjectCombo();
  }
  
  private static interface GetConfigurationFilesCallback {
    void done(String [] fileNames);
  }

  private void getConfigurationFiles(final IProject project, final GetConfigurationFilesCallback callback) {
    try {
      PlatformUI.getWorkbench().getProgressService().run(true, false, new IRunnableWithProgress() {
        @SuppressWarnings("unused")
        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
          monitor.beginTask("Retrieving configuration files for " + project.getName(), IProgressMonitor.UNKNOWN);
          synchronized (projectPathToAbsolutePath) {
            projectPathToAbsolutePath.clear();
            try {
              projectPathToAbsolutePath.putAll(configurationProvider.getConfigurations(project));
              
              Set<String> displayPaths = projectPathToAbsolutePath.keySet();
              final String[] paths = displayPaths.toArray(new String[displayPaths.size()]);
              Arrays.sort(paths);
              monitor.done();
              Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                  callback.done(paths);
                }
              });
            } catch (CoreException e) {
              IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.toString());
              ErrorDialog.openError(Display.getCurrent().getActiveShell(), "JS Test Driver",
                  "JsTestDriver Error", status);
            }
          }
        }
      });
    } catch (InvocationTargetException e1) {
      e1.printStackTrace();
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    }
  }

  private void createJstdPropreties(Composite control) {
    Label projectLabel = new Label(control, SWT.NONE);
    projectLabel.setText("Project:");

    projectCombo = new Combo(control, SWT.BORDER);
    GridData projectGridData = new GridData(GridData.FILL_HORIZONTAL);
    projectCombo.setLayoutData(projectGridData);
    projectCombo.addKeyListener(new KeyListener() {

      @Override
      public void keyPressed(KeyEvent e) {
      }

      @Override
      public void keyReleased(KeyEvent e) {
        setTabDirty();
      }
    });

    projectCombo.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
      }

      @Override
      public void widgetSelected(SelectionEvent e) {
        setUpConfFileCombo();
        setTabDirty();
      }
    });

    Label confFileLabel = new Label(control, SWT.NONE);
    confFileLabel.setText("Conf File:");

    confFileCombo = new Combo(control, SWT.BORDER);
    GridData confFileGridData = new GridData(GridData.FILL_HORIZONTAL);
    confFileCombo.setLayoutData(confFileGridData);
    confFileCombo.addKeyListener(new KeyListener() {

      @Override
      public void keyPressed(KeyEvent e) {
      }

      @Override
      public void keyReleased(KeyEvent e) {
        setTabDirty();
      }
    });

    confFileCombo.addSelectionListener(new SelectionListener() {

      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
      }

      @Override
      public void widgetSelected(SelectionEvent e) {
        setTabDirty();
      }
    });
    
    GridData runOnEverySaveButtonGridData = new GridData(GridData.FILL_HORIZONTAL);
    runOnEverySaveButtonGridData.horizontalSpan = 3;
    runOnEverySaveCheckbox = new Button(control, SWT.CHECK);
    runOnEverySaveCheckbox.setLayoutData(runOnEverySaveButtonGridData);
    runOnEverySaveCheckbox.setText("Run on Every Save");
    runOnEverySaveCheckbox.addSelectionListener(new SelectionListener() {
      
      @Override
      public void widgetSelected(SelectionEvent e) {
        setTabDirty();
      }
      
      @Override
      public void widgetDefaultSelected(SelectionEvent e) {
      }
    });
  }

  private void setUpProjectCombo() {
    IProject[] projects = projectHelper.getAllProjects();
    if (projects != null) {
      String[] projectNames = Lists.transform(Arrays.asList(projects),
          new ProjectToName()).toArray(new String[projects.length]);
      Arrays.sort(projectNames);
      projectCombo.setItems(projectNames);
    }
  }
  
  private void setUpConfFileCombo() {
    IProject project = getSelectedProject();
    if (project != null) {
      getConfigurationFiles(project, new GetConfigurationFilesCallback() {
        @Override
        public void done(String[] fileNames) {
          confFileCombo.setItems(fileNames);
        }
      });
    }
  }

  private IProject getSelectedProject() {
    String projectName = getSelectedComboString(projectCombo);
    if (projectName != null && !"".equals(projectName)) {
      return projectHelper.getProject(projectName);
    } else {
      return null;
    }
  }

  private void setTabDirty() {
    setDirty(true);
    updateLaunchConfigurationDialog();
  }

  @Override
  public String getName() {
    return "JsTestDriver";
  }

  @Override
  public boolean isValid(ILaunchConfiguration launchConfig) {
    boolean isSelected =
        !"".equals(getSelectedComboString(projectCombo))
        && !"".equals(getSelectedComboString(confFileCombo));
    if (isSelected) {
      String projectName = getSelectedComboString(projectCombo);
      if (configurationHelper.isExistingLaunchConfigWithRunOnSaveOtherThanCurrent(projectName,
          launchConfig.getName()) && runOnEverySaveCheckbox.getSelection()) {
        setErrorMessage(MessageFormat.format("Project named {0} already has another active"
            + " configuration with Run on every save set.", projectName));
        return false;
      }
      setErrorMessage(null);
      return true;
    }
    return false;
  }

  @Override
  public void initializeFrom(final ILaunchConfiguration configuration) {
    try {
      String initProjectName = configuration.getAttribute(LaunchConfigurationConstants.PROJECT_NAME, "");
      if (initProjectName != null && !"".equals(initProjectName.trim())) {
        // find project
        selectComboItem(projectCombo, initProjectName);
        IProject project = new ProjectHelper().getProject(initProjectName);
        if (project == null || !project.exists()) {
          setErrorMessage(MessageFormat
              .format(
                  "Project named {0} does not exist. Please choose another project.",
                  initProjectName));
        }

        // initialize configuration files combo
        final String initConfFileName =
            configuration.getAttribute(LaunchConfigurationConstants.CONF_FILENAME, (String) null);
        getConfigurationFiles(project, new GetConfigurationFilesCallback() {

          @Override
          public void done(String[] fileNames) {
            confFileCombo.setItems(fileNames);
            String confFileName = initConfFileName;
            if (initConfFileName == null) {
              ConfigurationFileSelectHelper helper =
                  new ConfigurationFileSelectHelper(EXPECTED_FILENAME);
              confFileName = helper.findSuitableConfigurationFile(fileNames);
            }
            selectComboItem(confFileCombo, confFileName);
          }
        });
        // initialize run on every save checkbox
        runOnEverySaveCheckbox.setSelection(configuration.getAttribute(
            LaunchConfigurationConstants.RUN_ON_EVERY_SAVE, false));
      }


    } catch (CoreException e) {
      logger.log(Level.WARNING, "Core exception occured", e);
    }
  }

  private void selectComboItem(Combo combo, String item) {
    combo.select(Arrays.binarySearch(combo.getItems(), item));
  }
  
  private String getSelectedComboString(Combo combo) {
    int selectionIndex = combo.getSelectionIndex();
    if (selectionIndex != -1) {
      return combo.getItem(selectionIndex).trim();
    }
    return "";
  }

  @Override
  public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    if (getSelectedProject() != null) {
      ConfigurationData data = projectPathToAbsolutePath.get(getSelectedComboString(confFileCombo));
      configuration.setAttribute(LaunchConfigurationConstants.PROJECT_NAME, getSelectedComboString(projectCombo));
      
      if (data != null) {
        configuration.setAttribute(LaunchConfigurationConstants.CONF_FILENAME, data.getName());
        configuration.setAttribute(LaunchConfigurationConstants.CONF_FULLPATH,
            data.getConfigurationPath());
        int i = 0;
        for (File path : data.getBasePaths()) {
          configuration.setAttribute(
              String.format("%s_%s", LaunchConfigurationConstants.BASEPATH, i++),
              path.getAbsolutePath());
        }
      }
      configuration.setAttribute(LaunchConfigurationConstants.RUN_ON_EVERY_SAVE, runOnEverySaveCheckbox.getSelection());
    }
  }

  @Override
  public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
    configuration.setAttribute(LaunchConfigurationConstants.PROJECT_NAME, "");
    configuration.setAttribute(LaunchConfigurationConstants.CONF_NAME, "");
    configuration.setAttribute(LaunchConfigurationConstants.CONF_FILENAME, "");
    configuration.setAttribute(LaunchConfigurationConstants.CONF_FULLPATH, "");
    configuration.setAttribute(LaunchConfigurationConstants.RUN_ON_EVERY_SAVE, false);
  }
}
