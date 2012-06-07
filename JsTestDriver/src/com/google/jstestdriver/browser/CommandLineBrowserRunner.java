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
package com.google.jstestdriver.browser;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.jstestdriver.FileUploader;
import com.google.jstestdriver.ProcessFactory;
import com.google.jstestdriver.SlaveBrowser;

/**
 * Runs a browser from the command line.
 * @author corbinrsmith@gmail.com (Cory Smith)
 */
public class CommandLineBrowserRunner implements BrowserRunner {
  private static final Logger logger =
      LoggerFactory.getLogger(CommandLineBrowserRunner.class);
  private final String browserPath;
  private final String browserArgs;
  private final ProcessFactory processFactory;
  private Process process;
  private LogReader logReader = new NullLogReader();

  public CommandLineBrowserRunner(String browserPath,
                                  String browserArgs,
                                  ProcessFactory processFactory) {
    this(browserPath,
         browserArgs,
         processFactory,
         Executors.newCachedThreadPool());
  }
  
  public CommandLineBrowserRunner(String browserPath,
                                  String browserArgs,
                                  ProcessFactory processFactory,
                                  ExecutorService executor) {
    this.browserPath = browserPath;
    this.processFactory = processFactory;
    this.browserArgs = browserArgs;
  }

  @Override
  public void startBrowser(String serverAddress) {
    try {
      String processArgs = "";
      if (this.browserArgs.contains("%s")) {
        processArgs = this.browserArgs.replace("%s", serverAddress);
      } else {
        if (this.browserArgs.length() > 0) {
          processArgs = this.browserArgs + " ";
        }
        processArgs += serverAddress;
      }
      String[] args = processArgs.split(" ");

      String[] finalArgs;
      finalArgs = new String[args.length + 1];
      finalArgs[0] = browserPath;
      System.arraycopy(args, 0, finalArgs, 1, args.length);
      process = processFactory.start(finalArgs);
      logReader = new LogReaderImpl(process.getErrorStream(),
                    process.getInputStream());
    } catch (IOException e) {
      logger.error("Could not start: {} because {}", browserPath, e.toString());
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stopBrowser() {
    try {
      logReader.stop();
      process.destroy();
      if (process.exitValue() != 0) {
        logger.warn("Unexpected shutdown " + process + " "
          + process.exitValue() + "\nLogs:" + getLog());
      }
    } catch (IllegalThreadStateException e) {
      logger.warn("Process refused to exit [" + browserPath +" ]: "+ process);
    }
  }

  @Override
  public int getTimeout() {
    return 30;
  }

  @Override
  public int getNumStartupTries() {
    return 1;
  }

  @Override
  public long getHeartbeatTimeout() {
    return SlaveBrowser.TIMEOUT;
  }

  @Override
  public int getUploadSize() {
    return FileUploader.CHUNK_SIZE;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((browserPath == null) ? 0 : browserPath.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof CommandLineBrowserRunner)) {
      return false;
    }
    CommandLineBrowserRunner other = (CommandLineBrowserRunner) obj;
    return (((browserPath == null && other.browserPath == null) ||
             browserPath.equals(other.browserPath)) &&
            ((browserArgs == null && other.browserArgs == null) ||
             browserArgs.equals(other.browserArgs)));
  }

  @Override
  public String toString() {
    return "CommandLineBrowserRunner [\nbrowserPath=" + browserPath +
        "\nargs=" + browserArgs +
        ",\nprocess=" + process + ",\n process log={\n" + getLog() + "\n}]";
  }

  private String getLog() {
   return logReader.getLog();
  }

  interface LogReader {

    public abstract void stop();

    public abstract String getLog();

    public abstract void run();

  }

  private static class NullLogReader implements LogReader {

    @Override
    public void stop() {
    }

    @Override
    public String getLog() {
      return "Not Started";
    }

    @Override
    public void run() {
    }
  }

  private static class LogReaderImpl implements Runnable, LogReader {
    private final InputStream errorStream;
    private final InputStream inputStream;
    private AtomicBoolean stream = new AtomicBoolean(true);
    private StringBuffer errLog = new StringBuffer();
    private StringBuffer outLog  = new StringBuffer();

    public LogReaderImpl(InputStream errorStream, InputStream inputStream) {
      this.errorStream = errorStream;
      this.inputStream = inputStream;
      
    }
    @Override
    public void stop() {
      stream.set(false);
    }
    @Override
    public String getLog() {
      String out = "error:\n" + errLog + "\nout:\n" + outLog;
      errLog.delete(0, errLog.length());
      outLog.delete(0, outLog.length());
      return out;
    }

    @Override
    public void run() {
      byte[] errBuffer = new byte[512];
      byte[] stdOutBuffer = new byte[512];
      try {
        while (stream.get()) {
          while (errorStream.available() > 0) {
            errorStream.read(errBuffer);
            errLog.append(errBuffer);
          }
          while (inputStream.available() > 0) {
            inputStream.read(stdOutBuffer);
            outLog.append(errBuffer);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
