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
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;




/**
 * Handles searching of files for testcase.
 * 
 * @author m.jurcovicova
 */
// TODO(corysmith): Merge with TestCaseNameFinder.
public class TestCaseFinder {
  private static final Logger logger = Logger.getLogger(TestCaseFinder.class.getName());

  public TestCaseFinderResult find(IFile[] loadedFiles, String testCaseName,
      String testName) {
    if (loadedFiles == null || loadedFiles.length == 0) {
      // nothing found
      return new TestCaseFinderResult();
    }

    return searchInFiles(loadedFiles, testCaseName, testName);
  }

  private TestCaseFinderResult searchInFiles(IFile[] loadedFiles,
      String testCaseName, String testName) {
    TextSearchEngine engine = TextSearchEngine.create();
    TestCaseSearchRequestor requestor = new TestCaseSearchRequestor();
    engine.search(loadedFiles, requestor, createPattern(testCaseName, testName),
        new NullProgressMonitor());
    return requestor.getResult();
  }

  private Pattern createPattern(String testCaseName, String testName) {
    return Pattern.compile("[;\\s]" + testCaseName + "\\.prototype\\." + testName + "[\\s=].*");
  }

  private final class TestCaseSearchRequestor extends TextSearchRequestor {
    private TestCaseFinder.TestCaseFinderResult result = new TestCaseFinderResult();

    @SuppressWarnings("unused")
    @Override
    public boolean acceptPatternMatch(TextSearchMatchAccess matchAccess) throws CoreException {
      result =
          new TestCaseFinderResult(matchAccess.getFile(), matchAccess.getMatchOffset() + 1,
              matchAccess.getMatchLength());
      return false;
    }

    @SuppressWarnings("unused")
    @Override
    public boolean acceptFile(IFile file) throws CoreException {
      if (logger.isLoggable(Level.FINE)) {
        logger.fine("Searching through file:" + file);
      }
      return true;
    }

    public TestCaseFinder.TestCaseFinderResult getResult() {
      return result;
    }
  }

  static class TestCaseFinderResult {
    private final IFile file;
    private final int matchOffset;
    private final int matchLength;

    public TestCaseFinderResult() {
      this(null, 0, 0);
    }

    public TestCaseFinderResult(IFile file, int matchOffset, int matchLength) {
      this.file = file;
      this.matchOffset = matchOffset;
      this.matchLength = matchLength;
    }

    public boolean hasMatch() {
      return file != null;
    }

    public IFile getFile() {
      return file;
    }

    public int getMatchOffset() {
      return matchOffset;
    }

    public int getMatchLength() {
      return matchLength;
    }
  }
}
