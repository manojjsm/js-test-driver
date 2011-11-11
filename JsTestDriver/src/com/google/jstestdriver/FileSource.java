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

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class FileSource {

  private String fileSrc;
  private String basePath;
  private long timestamp;
  private long length;

  public FileSource() {
  }

  public FileSource(String fileSrc, String basePath, long timestamp, long length) {
    this.fileSrc = fileSrc;
    this.basePath = basePath;
    this.timestamp = timestamp;
    this.length = length;
  }

  public String getFileSrc() {
    return fileSrc;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public String getBasePath() {
    return basePath;
  }
  
  /**
   * @return the length
   */
  public long getLength() {
    return length;
  }

  /**
   * @param length the length to set
   */
  public void setLength(long length) {
    this.length = length;
  }

  /**
   * @param timestamp the timestamp to set
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public void setFileSource(String fileSrc) {
    this.fileSrc = fileSrc;
  }

  public void setBasePath(String basePath) {
    this.basePath = basePath;
  }
  
  @Override
  public String toString() {
    return String.format("%s(%s, %s, %s)", getClass().getSimpleName(), fileSrc, basePath, timestamp);
  }
  
  public FileInfo toFileInfo(String contents) {
    return new FileInfo(this.getBasePath(), this.getTimestamp(),
      this.getLength(), false, false, contents, this.getFileSrc());
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((basePath == null) ? 0 : basePath.hashCode());
    result = prime * result + ((fileSrc == null) ? 0 : fileSrc.hashCode());
    result = prime * result + (int) (length ^ (length >>> 32));
    result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    FileSource other = (FileSource) obj;
    if (basePath == null) {
      if (other.basePath != null) return false;
    } else if (!basePath.equals(other.basePath)) return false;
    if (fileSrc == null) {
      if (other.fileSrc != null) return false;
    } else if (!fileSrc.equals(other.fileSrc)) return false;
    if (length != other.length) return false;
    if (timestamp != other.timestamp) return false;
    return true;
  }
}
