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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.jstestdriver.config.UnreadableFile;
import com.google.jstestdriver.config.UnreadableFilesException;
import com.google.jstestdriver.hooks.FileParsePostProcessor;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.util.DisplayPathSanitizer;

import org.apache.oro.io.GlobFilenameFilter;
import org.apache.oro.text.GlobCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Handles the resolution of glob paths (*.js) and relative paths.
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class PathResolver {
  private static final Logger logger = LoggerFactory.getLogger(PathResolver.class);

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
        expandFileInfosFromFileInfo(resolvedFiles, unreadable, fileInfo, filePath);
      }
    }
    if (!unreadable.isEmpty()) {
      throw new UnreadableFilesException(unreadable);
    }

    resolvedFiles = postProcessFiles(resolvedFiles);

    return consolidatePatches(resolvedFiles);
  }

  private void expandFileInfosFromFileInfo(Set<FileInfo> resolvedFiles,
      List<UnreadableFile> unreadable, FileInfo fileInfo, String filePath) {
    List<String> unresolvedPaths = Lists.newArrayListWithCapacity(basePaths.size());
    for (File basePath : basePaths) {
        File file = new File(basePath, filePath);
        File absoluteDir = file.getParentFile().getAbsoluteFile();
        if (absoluteDir.getName().equals("**")) {
        	absoluteDir = absoluteDir.getParentFile();
        }
        // Get all files for the current FileInfo. This will return one file
        // if the FileInfo doesn't represent a glob
        String[] expandedFileNames =
            expandGlob(absoluteDir.getAbsolutePath(), file.getName(), absoluteDir);
        if (expandedFileNames == null) {
          continue;
        }

        for (String fileName : expandedFileNames) {
          File sourceFile = new File(absoluteDir, fileName);
          createFileInfo(resolvedFiles, unreadable, fileInfo, sourceFile, basePath);
        }
        return;
    }
    unreadable.add(new UnreadableFile(fileInfo.getFilePath(),
        basePaths.toErrorString(fileInfo.getFilePath())));
  }

  private void createFileInfo(Set<FileInfo> resolvedFiles, List<UnreadableFile> unreadable,
      FileInfo fileInfo, File sourceFile, File basePath) {
    if (!sourceFile.canRead()) {
      unreadable.add(
          new UnreadableFile(fileInfo.getFilePath(), sourceFile.getAbsolutePath()));
    } else {
      String absolutePath = resolveRelativePathReferences(sourceFile.getAbsolutePath());
      String displayPath = sanitizer.sanitize(absolutePath, basePath);

      File resolvedFile = new File(absolutePath);
      long timestamp = resolvedFile.lastModified();

      FileInfo newFileInfo = fileInfo.fromResolvedPath(absolutePath, displayPath, timestamp);

      resolvedFiles.add(newFileInfo);
    }
  }

  /**
   * Creates a full resolved path to a resource without following the sym links.
   */
  public File resolvePath(String filePath) {
    return resolvePathToFileInfo(filePath, false, false).toFile();
  }

  
  /**
   * Resolves a path to a {@link FileInfo}.
   * @param path The path to the file.
   * @param isPatch Indicates if this file is intended to patch the file it loads before.
   * @param serveOnly Indicates that this 
   * @return A FileInfo generated from the path.
   * @throws {@link UnreadableFilesException} If the file can't be read.
   */
  public FileInfo resolvePathToFileInfo(String path, boolean isPatch, boolean serveOnly) {
    for (File basePath : basePaths) {
        File resolved = resolvePath(path, basePath);
        if (resolved == null) {
          continue;
        }
        return new FileInfo(resolved.getAbsolutePath(),
            resolved.lastModified(),
            resolved.length(),
            isPatch,
            serveOnly,
            null,
            sanitizer.sanitize(resolved.getAbsolutePath(), basePath));
    }
    throw new UnreadableFilesException(Lists.newArrayList(new UnreadableFile(path, basePaths
        .toErrorString(path))));
  }

  private File resolvePath(String filePath, File basePath) {
    File absolute = new File(filePath);
    if (!absolute.isAbsolute()) {
      absolute = new File(basePath, filePath);
    }
    File resolved = new File(resolveRelativePathReferences(absolute.getAbsolutePath()));
    if (resolved.canRead()) {
      return resolved;
    }
    return null;
  }

  /**
   * This function is needed to deal with removing ".." from a path.
   * On a linux/unix based system, using the canonical file name can cause 
   * some strange issues, as well as confusing debugging, as the file name
   * may not match the users expectations.
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
	  FilenameFilter fileFilter = new GlobFilenameFilter(fileNamePattern,
		        GlobCompiler.DEFAULT_MASK | GlobCompiler.CASE_INSENSITIVE_MASK);
	  String[] filteredFiles = expandDeepDirectoryGlobPaths(dir, fileFilter, "").toArray(new String[0]);

    if (filteredFiles == null || filteredFiles.length == 0) {
        return null;
    }
    Arrays.sort(filteredFiles, String.CASE_INSENSITIVE_ORDER);
    return filteredFiles;
  }
  
  private Set<String> expandDeepDirectoryGlobPaths( File rootDir, FilenameFilter fileFilter, String basePath ) {
	  Set<String> foundFiles = new LinkedHashSet<String>();
	  
	  if (!rootDir.isDirectory()) return foundFiles;
	  
	  File[] children = rootDir.listFiles();
	  for (File child : children) {
		  foundFiles.addAll( expandDeepDirectoryGlobPaths(child, fileFilter, basePath + "/" + child.getName()) );
	  }
	  
	  String[] childFiles = rootDir.list(fileFilter);
	  for (String childFilename : childFiles) {
		foundFiles.add(basePath + "/" + childFilename);
	  }
	  
	  return foundFiles;
  }
  
  

  public List<Plugin> resolve(List<Plugin> plugins) {
    List<UnreadableFile> unreadable = Lists.newLinkedList();
    List<Plugin> resolved = Lists.newLinkedList();
    for (Plugin plugin : plugins) {
      File resolvedFile = resolvePath(plugin.getPathToJar());
      /*if (!resolvedFile.exists()) {
        unreadable.add(new UnreadableFile(plugin.getPathToJar(), basePaths.toErrorString(plugin.getPathToJar())));
        continue;
      }*/
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