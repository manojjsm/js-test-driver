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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 *
 *
 * @author Cory Smith (corbinrsmith@gmail.com) 
 */
public class BasePaths implements Collection<File> {
  private final List<File> paths;

  /**
   * Creates a 
   */
  public BasePaths(File... paths) {
    this(Lists.newArrayList(paths));
  }
  /**
   * @param parentFile
   */
  public BasePaths(List<File> paths) {
    this.paths = paths;
  }

  /**
   * @param arg0
   * @return
   * @see java.util.List#add(java.lang.Object)
   */
  public boolean add(File arg0) {
    return paths.add(arg0);
  }

  /**
   * @param arg0
   * @param arg1
   * @see java.util.List#add(int, java.lang.Object)
   */
  public void add(int arg0, File arg1) {
    paths.add(arg0, arg1);
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
   * @param arg0
   * @param arg1
   * @return
   * @see java.util.List#addAll(int, java.util.Collection)
   */
  public boolean addAll(int arg0, Collection<? extends File> arg1) {
    return paths.addAll(arg0, arg1);
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
   * @return 
   */
  public BasePaths applyRelativePath(final String relativePath) {
    return new BasePaths(Lists.transform(paths, new Function<File, File>() {
      @Override
      public File apply(File path) {
        return new File(path, relativePath);
      }
    }));
  }
  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "BasePaths [paths=" + paths + "]";
  }
}
