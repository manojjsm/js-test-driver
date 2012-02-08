/*
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
package com.google.jstestdriver.config;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.Plugin;
import com.google.jstestdriver.browser.DocType;
import com.google.jstestdriver.browser.DocTypeParser;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.requesthandlers.GatewayConfiguration;
import com.google.jstestdriver.server.gateway.MockResponse;

import org.jvyaml.YAML;

import java.io.File;
import java.io.Reader;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * Parses Yaml files.
 * 
 * @author corysmith@google.com (Cory Smith)
 */
public class YamlParser implements ConfigurationParser {

  private DocTypeParser docTypeParser = new DocTypeParser();

  @SuppressWarnings("unchecked")
  public Configuration parse(Reader configReader, BasePaths defaultBasePaths) {
    Map<Object, Object> data = (Map<Object, Object>) YAML.load(configReader);
    Set<FileInfo> resolvedFilesLoad = new LinkedHashSet<FileInfo>();
    Set<FileInfo> testFiles = new LinkedHashSet<FileInfo>();
    Set<FileInfo> resolvedFilesExclude = new LinkedHashSet<FileInfo>();

    String server = "";
    DocType doctype = docTypeParser.parse("quirks");
    BasePaths basePaths = defaultBasePaths;
    long timeOut = 0;
    List<Plugin> plugins = Lists.newLinkedList();
    JsonArray gatewayConfig = new JsonArray();

    if (data.containsKey("load")) {
      resolvedFilesLoad.addAll(createFileInfos((List<String>) data
        .get("load"), false));
    }
    if (data.containsKey("test")) {
      testFiles.addAll(createFileInfos((List<String>) data
          .get("test"), false));
    }
    if (data.containsKey("exclude")) {
      resolvedFilesExclude.addAll(createFileInfos((List<String>) data
        .get("exclude"), false));
    }
    if (data.containsKey("server")) {
      server = (String) data.get("server");
    }
    if (data.containsKey("plugin")) {
      for (Map<String, String> value :
          (List<Map<String, String>>) data.get("plugin")) {
        plugins.add(new Plugin(value.get("name"), value.get("jar"),
            value.get("module"), createArgsList(value.get("args"))));
      }
    }
    if (data.containsKey("serve")) {
      Set<FileInfo> resolvedServeFiles = createFileInfos((List<String>) data.get("serve"),
        true);
      resolvedFilesLoad.addAll(resolvedServeFiles);
    }
    if (data.containsKey("doctype")) {
      doctype = docTypeParser.parse((String) data.get("doctype"));
    }
    if (data.containsKey("timeout")) {
      timeOut = (Long) data.get("timeout");
    }
    if (data.containsKey("basepath")) {
      Object rawBasePaths = data.get("basepath");
      List<String> stringBasePaths = Lists.newArrayList();
      if (rawBasePaths instanceof String) {
        stringBasePaths.add((String) data.get("basepath"));
      } else if (rawBasePaths instanceof List) {
        stringBasePaths.addAll((List<String>) rawBasePaths);
      }
      for (String stringPath : stringBasePaths) {
        File basePath = new File(stringPath);
        if (!basePath.isAbsolute()) {
          basePaths = basePaths.applyRelativePath(stringPath);
        } else {
          basePaths.add(basePath);
        }
      }
    }
    if (data.containsKey("proxy")) {
      copyDataToGatewayConfig(data, "proxy", gatewayConfig);
    }
    if (data.containsKey("gateway")) {
      copyDataToGatewayConfig(data, "gateway", gatewayConfig);
    }

    return new ParsedConfiguration(resolvedFilesLoad,
                                   resolvedFilesExclude,
                                   plugins,
                                   server,
                                   timeOut,
                                   basePaths,
                                   Lists.newArrayList(testFiles),
                                   gatewayConfig,
                                   doctype);
  }

  @SuppressWarnings("unchecked")
  private void copyDataToGatewayConfig(
      Map<Object, Object> data, String key, JsonArray gatewayConfig) {
    for (Map<Object, Object> value :
        (List<Map<Object, Object>>) data.get(key)) {
      JsonObject entry = new JsonObject();
      if (value.containsKey(GatewayConfiguration.MATCHER)) {
        entry.addProperty(GatewayConfiguration.MATCHER,
            (String) value.get(GatewayConfiguration.MATCHER));
      }
      if (value.containsKey(GatewayConfiguration.SERVER)) {
        entry.addProperty(GatewayConfiguration.SERVER,
            (String) value.get(GatewayConfiguration.SERVER));
      }
      if (value.containsKey(MockResponse.STATUS)) {
        entry.addProperty(MockResponse.STATUS,
            (Number) value.get(MockResponse.STATUS));
      }
      if (value.containsKey(MockResponse.RESPONSE_HEADERS)) {
        Map<String, String> responseHeaders =
            (Map<String, String>) value.get(MockResponse.RESPONSE_HEADERS);
        JsonObject jsonHeaders = new JsonObject();
        for (Entry<String, String> jsonEntry : responseHeaders.entrySet()) {
          jsonHeaders.addProperty(jsonEntry.getKey(), jsonEntry.getValue());
        }
        entry.add(MockResponse.RESPONSE_HEADERS, jsonHeaders);
      }
      if (value.containsKey(MockResponse.CONTENT_TYPE)) {
        entry.addProperty(MockResponse.CONTENT_TYPE,
            (String) value.get(MockResponse.CONTENT_TYPE));
      }
      if (value.containsKey(MockResponse.RESPONSE_TEXT)) {
        entry.addProperty(MockResponse.RESPONSE_TEXT,
            (String) value.get(MockResponse.RESPONSE_TEXT));
      }
      gatewayConfig.add(entry);
    }
  }

  private List<String> createArgsList(String args) {
    if (args == null) {
      return Collections.<String> emptyList();
    }
    List<String> argsList = Lists.newLinkedList();
    String[] splittedArgs = args.split(",");

    for (String arg : splittedArgs) {
      argsList.add(arg.trim());
    }
    return argsList;
  }

  private Set<FileInfo> createFileInfos(List<String> files, boolean serveOnly) {
    if (files != null) {
      Set<FileInfo> fileInfos = new LinkedHashSet<FileInfo>();

      for (String f : files) {
        boolean isPatch = f.startsWith("patch");

        if (isPatch) {
          String[] tokens = f.split(" ", 2);
          f = tokens[1].trim();
        }
        fileInfos.add(new FileInfo(f, -1, -1, isPatch, serveOnly, null, f));
      }
      return fileInfos;
    }
    return Collections.emptySet();
  }
}
