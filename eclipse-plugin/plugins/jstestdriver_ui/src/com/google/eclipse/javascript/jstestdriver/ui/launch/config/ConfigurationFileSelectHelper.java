// Copyright 2012 Google Inc. All Rights Reserved.

package com.google.eclipse.javascript.jstestdriver.ui.launch.config;

/**
 * This class contains configuration file pre-selection logic. Its purpose is to leave 
 * the main class uncluttered with string comparison logic. 
 */
class ConfigurationFileSelectHelper {

  private final String expectedFilename;
  
  public ConfigurationFileSelectHelper(String expectedFilename) {
    super();
    this.expectedFilename = expectedFilename;
  }

  public String findSuitableConfigurationFile(String[] configurationFiles) {
    if (configurationFiles==null || configurationFiles.length==0)
      return null;
    
    String result = configurationFiles[0];
    for (String candidate : configurationFiles) {
      result = betterCandidate(candidate, result);
    }
    return result;
  }

  private String betterCandidate(String candidate1, String candidate2) {
    boolean c1ending = candidate1.endsWith(expectedFilename);
    boolean c2ending = candidate2.endsWith(expectedFilename);
    
    if (c1ending == c2ending) {
      return shorterString(candidate1, candidate2);
    }

    if (c1ending) {
      return candidate1;
    }

    return candidate2;
  }

  private String shorterString(String candidate1, String candidate2) {
    if (candidate1.length() <= candidate2.length()) {
      return candidate1;
    }
    return candidate2;
  }
}