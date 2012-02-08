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
package com.google.jstestdriver.config;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.jstestdriver.FileInfo;
import com.google.jstestdriver.Plugin;
import com.google.jstestdriver.model.BasePaths;
import com.google.jstestdriver.requesthandlers.GatewayConfiguration;
import com.google.jstestdriver.server.gateway.MockResponse;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
 */
public class YamlParserTest extends TestCase {


  public void testParseConfigFileAndHaveListOfFiles() throws Exception {
    String configFile = "load:\n - code/*.js\n - test/*.js\nexclude:\n"
      + " - code/code2.js\n - test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null);
    Set<FileInfo> files = config.getFilesList();
    List<FileInfo> listFiles = new ArrayList<FileInfo>(files);

    assertEquals(2, files.size());
    assertTrue(listFiles.get(0).getFilePath().replace(File.separatorChar, '/').endsWith("code/*.js"));
    assertTrue(listFiles.get(1).getFilePath().replace(File.separatorChar, '/').endsWith("test/*.js"));
  }
 
  public void testParseConfigFileAndHaveListOfFilesWithPatches()
      throws Exception {

    String configFile = "load:\n" +
      "- code/code.js\n" +
      "- patch code/patch.js\n" +
      "- code/code2.js\n" +
      "- test/*.js\n" +
      "exclude:\n" +
      "- code/code2.js\n" +
      "- test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse( new InputStreamReader(bais), null);
    Set<FileInfo> files = config.getFilesList();
    List<FileInfo> listFiles = new ArrayList<FileInfo>(files);

    assertEquals(3, files.size());
    assertTrue(listFiles.get(1).toString(),
        listFiles.get(0).getFilePath().replace(File.separatorChar, '/').endsWith("code/code.js"));
    assertTrue(listFiles.get(1).toString(),
        listFiles.get(1).getFilePath().replace(File.separatorChar, '/').endsWith("code/patch.js"));
  }

  public void testParsePlugin() {
    Plugin expected = new Plugin("test", "pathtojar", "com.test.PluginModule",
      Lists.<String> newArrayList());
    String configFile = "plugin:\n" + "  - name: test\n"
      + "    jar: \"pathtojar\"\n" + "    module: \"com.test.PluginModule\"\n";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null);
    List<Plugin> plugins = config.getPlugins();
    assertEquals(expected, plugins.get(0));
  }

  public void testParsePlugins() {
    List<Plugin> expected = new LinkedList<Plugin>(Arrays.asList(new Plugin(
      "test", "pathtojar", "com.test.PluginModule", Lists
        .<String> newArrayList()), new Plugin("test2", "pathtojar2",
      "com.test.PluginModule2", Lists.<String> newArrayList("hello", "world",
        "some/file.js"))));
    String configFile = "plugin:\n" + "  - name: test\n"
      + "    jar: \"pathtojar\"\n" + "    module: \"com.test.PluginModule\"\n"
      + "  - name: test2\n" + "    jar: \"pathtojar2\"\n"
      + "    module: \"com.test.PluginModule2\"\n"
      + "    args: hello, world, some/file.js\n";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null);
    List<Plugin> plugins = config.getPlugins();

    assertEquals(2, plugins.size());
    assertEquals(expected, plugins);
    assertEquals(0, plugins.get(0).getArgs().size());
    assertEquals(3, plugins.get(1).getArgs().size());
  }

  public void testParsePluginArgs() throws Exception {
    String configFile = "plugin:\n" + "  - name: test\n"
      + "    jar: \"pathtojar\"\n" + "    module: \"com.test.PluginModule\"\n"
      + "    args: hello, mooh, some/file.js, another/file.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null);
    List<Plugin> plugins = config.getPlugins();
    Plugin plugin = plugins.get(0);
    List<String> args = plugin.getArgs();

    assertEquals(4, args.size());
    assertEquals("hello", args.get(0));
    assertEquals("mooh", args.get(1));
    assertEquals("some/file.js", args.get(2));
    assertEquals("another/file.js", args.get(3));
  }

  public void testParsePluginNoArgs() throws Exception {
    String configFile = "plugin:\n" + "  - name: test\n"
      + "    jar: \"pathtojar\"\n" + "    module: \"com.test.PluginModule\"\n";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null);
    List<Plugin> plugins = config.getPlugins();
    Plugin plugin = plugins.get(0);
    List<String> args = plugin.getArgs();

    assertEquals(0, args.size());
  }

  public void testServeFile() throws Exception {
    String configFile = "load:\n" + " - code/*.js\n" + " - test/*.js\n"
      + "serve:\n" + " - serve/serve1.js\n" + "exclude:\n"
      + " - code/code2.js\n" + " - test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null);
    Set<FileInfo> serveFilesSet = config.getFilesList();
    List<FileInfo> serveFiles = new ArrayList<FileInfo>(serveFilesSet);

    assertEquals(3, serveFilesSet.size());
    assertTrue(serveFiles.get(0).getFilePath().replace(File.separatorChar, '/').endsWith("code/*.js"));
    assertTrue(serveFiles.get(1).getFilePath().replace(File.separatorChar, '/').endsWith("test/*.js"));
    assertTrue(serveFiles.get(2).getFilePath().replace(File.separatorChar, '/').endsWith("serve/serve1.js"));
    assertTrue(serveFiles.get(2).isServeOnly());
  }
  
  public void testParseTests() throws Exception {
    String configFile =
          "load:\n"
        + " - code/*.js\n"
        + "test:\n"
        + " - test/*.js\n"
        + "serve:\n"
        + " - serve/serve1.js\n"
        + "exclude:\n"
        + " - code/code2.js\n"
        + " - test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();
    
    Configuration config = parser.parse(new InputStreamReader(bais), null);
    Set<FileInfo> serveFilesSet = config.getFilesList();
    List<FileInfo> serveFiles = Lists.newArrayList(serveFilesSet);
    
    assertEquals(2, serveFilesSet.size());
    assertTrue(serveFiles.get(0).getFilePath().replace(File.separatorChar, '/').endsWith("code/*.js"));
    assertTrue(serveFiles.get(1).getFilePath().replace(File.separatorChar, '/').endsWith("serve/serve1.js"));
    assertTrue(serveFiles.get(1).isServeOnly());
    
    List<FileInfo> tests = config.getTests();
    assertEquals("test/*.js", tests.get(0).getFilePath());
  }

  public void testParseBasePath() throws Exception {
    String configFile = "basepath: /some/path\n"
      + "load:\n"
      + " - code/*.js\n"
      + "test:\n"
      + " - test/*.js\n"
      + "serve:\n"
      + " - serve/serve1.js\n"
      + "exclude:\n"
      + " - code/code2.js\n"
      + " - test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();
    
    Configuration config = parser.parse(new InputStreamReader(bais), new BasePaths());
    Set<FileInfo> serveFilesSet = config.getFilesList();
    List<FileInfo> serveFiles = Lists.newArrayList(serveFilesSet);
    
    assertEquals(2, serveFilesSet.size());
    assertTrue(serveFiles.get(0).getFilePath().replace(File.separatorChar, '/').endsWith("code/*.js"));
    assertTrue(serveFiles.get(1).getFilePath().replace(File.separatorChar, '/').endsWith("serve/serve1.js"));
    assertTrue(serveFiles.get(1).isServeOnly());
    
    List<FileInfo> tests = config.getTests();
    assertEquals("test/*.js", tests.get(0).getFilePath());
    Iterator<File> basePathIterator = config.getBasePaths().iterator();
    assertEquals(new File("/some/path"), basePathIterator.next());
  }

  public void testParseGatewayConfiguration() throws Exception {
    String configFile =
        "gateway:\n"
      + " - {matcher: /asdf*, server: \"http://www.google.com\"}\n"
      + " - {matcher: /two, status: 200}\n"
      + " - {matcher: /three, responseHeaders: {X-0: \"0\", X-1: \"1\"}}\n"
      + " - {matcher: /four, contentType: \"text/plain\"}\n"
      + " - {matcher: /five, responseText: \"hello world\"}\n";
    ByteArrayInputStream bias = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bias), null);
    JsonArray gatewayConfig = config.getGatewayConfiguration();
    assertEquals(5, gatewayConfig.size());

    JsonObject first = gatewayConfig.get(0).getAsJsonObject();
    assertTrue(first.has(GatewayConfiguration.MATCHER));
    assertEquals("/asdf*", first.get(GatewayConfiguration.MATCHER).getAsString());
    assertTrue(first.has(GatewayConfiguration.SERVER));
    assertEquals("http://www.google.com", first.get(GatewayConfiguration.SERVER).getAsString());

    JsonObject second = gatewayConfig.get(1).getAsJsonObject();
    assertTrue(second.has(GatewayConfiguration.MATCHER));
    assertEquals("/two", second.get(GatewayConfiguration.MATCHER).getAsString());
    assertTrue(second.has(MockResponse.STATUS));
    assertEquals(200, second.get(MockResponse.STATUS).getAsInt());

    JsonObject third = gatewayConfig.get(2).getAsJsonObject();
    assertTrue(third.has(GatewayConfiguration.MATCHER));
    assertEquals("/three", third.get(GatewayConfiguration.MATCHER).getAsString());
    assertTrue(third.has(MockResponse.RESPONSE_HEADERS));

    JsonObject responseHeaders = third.get(MockResponse.RESPONSE_HEADERS).getAsJsonObject();
    assertEquals(2, responseHeaders.entrySet().size());
    assertTrue(responseHeaders.has("X-0"));
    assertEquals("0", responseHeaders.get("X-0").getAsString());
    assertTrue(responseHeaders.has("X-1"));
    assertEquals("1", responseHeaders.get("X-1").getAsString());

    JsonObject fourth = gatewayConfig.get(3).getAsJsonObject();
    assertTrue(fourth.has(GatewayConfiguration.MATCHER));
    assertEquals("/four", fourth.get(GatewayConfiguration.MATCHER).getAsString());
    assertTrue(fourth.has(MockResponse.CONTENT_TYPE));
    assertEquals("text/plain", fourth.get(MockResponse.CONTENT_TYPE).getAsString());

    JsonObject fifth = gatewayConfig.get(4).getAsJsonObject();
    assertTrue(fifth.has(GatewayConfiguration.MATCHER));
    assertEquals("/five", fifth.get(GatewayConfiguration.MATCHER).getAsString());
    assertTrue(fifth.has(MockResponse.RESPONSE_TEXT));
    assertEquals("hello world", fifth.get(MockResponse.RESPONSE_TEXT).getAsString());
  }

  public void testParseGatewayConfiguration_empty() throws Exception {
    String configFile = "load:\n - code/*.js\n - test/*.js\nexclude:\n"
      + " - code/code2.js\n - test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null);
    assertEquals(0, config.getGatewayConfiguration().size());
  }
  
  public void testParseDoctype_strict() throws Exception {
    String configFile =
        "doctype: strict\n"+
        "load:\n" +
        " - code/*.js\n" +
        " - test/*.js\n" +
        "exclude:\n" +
        " - code/code2.js\n" +
        " - test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null);
    assertEquals("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">",
      config.getDocType().toHtml());
  }

  public void testParseDoctype_quirks() throws Exception {
    String configFile =
      "doctype: quirks\n"+
      "load:\n" +
      " - code/*.js\n" +
      " - test/*.js\n" +
      "exclude:\n" +
      " - code/code2.js\n" +
      " - test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();
    
    Configuration config = parser.parse(new InputStreamReader(bais), null);
    assertEquals("<!DOCTYPE html PUBLIC>", config.getDocType().toHtml());
  }

  public void testParseDoctype_unknown() throws Exception {
    String configFile =
      "doctype: keriks\n"+
      "load:\n" +
      " - code/*.js\n" +
      " - test/*.js\n" +
      "exclude:\n" +
      " - code/code2.js\n" +
      " - test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    try {
      parser.parse(new InputStreamReader(bais), null);
      fail("expected configuration exception");
    } catch (ConfigurationException e) {
      
    }
  }

  public void testParseDoctype_custom() throws Exception {
    String configFile =
      "doctype: <!DOCTYPE zubzub>\n"+
      "load:\n" +
      " - code/*.js\n" +
      " - test/*.js\n" +
      "exclude:\n" +
      " - code/code2.js\n" +
      " - test/test2.js";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null);
    assertEquals("<!DOCTYPE zubzub>", config.getDocType().toHtml());
  }
  
  public void testParseDuplicatePathsInServeAndLoad() throws Exception {
    String configFile =
      "load:\n" +
      " - foo.js\n" +
      " - bar.js\n" +
      "serve:\n" +
      " - foo.js\n";
    ByteArrayInputStream bais = new ByteArrayInputStream(configFile.getBytes());
    ConfigurationParser parser = new YamlParser();

    Configuration config = parser.parse(new InputStreamReader(bais), null);
    Iterator<FileInfo> files = config.getFilesList().iterator();
    assertEquals("foo.js", files.next().getFilePath());
    assertEquals("bar.js", files.next().getFilePath());
    assertFalse(files.hasNext());
  }
}
