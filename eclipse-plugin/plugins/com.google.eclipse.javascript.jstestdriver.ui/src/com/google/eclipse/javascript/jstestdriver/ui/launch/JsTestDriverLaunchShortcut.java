/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.eclipse.javascript.jstestdriver.ui.launch;

import static com.google.eclipse.javascript.jstestdriver.core.model.LaunchConfigurationConstants.JSTD_LAUNCH_CONFIGURATION_TYPE;
import static com.google.eclipse.javascript.jstestdriver.core.model.LaunchConfigurationConstants.PROJECT_NAME;
import static com.google.eclipse.javascript.jstestdriver.core.model.LaunchConfigurationConstants.TESTS_TO_RUN;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.google.eclipse.javascript.jstestdriver.core.JstdTestRunner;
import com.google.eclipse.javascript.jstestdriver.core.ServiceLocator;
import com.google.eclipse.javascript.jstestdriver.core.model.LaunchConfigurationConstants;

/**
 * Launcher which knows how to run from a specific editor window.
 *
 * @author shyamseshadri@gmail.com (Shyam Seshadri)
 */
public class JsTestDriverLaunchShortcut implements ILaunchShortcut {
  private final TestCaseNameFinder finder = new TestCaseNameFinder();
  private final Logger logger = Logger.getLogger(JsTestDriverLaunchShortcut.class.getName());
  public static final String LAUNCH_CONFIG_CREATORS =
      "com.google.jstestdrvier.eclipse.ui.launchConfigCreator";


  /**
   * 
   */
  public JsTestDriverLaunchShortcut() {
  }

  @Override
  public void launch(ISelection selection, String mode) {
    if (selection instanceof IStructuredSelection) {
      try {
        IStructuredSelection structuredSelection = (IStructuredSelection) selection;
        List<String> testCases = new ArrayList<String>();
        String projectName = "";
        List<IFile> files = new ArrayList<IFile>();
        for (Object object : structuredSelection.toArray()) {
          if (object instanceof IFile) {
            IFile file = (IFile) object;
            files.add(file);
            projectName = file.getProject().getName();
            testCases.addAll(finder.getTestCases(file.getLocation().toFile()));
          }
        }
        runTests(projectName, testCases);
      } catch (CoreException e) {
        logger.log(Level.SEVERE, "", e);
      } catch (IOException e) {
        logger.log(Level.SEVERE, "", e);
      }
    }
  }

  @Override
  public void launch(IEditorPart editor, String mode) {
    List<String> testCases = new ArrayList<String>();
    try {
      String projectName = "";
      List<IFile> files = new ArrayList<IFile>();
      if (editor instanceof AbstractTextEditor) {
        AbstractTextEditor textEditor = (AbstractTextEditor) editor;
        // org.eclipse.wst.jsdt.internal.ui.javaeditor.CompilationUnitEditor
        // If we can find some particular tests selected in the file
        if (textEditor.getSelectionProvider().getSelection() instanceof ITextSelection) {
          int startLine = updateTestCasesFromSelection(testCases, textEditor);
          if (testCases.isEmpty()) {
            updateTestCasesFromCurrentLine(testCases, textEditor, startLine);
          }
        }
      }

      // Else lets add the entire file
      if (editor.getEditorInput() instanceof IFileEditorInput) {
        IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
        files.add(file);
        projectName = file.getProject().getName();
        if (testCases.isEmpty()) {
          updateTestCasesFromFile(editor, testCases);
        }
      }

      runTests(projectName, testCases);

    } catch (CoreException e) {
      logger.log(Level.SEVERE, "", e);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "", e);
    }
  }

  private void updateTestCasesFromFile(IEditorPart editor, List<String> testCases)
      throws IOException {
    IFileEditorInput editorInput = (IFileEditorInput) editor.getEditorInput();
    File jsFile = editorInput.getFile().getLocation().toFile();
    testCases.addAll(finder.getTestCases(jsFile));
  }

  private int updateTestCasesFromSelection(List<String> testCases, AbstractTextEditor textEditor) {
    ITextSelection selection = (ITextSelection) textEditor.getSelectionProvider().getSelection();
    int startLine = selection.getStartLine();
    String selectionString = selection.getText();
    if (selectionString != null && !"".equals(selectionString.trim())) {
      testCases.addAll(finder.getTestCases(selectionString));
    }
    return startLine;
  }

  private void updateTestCasesFromCurrentLine(
      List<String> testCases, AbstractTextEditor textEditor, int startLine) {
    IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
    try {
      IRegion region = document.getLineInformation(startLine);
      String sourceCode = document.get(region.getOffset(), region.getLength());
      testCases.addAll(finder.getTestCases(sourceCode));
    } catch (BadLocationException e) {
      logger.log(Level.SEVERE, "", e);
    }
  }

  private void runTests(String projectName, List<String> testCases) throws CoreException {
    ILaunchConfiguration launchConfiguration = getExistingJstdLaunchConfiguration(projectName);
    if (launchConfiguration == null) {
      runFromNewLaunchConfiguration(projectName, testCases);
    } else {
      runFromExistingLaunchConfiguration(launchConfiguration, testCases);
    }
  }

  private void runFromNewLaunchConfiguration(String projectName, List<String> testCases) {
    createJstdLaunchConfiguration(projectName, testCases);
  }

  private void runFromExistingLaunchConfiguration(
      ILaunchConfiguration launchConfiguration, final List<String> testCases) throws CoreException {
    final ILaunchConfigurationWorkingCopy workingCopy =
        launchConfiguration.copy("new run").getWorkingCopy();
    workingCopy.setAttribute(TESTS_TO_RUN, testCases);
    final ILaunchConfiguration configuration = workingCopy.doSave();

    Job job = new EclipseTestRunnerJob(configuration, testCases, ServiceLocator.getService(
        JstdTestRunner.class), ServiceLocator.getExtensionPoints(
        ILaunchValidator.class, ILaunchValidator.class.getName()));
    job.schedule();
  }

  private ILaunchConfiguration getExistingJstdLaunchConfiguration(String projectName)
      throws CoreException {
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfigurationType type =
        launchManager.getLaunchConfigurationType(JSTD_LAUNCH_CONFIGURATION_TYPE);
    ILaunchConfiguration[] launchConfigurations = launchManager.getLaunchConfigurations(type);
    for (ILaunchConfiguration configuration : launchConfigurations) {
      if (configuration.getAttribute(PROJECT_NAME, "").equals(projectName)) {
        return configuration;
      }
    }
    return null;
  }

  private void createJstdLaunchConfiguration(String projectName, List<String> testCases) {
    ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    ILaunchConfigurationType type =
        launchManager.getLaunchConfigurationType(JSTD_LAUNCH_CONFIGURATION_TYPE);
    try {
      // create and initialize a new launch configuration
      ILaunchConfigurationWorkingCopy newLauchConfiguration = type.newInstance(
          null, launchManager.generateLaunchConfigurationName(projectName));
      newLauchConfiguration.setAttribute(PROJECT_NAME, projectName);
      newLauchConfiguration.setAttribute(LaunchConfigurationConstants.TESTS_TO_RUN, testCases);

      // let user edit/run/cancel the configuraion
      int result = DebugUITools.openLaunchConfigurationDialog(
          Display.getCurrent().getActiveShell(), newLauchConfiguration,
          "org.eclipse.debug.ui.launchGroup.run", null);
      if (result == Window.OK) {
        // we do not want to save tests subset into main launch configuration forever
        newLauchConfiguration.setAttribute(
            LaunchConfigurationConstants.TESTS_TO_RUN, new ArrayList<String>());
        newLauchConfiguration.doSave();
      }
    } catch (CoreException e) {
      logger.log(Level.SEVERE, "Could not create new launch configuration.", e);
    }
  }

}
