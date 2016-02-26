# Introduction #

JsTestDriver aims to help javascript developers use good TDD practices
and aims to make writing unit tests as easy as what already exists
today for java with JUnit.


# Laying out Your Project #

We have created a sample Hello-World project for you here:
http://code.google.com/p/js-test-driver/source/browse/#svn/samples/hello-world

There are three things you need to set up your project with JsTestDriver:
  1. **source folder:** Although not strictly required we recommend that you create a source folder for your application. In our case we have named it `src`.
  1. **test folder:** Again not strictly necessary, but keeping your test code away from your production code is a good practice. In our case we have named it `src-test`.
  1. **configuration file:** By default the JsTestDriver runner looks for the configuration file named `jsTestDriver.conf` in the current directory. We recommend that you name it same way, or you will have to enter it as a command line option all of the time.

# Writing your production code #

You write your JavaScript production code as you normaly would. We have created a sample `Greeter` class for domenstration purposes.
```
myapp = {};

myapp.Greeter = function() { };

myapp.Greeter.prototype.greet = function(name) {
  return "Hello " + name + "!";
};
```


# Writing your test code #

We have tried to follow the JUnit testing conventions as much as possible. If you are familiar with JUnit, than you should be right at home with JsTestDriver.

```
GreeterTest = TestCase("GreeterTest");

GreeterTest.prototype.testGreet = function() {
  var greeter = new myapp.Greeter();
  assertEquals("Hello World!", greeter.greet("World"));
};
```

The JsTestDriver needs to know about all of your `TestCase` classes for this reason you declare a new test class using the notation below. Once the `TestCase` is declared it acts as a normal class declaration in JavaScript. You can use normal prototype declarations to create test methods. See TestCase for more information.
```
GreeterTest = TestCase("GreeterTest");
```

Test methods are declared on the prototype. Optionally you can declare `setUp()` and `tearDown()` method just as in JUnit.
```
GreeterTest.prototype.testGreet = function() {
  var greeter = new myapp.Greeter();
  assertEquals("Hello World.", greeter.greet("World"));
};
```

# Writing configuration file #

If you are familiar with Java, you can think of the configuration file as classpath for JavaScript. The file contains information about which JavaScript source files need to be loaded into the browser and in which order. The configuration file is in [YAML](http://www.yaml.org/) format.

```
server: http://localhost:9876

load:
  - src/*.js
  - src-test/*.js
```

The server directive tells the test runner where to look for the test server. If the directive is not specified you will need to enter the server information through a command line flag.

The load directive tells the test runner which JavaScript files to load into the browsers and in which order. The directives can contain "`*`" for globing multiple files at once. In our case we are saying to load all of the files in the src folder followed by all of the files in the srt-test folder. For more information see ConfigurationFile.



# Starting the server & Capturing browsers #

Before you can run any of your tests you need to start the test server and capture at least one slave browser. The server does not have to reside on the machine where the test runner is, and the browsers themselves can be at different machines as well.

Starting server on port 9876:
```
java -jar JsTestDriver.jar --port 9876
```

Then capture a browser by going to the URL of your JsTestDriver server, if running on localhost it should be:
```
http://localhost:9876
```

Click the link `Capture This Browser`. Your browser is now captured and used by the JsTestDriver server.

You can also directly capture the browser by using the URL:
```
http://localhost:9876/capture
```

You can also tell the server to autocapture the browser by providing a path to the browser exectable on the cammand line. Multiple browsers can be specified if separated by a comma ','.
```
java -jar JsTestDriver.jar --port 9876 --browser firefoxpath,chromepath
```

For full line of command line flags refer to CommandLineFlags.


# Running the tests #

Now that we have server running and at least one browser captured we can start running tests. Tests can be executed from the command line using:
```
java -jar JsTestDriver.jar --tests all
```

As long as the jsTestDriver.conf file is present in the current directory the test runner will read it and use it to locate the server and the files which need to be loaded into the browser. It will then load any files (which have changed) into the browser and run your tests reporting the results an standard out.

```
Total 2 tests (Passed: 2; Fails: 0; Errors: 0) (0.00 ms)
  Safari 528.16: Run 1 tests (Passed: 1; Fails: 0; Errors 0) (0.00 ms)
  Firefox 1.9.0.10: Run 1 tests (Passed: 1; Fails: 0; Errors 0) (0.00 ms)
```


# Automatically running tests in Eclipse #

For discussion how to set up your tests to run automatically on save see: http://misko.hevery.com/2009/05/07/configure-your-ide-to-run-your-tests-automatically/