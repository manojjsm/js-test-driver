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

package com.google.eclipse.javascript.jstestdriver.core.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.jstestdriver.FileSource;


/**
 * Contains a collection of all files load in a given configuration.
 * 
 * @author m.jurcovicova
 */
public class LoadedSourceFileLibrary {

  private static final Logger logger = LoggerFactory.getLogger(LoadedSourceFileLibrary.class);
  private Map<String, Set<IFile>> loadedSources = new HashMap<String, Set<IFile>>();

  public void addTestCaseSource(FileSource fileSource) {
    IPath location = new Path(fileSource.getBasePath());
    IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(location);
    if (file != null) {
      Set<IFile> projectTestCases = getProjectTestCases(file.getProject());
      projectTestCases.add(file);
    } else {
      logger.warn("Could not locate file {}", fileSource.getFileSrc());
    }
  }

  private Set<IFile> getProjectTestCases(IProject testedProject) {
    return getProjectTestCases(testedProject.getName());
  }

  private Set<IFile> getProjectTestCases(String projectName) {
    if (!loadedSources.containsKey(projectName)) {
      loadedSources.put(projectName, new HashSet<IFile>());
    }

    return loadedSources.get(projectName);
  }

  public IFile[] getLoadedFiles(IProject testedProject) {
    if (testedProject == null) {
      return getAllLoadedFiles();
    }

    Set<IFile> projectTestCases = getProjectTestCases(testedProject);
    return projectTestCases.toArray(new IFile[0]);
  }

  private IFile[] getAllLoadedFiles() {
    Set<IFile> result = new HashSet<IFile>();
    for (Set<IFile> set : loadedSources.values()) {
      result.addAll(set);
    }
    return result.toArray(new IFile[0]);
  }
}
