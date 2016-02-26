Our goal is to have a better way to write JavaScript tests

# Overview #
JsTestDriver is to unit-tests what Selenium is to end-to-end tests. The goal of JS Test Driver is to make JavaScript unit test development as seamless as and easy as Java unit tests.

Features
  * Support TDD development model
  * Super fast test execution
  * Super easy set up
  * Seamless integration with existing IDEs
  * Debugger Support
  * Federated test executions across all browsers and platforms
  * Focus on the command line and the continuous build
  * Designed to be farm friendly
  * HTML Loading Support

## Support TDD development model ##

Test-Driven-Development encourages to write lots of small focused tests and run them often. The goal of JsTestDriver was to enable the execution of the complete test suite on each save. This places severe constraints on latency. The goal was to enable the running of large (hundreds/thousands) of tests in under one second. If I am working in an IDE I save my files often. This means that as a user I can only tolerate a second or two for my tests results to come back.

Traditional test frameworks fall short of this goal. First it is not possible to run all tests in interactive mode. The reason is that each test case consist of HTML file and a collection of tests (usually around 10). The developer then loads the HTML into the Browser. To run the tests you have to refresh the browser. The Browser then reloads, reparses all of the resources and executes only the tests in this test case. Therefore as a developer I can only run one test case at a time. This implies that it is easy to break other test cases and not notice it until you check it in and the continuous build runs all of the tests cases in your tests suite. Constant refreshing of the browser also breaks the development flow, and makes it that the developer does not run the tests often.

In contrast JsTestDriver runs all of the tests every time you save.

## Super fast test execution ##

If we wish to execute all of the tests in under one sec we have to rethink the way the tests are run. Lets see where the latency of running tests comes from in traditional test frameworks:
  * Start up of browser can take several seconds
  * Constant reloading of the HTML test files causes the reloading of all of the resources which puts strain on the network.
  * If the resource is JavaScript than reload also implies the re-parsing of the JavaScript AST

We designed the JsTestDriver for speed from ground up. To achieve super fast execution times JsTestDriver consist of server and client code. The server captures any number of web browsers and keeps them "hot" and ready for test execution. The browser loads and parses the HTML file only once. The original HTML file contains code which turns the browser into a slave listening on the server for commands to execute. Each browser then evals any code which the server sends a request for. The server then loads your production and tests code and runs the tests for you. If you change any code the server only loads the changed code into the browser. This greatly lowers the amount of reparsing which the browser needs to do. Additionally the server is eager and it loads the code aggressively into the browser even before it is ready to run. Because the browser and their documents with your application and test code are long lived running tests is in the millisecond range. Allowing JsTestDriver to finish the whole test suite in sub-second times.

## Super easy set up ##

Writing a test should be as simple as:
  1. start the JsTestsDriver
  1. write test
  1. write production code. The test running should be automatic on each save.

In contrast traditional test frameworks require you to write an HTML file. This file needs to contain JavaScript dependencies to the framework, production code and tests. This means that each HTML file has a complex project dependent set up. Most developers tend to solve this by cutting and pasting the initialization files between the HTML test files.

In contrast JsTestDriver does not require any HTML file to be written by the developer. It really is as easy as writing your tests in JavaScript format.

## Seamless integration with existing IDEs ##

Traditional test frameworks don't play nice with the IDEs. This is because they do not run in the IDE but in the browser. The context switch does not seem like a lot but it is enough to make the process clunky. Our goal is to be able to right-click on the test in the IDE and run it from within the IDE in isolation (not possible now) or as part of the whole test suite (not possible now) and have the results report inside the IDE UI (not possible now) where you can directly click on the stack trace and go to the source of the exception (again not possible with existing tools).

JsTestDriver consist  of a client and a server. The server can run anywhere, but usually on the same machine. The client on the other hand is an embedable code which can be run from command line or from within any tool, such as an IDE.

## Debugger support ##

Each browser has an existing debugger support. Our goal with JsTestDriver is to make sure that we do not break any of the existing debugger workflows and that you can place breakpoint in your production or test code.

## Federated test executions across all browsers and platforms ##

Traditional JavaScript test frameworks run inside of a single browser. This means that each developer can run the code only in one browser at a time. This means that most tests are not run on most browsers most of the time. To make the matters worse the developer usually only has a single platform available at a time, which further complicates the problem.

In JsTestDriver the server can capture any number of browsers from any number of machines and any number of platforms. We can even have multiple versions of the same browser captured at a same time. When tests are run they are executed on all browsers in parallel. This means that when the developer runs the tests the answer will include the pass or fail information from all of the browsers at once. This means that you can easily compare browser (miss)behavior.

## Focus on the command line and the continuous build ##

Most traditional test frameworks focus on running the tests and forget that tests need to be run in automated fashion in the continuous build machine. The biggest problem being how do I get the result of my tests out of the browser and in to a JUnit compliant XML file. Often times developers write a JUnit target which starts Selenium and controls the HTML tests runner. Java then uses selenium to load individual HTML files and records the pass fail information. This is extremely slow, not to mention backwards. Why do I need to write Java code to run JavaScript tests?

## Designed to be farm friendly ##

JsTestDriver is designed with farm availability in mind. The JsTestDriver server can run anywhere. This means that you can have a large pool of servers running all configured with the right browser and ready to run the tests for any developer. This will greatly help with the continuous build machines ability to execute the tests across multiple platforms. It will also enable the teams to easily set up continuous builds.

# Future Road Map #

## HTML loading support ##

Since we no longer have HTML files we need an alternative method of loading the HTML test data into the tests. This is currently supported as a string literal, but we are working on a cleaner way to load HTML content into tests.

## Support for code coverage through on the fly code instrumentation ##

Once the basics are in place we would like to include support for on the fly code instrumentation of the code to gather coverage numbers. Since the server sends all of the data to the server, the server can do on the fly instrumentation of the code.

## Status Console/Charts ##

A status page for the server which would show the state of the browsers, coverage, performance, and allowed the control of the browsers remotely.