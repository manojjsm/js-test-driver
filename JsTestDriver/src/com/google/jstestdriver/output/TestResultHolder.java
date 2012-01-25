package com.google.jstestdriver.output;

import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Multimaps.synchronizedMultimap;

import java.util.Collection;
import java.util.Map;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Singleton;
import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.FileResult;
import com.google.jstestdriver.TestCase;
import com.google.jstestdriver.TestResult;
import com.google.jstestdriver.hooks.TestListener;

/**
 * A data storage for test results. It listens on each browser for incoming test results,
 * on multiple threads. Then the XmlPrinter can use it to find the test results, so it can
 * produce our XML output files.
 * It should be bound as a Singleton to be sure the data is shared between these classes.
 * @author alexeagle@google.com (Alex Eagle)
 */
@Singleton
public class TestResultHolder implements TestListener {
  private static final class LinkListSupplier<T> implements
      Supplier<Collection<T>> {
    public Collection<T> get() {
          return newLinkedList();
        }
  }

  private final Multimap<BrowserInfo, TestResult> results;
  private final Multimap<BrowserInfo, TestCase> testCases;
  private Multimap<BrowserInfo, FileResult> fileResults;

  public TestResultHolder() {
    results = synchronizedMultimap(createMultiMap(Maps.<BrowserInfo, Collection<TestResult>>newLinkedHashMap()));
    testCases = synchronizedMultimap(createMultiMap(Maps.<BrowserInfo, Collection<TestCase>>newLinkedHashMap()));
    fileResults = synchronizedMultimap(createMultiMap(Maps.<BrowserInfo, Collection<FileResult>>newLinkedHashMap()));
  }

  private <T> Multimap<BrowserInfo, T> createMultiMap(
      Map<BrowserInfo, Collection<T>> map) {
    Supplier<Collection<T>> collectionSupplier = new LinkListSupplier<T>();
    Multimap<BrowserInfo, T> resultMultimap =
        Multimaps.<BrowserInfo, T>newMultimap(map, collectionSupplier);
    return resultMultimap;
  }

  /**
   * @return a map of browser name to test results from that browser
   */
  public Multimap<BrowserInfo, TestResult> getResults() {
    return results;
  }

  /**
   * @return a map of browser name to test results from that browser
   */
  public Multimap<BrowserInfo, TestCase> getCases() {
    return testCases;
  }

  /**
   * @return a map of browser name to test results from that browser
   */
  public Multimap<BrowserInfo, FileResult> getFileResults() {
    return fileResults;
  }

  public void onTestComplete(TestResult testResult) {
    results.put(testResult.getBrowserInfo(), testResult);
  }

  public void finish() {
  }

  public void onFileLoad(BrowserInfo browser, FileResult fileResult) {
    
  }

  @Override
  public void onTestRegistered(BrowserInfo browser, TestCase testCase) {
    // TODO Auto-generated method stub
    
  }
}
