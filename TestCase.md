# Declaring a TestCase #

It is necessary to tell the test runner about your tests suites. Most test runners deal with this by letting you build up test suites. This can be tedious and error prone resulting for un-run tests. In JsTestDriver we register the test case during the declaration phase. There are two ways to declare tests in the TestCase.

Using prototype:
```
MyTestCase = TestCase("MyTestCase");

MyTestCase.prototype.testA = function(){
};

MyTestCase.prototype.testB = function(){
};
```

Using inline declaration:

```
TestCase("MyTestCase", {
    testA:function(){
    },
    testB:function(){
    }
  });
```

# Test life-cycle #

When tests execute they follow JUnit life-cycle:
  1. Instantiate new instance of TestCase for each test method.
  1. Execute the `setUp()` method.
  1. Execute the `testMethod()` method.
  1. Execute the `tearDown()` method.


# Asserts #

The asserts are declared in [Asserts.js](http://code.google.com/p/js-test-driver/source/browse/trunk/JsTestDriver/src/com/google/jstestdriver/javascript/Asserts.js). They follow JUnit assert conventions. This means that first argument is message and is optionally followed by expected and actual values.

## `expectAsserts(count)` ##

This asserts tells the JsTestDriver how many asserts to expect. This is useful with callbacks.

```
MyTestCase.prototype.testExample = function () {
  expectAsserts(1);
  var worker = new Worker();
  var doSomething = {};
  worker.listener = function (work){
    assertSame(doSomething, work);
  };
  worker.perform(doSomething);
};
```

In the above example we have a callback function which contains an `assert`. However if the `Worker` does not call the callback function than the test would pass even thought the callback did not get called. To prevent this false positive the `expectAsserts` tells the test runner that the test is only valid if one `assert` has been called.

## `fail([msg])` ##

## `assertTrue([msg], actual)` ##

## `assertFalse([msg], actual)` ##

## `assertEquals([msg], expected, actual)` ##

## `assertSame([msg], expected, actual)` ##

## `assertNotSame([msg], expected, actual)` ##

## `assertNull([msg], actual)` ##

## `assertNotNull([msg], actual)` ##

# Console #

Some browsers provide console object which can be forwarded to the test runner output with `-captureConsole` flag. See CommandLineFlags. In addition there is `jstestdriver.console` class provided for you which is always forwarded to the test runners standard output.

```
ConsoleTest = TestCase("ConsoleTest");

ConsoleTest.prototype.testGreet = function() {
  jstestdriver.console.log("JsTestDriver", "Hello World!");
  console.log("Browser", "Hello World!");
};
```

The result of the above test when run is shown (the browser message will only be shown when `-captureConsole` is used.)

```
$ java -jar JsTestDriver.jar --tests all --captureConsole
.
Total 1 tests (Passed: 1; Fails: 0; Errors: 0) (1.00 ms)
  Safari 528.16: Run 1 tests (Passed: 1; Fails: 0; Errors 0) (1.00 ms)
    ConsoleTest.testGreet passed (1.00 ms)
      [LOG] JsTestDriver Hello World!
      [LOG] Browser Hello World!
```