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
package com.google.jstestdriver;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.oro.io.GlobFilenameFilter;
import org.apache.oro.text.GlobCompiler;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.jstestdriver.config.UnreadableFile;
import com.google.jstestdriver.config.UnreadableFilesException;
import com.google.jstestdriver.hooks.FileParsePostProcessor;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.util.DisplayPathSanitizer;

/**
 * Handles the resolution of glob paths (*.js) and relative paths.
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class PathResolver {

  private final Set<FileParsePostProcessor> processors;
  private final BasePaths basePaths;
  private DisplayPathSanitizer sanitizer;

  @Inject
  public PathResolver(BasePaths basePaths, Set<FileParsePostProcessor> processors,
      DisplayPathSanitizer sanitizer) {
    this.basePaths = basePaths;
    this.processors = processors;
    this.sanitizer = sanitizer;
  }

  private Set<FileInfo> consolidatePatches(Set<FileInfo> resolvedFilesLoad) {
    Set<FileInfo> consolidated = new LinkedHashSet<FileInfo>(resolvedFilesLoad.size());
    FileInfo currentNonPatch = null;
    for (FileInfo fileInfo : resolvedFilesLoad) {
      if (fileInfo.isPatch()) {
        if (currentNonPatch == null) {
          throw new IllegalStateException("Patch " + fileInfo
            + " without a core file to patch");
        }
        currentNonPatch.addPatch(fileInfo);
      } else {
        consolidated.add(fileInfo);
        currentNonPatch = fileInfo;
      }
    }
    return consolidated;
  }

  
  /**
   * Resolves files for a set of FileInfos:
   *  - Expands glob paths (e.g. "*.js") into distinct FileInfos
   *  - Sets last modified timestamp for each FileInfo
   *
   * @param unresolvedFiles the FileInfos to resolved
   * @return the resolved FileInfos
   */
  public Set<FileInfo> resolve(Set<FileInfo> unresolvedFiles) {
    Set<FileInfo> resolvedFiles = new LinkedHashSet<FileInfo>();
    List<UnreadableFile> unreadable = Lists.newLinkedList();
    for (FileInfo fileInfo : unresolvedFiles) {
      String filePath = fileInfo.getFilePath();

      if (fileInfo.isWebAddress()) {
        resolvedFiles.add(fileInfo.fromResolvedPath(filePath, filePath, -1));
      } else {
        List<IllegalArgumentException> es =
            Lists.newArrayListWithCapacity(basePaths.size());
        for (File basePath : basePaths) {
          try {
            File file = resolvePath(filePath, basePath);
            File absoluteDir = file.getParentFile().getAbsoluteFile();

            // Get all files for the current FileInfo. This will return one file
            // if the FileInfo
            // doesn't represent a glob
            String[] expandedFileNames =
                expandGlob(absoluteDir.getAbsolutePath(), file.getName(), absoluteDir);

            for (String fileName : expandedFileNames) {
              File sourceFile = new File(absoluteDir, fileName);
              createFileInfo(resolvedFiles, unreadable, fileInfo, sourceFile, basePath);
            }
          } catch (IllegalArgumentException e) {
            es.add(e);
          }
        }
        if (es.size() == basePaths.size()) {
          throw new IllegalArgumentException(String.format("Could not find %s in %s", filePath, basePaths));
        }
      }
    }
    if (!unreadable.isEmpty()) {
      throw new UnreadableFilesException(unreadable);
    }

    resolvedFiles = postProcessFiles(resolvedFiles);

    return consolidatePatches(resolvedFiles);
  }

  private void createFileInfo(Set<FileInfo> resolvedFiles, List<UnreadableFile> unreadable,
      FileInfo fileInfo, File sourceFile, File basePath) {
    if (!sourceFile.canRead()) {
      unreadable.add(
          new UnreadableFile(fileInfo.getFilePath(), sourceFile.getAbsolutePath()));
    } else {
      String absolutePath = sourceFile.getAbsolutePath();
      String displayPath = sanitizer.sanitize(absolutePath, basePath);

      File resolvedFile = new File(absolutePath);
      long timestamp = resolvedFile.lastModified();

      FileInfo newFileInfo =
          fileInfo.fromResolvedPath(absolutePath, displayPath, timestamp);

      resolvedFiles.add(newFileInfo);
    }
  }

  /**
   * Creates a full resolved path to a resource without following the sym links.
   */
  public File resolvePath(String filePath) {
    for (File basePath : basePaths) {
      try {
        return resolvePath(filePath, basePath);
      } catch (IllegalArgumentException e) {
        // noop, repeat to the next file.
      }
    }
    throw new IllegalArgumentException();
  }

  private File resolvePath(String filePath, File basePath) {
    File absolute = new File(filePath);
    if (!absolute.isAbsolute()) {
      absolute = new File(basePath, filePath);
    }
    return new File(resolveRelativePathReferences(absolute.getAbsolutePath()));
  }

  /**
   * This function is needed to deal with removing ".." from a path.
   * Java absolute paths  
   */
  private String resolveRelativePathReferences(String path) {
    Pattern pattern = Pattern.compile(Pattern.quote(File.separator));
    String[] elements = pattern.split(path);
    List<String> resolved = Lists.newArrayListWithExpectedSize(elements.length);
    for (String element : elements) {
      if ("..".equals(element)) {
        resolved.remove(resolved.size() - 1);
      } else {
        resolved.add(element);
      }
    }
    return Joiner.on(File.separator).join(resolved);
  }

  private String[] expandGlob(String filePath, String fileNamePattern, File dir) {
    String[] filteredFiles = dir.list(new GlobFilenameFilter(
        fileNamePattern, GlobCompiler.DEFAULT_MASK | GlobCompiler.CASE_INSENSITIVE_MASK));

    if (filteredFiles == null || filteredFiles.length == 0) {
        throw new IllegalArgumentException();
    }

    Arrays.sort(filteredFiles, String.CASE_INSENSITIVE_ORDER);

    return filteredFiles;
  }

  public List<Plugin> resolve(List<Plugin> plugins) {
    List<UnreadableFile> unreadable = Lists.newLinkedList();
    List<Plugin> resolved = Lists.newLinkedList();
    for (Plugin plugin : plugins) {
      File resolvedFile = resolvePath(plugin.getPathToJar());
      if (!resolvedFile.canRead()) {
        unreadable.add(new UnreadableFile(plugin.getPathToJar(), resolvedFile.getAbsolutePath()));
        continue;
      }
      resolved.add(plugin.getPluginFromPath(resolvedFile.getAbsolutePath()));
    }
    if (!unreadable.isEmpty()) {
      throw new UnreadableFilesException(unreadable);
    }
    return resolved;
  }

  private Set<FileInfo> postProcessFiles(Set<FileInfo> resolvedFiles) {
    Set<FileInfo> processedFiles = resolvedFiles;
    for (FileParsePostProcessor processor : processors) {
      processedFiles = processor.process(resolvedFiles);
    }
    return processedFiles;
  }
}