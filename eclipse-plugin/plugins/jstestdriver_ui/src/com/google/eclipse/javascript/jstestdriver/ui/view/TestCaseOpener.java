/*
 * Copyright 2011 Google Inc.
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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import com.google.eclipse.javascript.jstestdriver.core.model.LoadedSourceFileLibrary;
import com.google.eclipse.javascript.jstestdriver.ui.Activator;
import com.google.jstestdriver.TestResult;


/**
 * Handles searching of files for testcase.
 * 
 * @author m.jurcovicova
 */
public class TestCaseOpener {

  private static final String SELECTED_TEST_WAS_NOT_FOUND = "Selected test was not found.";
  private static final String CAN_NOT_OPEN = "Can not open the test.";

  private static final Logger logger = Logger.getLogger(TestCaseOpener.class.getName());

  private final LoadedSourceFileLibrary library;
  private final TestCaseFinder finder = new TestCaseFinder();

  public TestCaseOpener(LoadedSourceFileLibrary library) {
    this.library = library;
  }

  public void openTestSource(IProject testedProject, TestResult testResult,
      IWorkbenchPage workbenchPage) {
    IFile[] loadedFiles = library.getLoadedFiles(testedProject);
    TestCaseFinder.TestCaseFinderResult result =
        finder.find(loadedFiles, testResult.getTestCaseName(), testResult.getTestName());
    if (!result.hasMatch()) {
      showTestNotFoundMessage();
      return;
    }

    openInEditor(result, workbenchPage);
  }

  private void openInEditor(TestCaseFinder.TestCaseFinderResult result, IWorkbenchPage workbenchPage) {
    try {
      IEditorPart editor = getAndOpenEditor(result.getFile(), workbenchPage);
      if (editor instanceof ITextEditor) {
        ITextEditor textEditor = (ITextEditor) editor;
        textEditor.selectAndReveal(result.getMatchOffset(), result.getMatchLength());
      }

    } catch (PartInitException e) {
      logger.log(Level.SEVERE, "Could not open editor for: " + result.getFile(), e);
    }
  }

  private void showTestNotFoundMessage() {
    Status status =
        new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, SELECTED_TEST_WAS_NOT_FOUND, null);
    ErrorDialog.openError(Display.getCurrent().getActiveShell(), "JS Test Driver", CAN_NOT_OPEN,
        status);
  }

  private String getFileEditorId(IFile file) throws PartInitException {
    String editorId = null;
    IEditorDescriptor desc;
    desc = IDE.getEditorDescriptor(file);
    if (desc == null || !desc.isInternal()) {
      editorId = "org.eclipse.ui.DefaultTextEditor";
    } else {
      editorId = desc.getId();
    }
    return editorId;
  }

  private IEditorPart getAndOpenEditor(IFile file, IWorkbenchPage page) throws PartInitException {
    IEditorInput input = new FileEditorInput(file);
    IEditorPart editor = page.findEditor(input);
    if (editor != null) {
      page.bringToTop(editor);
      page.activate(editor);
      return editor;
    }

    String editorId = getFileEditorId(file);
    editor = page.openEditor(input, editorId, true);
    return editor;
  }
}
