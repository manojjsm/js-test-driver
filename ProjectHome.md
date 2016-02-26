[Quick Navigation](Navigation.md)

## News! ##
Release 1.3.4-a
First Release of 2012, 1.3.4-a: http://code.google.com/p/js-test-driver/downloads/list

Bugs and Fixes:
  * fixed the reset on syntax error bug.
  * Better error reporting when --preloadFiles is passed in.
  * Fixed [issue 302](https://code.google.com/p/js-test-driver/issues/detail?id=302): The parameter --dryRunFor TestCase name does not work properly
  * Fixed [issue 145](https://code.google.com/p/js-test-driver/issues/detail?id=145): assets are served with content type "text/plain" (may be missing your exact content type. If so, file a new bug)
  * Fixed capturing with more than 50 test cases with --preloadFiles
  * Fixed --browserTimeout not being respected with the --browsers flag.

Features:
  * --basePath and base\_path now accept multiple base paths.

News:
  * We'll be moving to Git on Google Code in the next two weeks.
  * With big thanks to m.jurcovicova, the eclipse plugin is back! The update site is here: http://js-test-driver.googlecode.com/svn/update/
  * Finally, it was inevitable, JsTestDriver has a Google plus page: https://plus.google.com/b/100948987862442440080/



Release 1.3.3d
  * Fixed [issue 273](https://code.google.com/p/js-test-driver/issues/detail?id=273), Invalid JSON is being sent to the browser with Windows paths
  * Improved queuing for simultaneous clients
Release 1.3.3c
  * Fixed [issue 278](https://code.google.com/p/js-test-driver/issues/detail?id=278), Noisy output when using --captureConsole
  * Fixed [issue 276](https://code.google.com/p/js-test-driver/issues/detail?id=276), Absolute Paths on Windows fail
  * Fixed [issue 273](https://code.google.com/p/js-test-driver/issues/detail?id=273), Invalid JSON is being sent to the browser with Windows paths

Release 1.3.3b
  * Fixed [issue 223](https://code.google.com/p/js-test-driver/issues/detail?id=223) for the last time. Really.

Release 1.3.3a
  * New test isolation plugin, documentation forthcoming: [issue 272](https://code.google.com/p/js-test-driver/issues/detail?id=272)
  * Fix for [issue 223](https://code.google.com/p/js-test-driver/issues/detail?id=223), which was real pain without a windows box
  * Add test step description to expired callback error messages in the asynch tests.
  * Fix [issue 234](https://code.google.com/p/js-test-driver/issues/detail?id=234) by failing fast when tests throw exceptions during test steps instead of waiting for all outstanding callbacks to expire.
  * Fixes  [issue 226](https://code.google.com/p/js-test-driver/issues/detail?id=226)  by adding HEAD to the HttpMethod enum.
  * SSL support to the jstd server, there are some certificate issues.
  * Proxy is now a custom Gateway, see documentation for details: http://code.google.com/p/js-test-driver/wiki/Gateway
  * Now inlines preloaded files on initial capture or reset. Loading should be faster.
  * New --captureAddress flag for to customize the way a browser is captured. (experimental)

Release 1.3.2
  * Fixed [issue 199](https://code.google.com/p/js-test-driver/issues/detail?id=199), starting a browser with the server
  * Improved failure handling, now displays multiple errors from AsyncTestCase correctly
  * Fixed coverage handling "empty" files
  * Fixed multiple path handling issues including the paths used in LOCV files.
  * Upon loading the UTF-8 DOM will not be considered data (I hope). [Issue 85](https://code.google.com/p/js-test-driver/issues/detail?id=85) related.
  * Improved error reporting for relative path resolution in files and plugins.

---

Release 1.3.1
  * Fixed error reporting for Asynch tests
  * Fixed [issue 197](https://code.google.com/p/js-test-driver/issues/detail?id=197), requiring a configuration file to start the server.
  * Fixed NPE when instrumenting an empty file.

---

[Proxy](Proxy.md) documentation!

---

Release 1.3.0
  * Increased stability of the server during refreshes and multiple test runs.
  * Escapes xml result file names to work on Os X, Ubuntu and Windows.
  * Handling for errors thrown in both the test and the teardown.
  * Correct formatting for errors in Asynch tests.
  * Added the "test" attribute to the config file, allowing the separation of tests and dependencies. (See http://code.google.com/p/js-test-driver/source/browse/trunk/JsTestDriver/jsTestDriver.conf for an example)
  * Added the "timeout" attribute to the config file, for indicating the total amount of time of a test run. (See http://code.google.com/p/js-test-driver/source/browse/trunk/JsTestDriver/jsTestDriver.conf for an example)
  * Added "basepath" attribute to the config file, for declaring the base directory for all file resolutions. (Docs forthcoming.)
  * Removed the basepath for inbrowser debugging. (shorter filenames while debugging)
  * Fixed [issue 159](https://code.google.com/p/js-test-driver/issues/detail?id=159) to speed up server start time.
  * Upgrade to Gson 1.6
  * Fixed dependency ordering on file change: Now, jstd will reload the changed file and all files that are loaded after that file with a persistent server.
  * fixed issue of losing tests when uploading takes longer than 2 seconds
  * [issue 145](https://code.google.com/p/js-test-driver/issues/detail?id=145) fixed the mimetype handling
  * Browser ids are now unique, no more recapture the browser by accident.
  * Changed the js execution break to happen between tests, not test suites. Should result in fewer browser panics with heavy tests.
  * [issue 154](https://code.google.com/p/js-test-driver/issues/detail?id=154), null pointer while running tests
  * [issue 190](https://code.google.com/p/js-test-driver/issues/detail?id=190), relative paths are in properly handled (regression introduced between 1.2.2 and 1.3.0)
  * Introduced --handlerPathPrefix to prefix all jstd urls on the server with a given prefix.
  * New proxy feature for running asynch tests against production server. Documentation forthcoming from Robert Dionne.
  * HTMLDOC now strips html comments due to browser insertion issues.
  * /quit now causes the jstd server to stop (in case you lost the process)
  * Asserts patches by Christian Johanson, vastly improved asserts.
  * Asynchronous testing api now matches documentation.
  * Lots of internal refactoring and further tweaks to plugin architecture (still very alpha.)

---


JsTestDriver is featured in a new book on test-driving your Javascript code. Check it out! http://tddjs.com/

---


The goal of JsTestDriver is to build a JavaScript test runner which:
  1. easily integrates with continuous builds systems and
  1. allows running tests on multiple browsers quickly to ease TDD style development.

# Features #
  * **Command Line Control:** JavaScript code in the browser by design is not allowed to interact with the filesystem or command line. This creates problem when trying to run tests in an automated fashion. A good test runner needs to allow control from the command line so that tests can be launched from an automation script. This also implies that the tests need to be able to publish their results to standard out or a file, outside of the browser sandbox. The JavaScript takes care of controlling and marshaling test results from the browser sandbox and make them available on the command line interface (or Java API).
  * **Parallel Test Executions Across Browsers:** JavaScript development means write once and test everywhere. It is quite common when developing that code passes in one browser but fails on others. If the developer is testing with just one browser then the most likely outcome is that the code works only in that browser. Checking the code into a continuous build than runs the code in all browsers and results in failure which are hard to debug, since the check-in can be quite large. JsTestDriver allows you to run your tests in parallel on many browsers and platforms at once. This is possible because the JsTestDriver server can capture any number of local or remote browsers.
  * **Fast Tests Execution:** TDD development asks to run tests often. Many JavaScript tests runners require you to write an HTML wrapper file which you refresh to re-run the tests, as a result you end up with lots of HTML wrappers which are equivalent to test suites. This means that you can only run one tests suite on one browser at a time. It also means that the browser has to continually re-parse the production code as it executes the individual tests suites. Finally running individual tests is often not possible if the only control is browser refresh. We take a different approach, JavaScript loads the production/test code at the beginning and keeps them in the browser. It then reloads only the source files which have changed. This greatly speeds up test execution, since in most cases the browser only needs to re-parse a single file to re-run it.
  * **Full Control of DOM:** Many JavaScript test harnesses report the test results into the DOM. This means that portions of the DOM can not be modified by the tests or you will lose the test result information. Since JsTestDriver reports the test status on the command line, tests are free to modify the DOM in any way they need for the test. JsTestDriver then resets the state of the DOM for the next test.
  * **Easy Configuration:** JsTestDriver comes bundled as a single JAR file. There is no need to write HTML wrapper classes which have complex script tags inclusions to bootstrap the test runner. All you have to write is your source file, tests file, and a configuration file listing location of your source/test files and you are ready to go.
  * **Code Coverage**: CodeCoverage can be computed for your tests
  * **Declarative HTML Injection**: Need specific DOM to be loaded to your test executes, no problem: HtmlDoc


# Overview #

JsTestDriver consist of a single JAR file which contains everything you need to get started. For in depth discussion of command line option see GettingStarted.

Here is an overview of how JsTestDriver works at runtime:

<img src='http://js-test-driver.googlecode.com/svn/wiki/Overview.png' />

  1. You start off by launching the **Server**. The server is responsible for loading the test runner code into the browser and in the process turning the browser into a slave. Once the browser is a slave it can be controlled to do any action from the command line. In our case the server will send commands to the browser to load source code, execute arbitrary functions and report the results back to the requester. A single server can capture any number of browsers, even from other machines across the network. The server does not need to be on your development machine. This is useful if your primary development platform is different than your primary test platform. For example I develop code on mac but want to run my tests against Internet Explorer on Windows.
  1. After you have a server running you can **capture** any number of browsers. The capturing process can be automatic through a command line option. In most cases you point your browser to the server by visiting the server URL. Once the browser is captured you can forget about the browser (minimize/hide it), since the server can run any number of tests on the browser for an indefinite period of time. A common use case is to capture the browsers once and then use them for the remainder of the development during that day. The only time you will need to interact with the browser is if you wish to debug code with the browser's debugger.
  1. At this point you need write some **source and tests code**. There is no need to write any HTML wrappers, the code is normal JavaScript code with tests resembling JUnit. Once you have a test we are ready to run our tests.
  1. One last thing you need to do is to create a **configuration file**. This file tells the JsTestDriver which JavaScript files need to be loaded into the browser and in which order (optionally, where the server is located.) Think of the configuration file as the Java classpath and your JavaScript files as JARs. The good news is that this file is usually very short since for most projects a single line which says load everything (we support globing) is sufficient.
  1. You are now ready to **run your tests**. Form now on all you need to do is to rerun this last step to re-run the tests. The server is intelligent to reload only the files which have changed into the browser and in the right order. Since the browser is kept ready the test execute extremely fast. The effect is more noticeable as the project gets bigger.

# See JsTestDriver in Action #
http://www.youtube.com/watch?v=V4wYrR6t5gE

<a href='http://www.youtube.com/watch?feature=player_embedded&v=V4wYrR6t5gE' target='_blank'><img src='http://img.youtube.com/vi/V4wYrR6t5gE/0.jpg' width='640' height=410 /></a>

# JsTestDriver for Eclipse #

Update Site: http://js-test-driver.googlecode.com/svn/update/

More details: [Eclipse plugin details](UsingTheEclipsePlugin.md)

<img src='http://js-test-driver.googlecode.com/svn/wiki/eclipse.png' />

# For IntelliJ IDEA #

Look for JSTestDriver in the Plugin Manager inside IDEA.

<img src='http://js-test-driver.googlecode.com/svn/wiki/idea.png' />

# Group #

Question? Answers? Feature Requests? Bored? join our group: [js-test-driver@googlegroups.com](mailto:js-test-driver@googlegroups.com)