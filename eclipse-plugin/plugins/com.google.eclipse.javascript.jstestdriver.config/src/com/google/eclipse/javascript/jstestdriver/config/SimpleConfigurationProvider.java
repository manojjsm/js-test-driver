/*
 * Copyright 2011 Google Inc.
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
package com.google.eclipse.javascript.jstestdriver.config;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.Maps;
import com.google.eclipse.javascript.jstestdriver.core.ConfigurationData;
import com.google.eclipse.javascript.jstestdriver.core.JsTestDriverConfigurationProvider;
import com.google.jstestdriver.model.BasePaths;


/**
 * Scans the current workspace for files ending with .conf or .jstd.
 *
 * @author Cory Smith (corbinrsmith@gmail.com)
 */
public class SimpleConfigurationProvider implements JsTestDriverConfigurationProvider {

  @Override
  public Map<String, ConfigurationData> getConfigurations(final IProject project)
      throws CoreException {
    final Map<String, ConfigurationData> configurationPaths = Maps.newHashMap();
    final BasePaths basePaths = new BasePaths(project.getLocation().toFile());
    project.accept(new IResourceVisitor() {
        @SuppressWarnings("unused") // interface requires the exception to be visible.
        @Override
      public boolean visit(IResource resource) throws CoreException {
        if (resource.getName().endsWith(".conf") || resource.getName().endsWith(".jstd")) {
          // get the path relative to the project.
          String resourcePath = resource.getProjectRelativePath().toOSString();
          configurationPaths.put(
              resourcePath, new ConfigurationData(resourcePath, resourcePath, basePaths));
        }
        return true;
      }
    });
    return configurationPaths;
  }
}

