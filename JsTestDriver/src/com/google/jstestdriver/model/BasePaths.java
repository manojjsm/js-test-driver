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
package com.google.jstestdriver.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 *
 *
 * @author Cory Smith (corbinrsmith@gmail.com) 
 */
public class BasePaths implements Collection<File> {
  private final Set<File> paths;

  public BasePaths(File... paths) {
    this(Sets.newHashSet(paths));
  }

  public BasePaths(List<File> paths) {
    this(Sets.newHashSet(paths));
  }

  public BasePaths(Set<File> paths) {
    this.paths = paths;
  }

  /**
   * @param arg0
   * @return
   * @see java.util.List#add(java.lang.Object)
   */
  @Override
  public boolean add(File arg0) {
    return paths.add(arg0);
  }

  /**
   * @param arg0
   * @return
   * @see java.util.List#addAll(java.util.Collection)
   */
  public boolean addAll(Collection<? extends File> arg0) {
    return paths.addAll(arg0);
  }

  /**
   * 
   * @see java.util.List#clear()
   */
  public void clear() {
    paths.clear();
  }

  /**
   * @param arg0
   * @return
   * @see java.util.List#contains(java.lang.Object)
   */
  public boolean contains(Object arg0) {
    return paths.contains(arg0);
  }

  /**
   * @param arg0
   * @return
   * @see java.util.List#containsAll(java.util.Collection)
   */
  public boolean containsAll(Collection<?> arg0) {
    return paths.containsAll(arg0);
  }

  /**
   * @param arg0
   * @return
   * @see java.util.List#equals(java.lang.Object)
   */
  public boolean equals(Object arg0) {
    return paths.equals(arg0);
  }

  /**
   * @return
   * @see java.util.List#hashCode()
   */
  public int hashCode() {
    return paths.hashCode();
  }

  /**
   * @return
   * @see java.util.List#isEmpty()
   */
  public boolean isEmpty() {
    return paths.isEmpty();
  }

  /**
   * @return
   * @see java.util.List#iterator()
   */
  public Iterator<File> iterator() {
    return paths.iterator();
  }

  /**
   * @param arg0
   * @return
   * @see java.util.List#removeAll(java.util.Collection)
   */
  public boolean removeAll(Collection<?> arg0) {
    return paths.removeAll(arg0);
  }

  /**
   * @param arg0
   * @return
   * @see java.util.List#retainAll(java.util.Collection)
   */
  public boolean retainAll(Collection<?> arg0) {
    return paths.retainAll(arg0);
  }

  /**
   * @return
   * @see java.util.List#size()
   */
  public int size() {
    return paths.size();
  }

  /**
   * @see java.util.Collection#remove(java.lang.Object)
   */
  @Override
  public boolean remove(Object object) {
    return paths.remove(object);
  }

  /**
   * @see java.util.Collection#toArray()
   */
  @Override
  public Object[] toArray() {
    return paths.toArray();
  }

  /**
   * @see java.util.Collection#toArray(T[])
   */
  @Override
  public <T> T[] toArray(T[] target) {
    return paths.toArray(target);
  }
  /**
   * @param basePath
   * @return A BasePaths containing the aboslute paths.
   */
  public BasePaths applyRelativePath(final String relativePath) {
    Set<File> relativePaths = Sets.newHashSet();
    for (File path : paths) {
      relativePaths.add(new File(path, relativePath));
    }
    return new BasePaths(relativePaths);
  }

  public BasePaths merge(Collection<File> newPaths) {
    Set<File> merged = Sets.newHashSet(newPaths);
    merged.addAll(paths);
    return new BasePaths(merged);
  }

  @Override
  public String toString() {
    return "BasePaths [paths=" + paths + "]";
  }

  /**
   * @param filePath
   * @return
   */
  public String toErrorString(String filePath) {
    List<String> errorPaths = Lists.newArrayListWithCapacity(paths.size());
    for (File basePath : paths) {
      errorPaths.add(new File(basePath, filePath).getAbsolutePath());
    }
    return "[\n" + Joiner.on("\n\t").join(errorPaths) + "\n]";
  }
}
