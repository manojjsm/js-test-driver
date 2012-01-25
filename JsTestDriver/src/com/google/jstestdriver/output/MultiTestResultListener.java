package com.google.jstestdriver.output;

import com.google.inject.Inject;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.FileResult;
import com.google.jstestdriver.TestCase;
import com.google.jstestdriver.TestResult;
import com.google.jstestdriver.hooks.TestListener;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Print results to multiple result printers simultaneously.
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public class MultiTestResultListener implements TestListener {
  private static final Logger logger = LoggerFactory.getLogger(MultiTestResultListener.class);
  private final Set<TestListener> delegates;

  @Inject
  public MultiTestResultListener(Set<TestListener> delegates) {
    this.delegates = delegates;
    logger.debug("listener:{} delegates: {}", this, delegates);
  }

  public void onTestComplete(TestResult testResult) {
    for (TestListener delegate : delegates) {
      delegate.onTestComplete(testResult);
    }
  }

  public void finish() {
    for (TestListener delegate : delegates) {
      delegate.finish();
    }
  }

  public void onFileLoad(BrowserInfo browser, FileResult fileResult) {
    for (TestListener delegate : delegates) {
      delegate.onFileLoad(browser, fileResult);
    }
  }

  @Override
  public void onTestRegistered(BrowserInfo browser, TestCase testCase) {
    for (TestListener delegate : delegates) {
      delegate.onTestRegistered(browser, testCase);
    }
  }
}