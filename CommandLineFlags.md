# Help #
```
$ java -jar JsTestDriver.jar --help
 --browser VAR             : The path to the browser executables, separated by ','. Arguments can be passed to the executables separated by ';'. '%s' will be replaced with the initial url. If '%s' is not included the url will be appended to the argument list.
 --browserTimeout VAR      : The ms before a browser is declared dead.
 --captureConsole          : Capture the console (if possible) from the browser
 --config VAL              : Loads the configuration file
 --dryRunFor VAR           : Outputs the number of tests that are going to be run as well as their names for a set of expressions or all to see all the tests
 --help                    : Help
 --port N                  : The port on which to start the JsTestDriver server
 --preloadFiles            : Preload the js files
 --requiredBrowsers VAR    : Browsers that all actions must be run on.
 --reset                   : Resets the runner
 --server VAL              : The server to which to send the command
 --serverHandlerPrefix VAL : Whether the handlers will be prefixed with jstd
 --testOutput VAL          : A directory to which serialize the results of the tests as XML
 --tests VAR               : Run the tests specified by the supplied regular expression. Use '#' to denote the separation between a testcase and a test.
 --verbose                 : Displays more information during a run
 --plugins VAL[,VAL]       : Comma separated list of paths to plugin jars.
 --config VAL              : Path to configuration file.
 --basePath VAL            : Override the base path in the configuration file. Defaults to the parent directory of the configuration file.
 --runnerMode VAL          : The configuration of the logging and frequency that the runner reports actions: DEBUG, DEBUG_NO_TRACE, DEBUG_OBSERVE, PROFILE, QUIET (default), INFO
 --serverHandlerPrefix     : Prefix for all jstd paths (to avoid conflict with proxy)
```

# Server Options #

## `--port` ##
When starting up a server all you need to specify is the `--port` on which port number should the server be running. The same port number is than used to both capture the browsers as well as for the test runner to connect to.

### Status ###

Once the server is running you can visit the server base URL with your browser to see that status of the servers.  If you started the server on port 4224 than you can visit the status of the server by visiting http://localhost:4224. Here is a sample output of the server status.

<blockquote>
<a href='http://localhost:4224/capture'>Capture This Browser</a><br />
<br />
Captured Browsers:<br />
<br />
Id: 2<br />
Name: Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.5; en-US; rv:1.9.0.10) Gecko/2009042315 Firefox/3.0.10<br />
Version: 5.0 (Macintosh; en-US)<br />
Operating System: MacIntel<br />
<br />
Id: 1<br />
Name: Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_7; en-us) AppleWebKit/528.16 (KHTML, like Gecko)<br /> Version/4.0 Safari/528.16<br />
Version: 5.0 (Macintosh; U; Intel Mac OS X 10_5_7; en-us) AppleWebKit/528.16 (KHTML, like Gecko) Version/4.0 Safari/528.16<br />
Operating System: MacIntel<br>
</blockquote>

### Browser Capture ###

Visiting http://localhost:4224/capture will automatically capture the browser by the server. At this point the browser is available to be used for running tests.

The browser does not have to be on the same machine. This allows a server running on one platform to capture browsers from other platform giving the developer full access to all browsers/platform combinations.

## `--browser` ##

If you wish to auto-capture a browser on startup you can specify a list of paths (separated by  comma '`,`') to the browser on the command line using this option. This will automatically launch the browser and capture it. This is useful when setting up JsTestDriver in ContinuousBuild mode. Arguments can be passed to the executables separated by ';'. '%s' will be replaced with the capture url. If '%s' is not included the capture url will be appended to the argument list.


# Test Runner Options #

If `--port` option is not present than we are running in a test runner mode. This client is run by the developer to run the tests in automated fashion from the IDE.

## `--captureConsole` ##

Many browsers have a `console` object which has logging methods for debugging.  When this option is specified than we try to intercept the console log messages and display them on the test runner standard out. This allows the developer to see what is going on on a remote browsers console to aid in debugging. (FireFox does not allow overwriting of the console object.)

## `--config` ##

By default the test runner reads the `jsTestDriver.conf` file in the current directory to get its configuration. This option allows you to override the location of the ConfigurationFile.

## `--reset` ##

Asks the server to reload the browsers. Sometimes it is passible that slave gets into an inconsistent state. For example a test has overridden some global variable. Sending a reset signal should restore the slave to standard configuration.

## `--server` ##

Specifies the location of the server to connect to for running the tests. By default we try to use the location form the ConfigurationFile, but this option allows the developer to override the configuration file.

## `--testOutput` ##

For ContinuousBuild it is often necessary to publish the test results in a file for later processing. This option specifies which directory the tests results should be written to. The test results are written in XML JUnit format which should be compatible with most of the continuous build systems out there. There will be one XML file written per browser captured. The format should be compatible with most continuous build systems which understand JUnit XML format.

## `--tests` ##

Specifies which tests should be run.
  * `all` special keyword to run all of the tests.
  * `TestCaseName` run all tests for that test case.
  * `TestCaseName#testName` run only the specified test, useful when debugging a single test. See DebuggingWithJsTestDriver.
  * Additionally, it accepts regular expressions for any part of the string. '#' is reserved, and unexpected results may happen when using # in a test name.

## `--verbose` ##

Normally the test runner tries to run as many tests as passible before reporting status of the test to the user. This is done for performance reasons. But it i possible that a test can hang the runner in this case you have no idea which tests is causing the problem since nothing is displayed. This option makes the test runner verbose and as a result it reports on every test as it runs allowing you to pinpoint the failure.