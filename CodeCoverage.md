# JsTestDriverCoverage #

Code coverage can be a valuable tool for gauging your code health. JsTestDriver makes it easy to generate code coverage for your JavaScript.

# Installation #

Coverage was developed as a plugin for JsTD. So, to enable it, download the coverage.jar and place it into the JsTestDriver plugins directory. Your directory structure should look like this:
```
/JsTestDriver.jar
/plugins/coverage.jar
```
Setup:
To enable coverage for a test configuration, add the following entry to the configuration after your files:
```
plugin:
 - name: "coverage"
   jar: "plugins/coverage.jar"
   module: "com.google.jstestdriver.coverage.CoverageModule"
```
This tells JsTD that it needs to add the plugin named coverage, from the module com.google.jstestdriver.coverage.CoverageModule that resides in the jar "plugins/coverage.jar".

For a working example, look at [coverage.conf](http://code.google.com/p/js-test-driver/source/browse/trunk/JsTestDriver/coverage.conf).

Running:
Just run the test as usual. If there is no --testOutput flag defined, coverage will be reported for each file as a percentage, with an aggregate percent displayed. With a --testOutput defined, line level coverage is recorded in the testOuput directory as <config filename>-coverage.dat in the LCOV format.

Generating Report:
The  jsTestDriver.conf-coverage.dat is compatible with the LCOV (http://ltp.sourceforge.net/coverage/lcov.php) visualizer. After a successful coverage run, execute

`genhtml jsTestDriver.conf-coverage.dat.`

Further details are here: (http://ltp.sourceforge.net/coverage/lcov/genhtml.1.php)