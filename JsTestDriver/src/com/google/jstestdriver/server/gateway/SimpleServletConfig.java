/*
 * Copyright 2010 Google Inc.
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
package com.google.jstestdriver.server.gateway;

import com.google.common.collect.Iterators;
import com.google.inject.Inject;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * A {@link ServletConfig} that uses a {@link Map} to configure a
 * {@link Servlet}.
 *
 * @author rdionne@google.com (Robert Dionne)
 */
public class SimpleServletConfig implements ServletConfig {

  private final String name;
  private final ServletContext servletContext;
  private final Map<String, String> config;

  @Inject
  public SimpleServletConfig(
      String name,
      ServletContext servletContext,
      Map<String, String> config) {
    this.name = name;
    this.servletContext = servletContext;
    this.config = config;
  }

  public String getServletName() {
    return name;
  }

  public ServletContext getServletContext() {
    return servletContext;
  }

  public String getInitParameter(String s) {
    return config.get(s);
  }

  public Enumeration<String> getInitParameterNames() {
    return Iterators.asEnumeration(config.keySet().iterator());
  }
}
