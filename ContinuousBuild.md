To run your tests as part of the continuous build step we provide an easy way to launch the server, capture browsers, run tests, report the status and than automatically shut down the browsers and the server. The command to do all of this is below. The key is to specify `--port` and `--tests` flags together

```
java -jar JsTestDriver.jar --port 4224 --browser broserpath1,browserpath2 --tests all --testOutput testOutputDir
```

Here is sample output of the server.
```
$ java -jar JsTestDriver.jar --port 4224 --browser open --tests all --testOutput .
May 21, 2009 7:13:24 PM org.slf4j.impl.JCLLoggerAdapter info
INFO: Logging to org.slf4j.impl.JCLLoggerAdapter(org.mortbay.log) via org.mortbay.log.Slf4jLog
May 21, 2009 7:13:24 PM org.slf4j.impl.JCLLoggerAdapter info
INFO: jetty-6.1.x
May 21, 2009 7:13:24 PM org.slf4j.impl.JCLLoggerAdapter info
INFO: Started SocketConnector@0.0.0.0:4224
May 21, 2009 7:13:24 PM org.slf4j.impl.JCLLoggerAdapter info
INFO: Browser Captured:
  Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_5_7; en-us)
  AppleWebKit/528.16 (KHTML, like Gecko) Version/4.0
  Safari/528.16 version 5.0 (Macintosh; U; Intel Mac OS X 10_5_7; en-us)
  AppleWebKit/528.16 (KHTML, like Gecko) Version/4.0 Safari/528.16 (1)
Total 1 tests (Passed: 1; Fails: 0; Errors: 0) (0.00 ms)
  Safari 528.16: Run 1 tests (Passed: 1; Fails: 0; Errors 0) (0.00 ms)
```

And here is a sample XML output file
```
$ cat TEST-com.google.jstestdriver.1.xml 
<?xml version="1.0" encoding="UTF-8"?>
<testsuite name="com.google.jstestdriver.1">
<testcase classname="GreeterTest" name="testGreet:Safari528.16" time="0.0010"/>
<system-out><![CDATA[[LOG] JsTestDriverHello World!
]]></system-out>
</testsuite>
```

---
## Integration with Existing CI Systems ##

Here are some examples of how you can integrate JsTestDriver with existing Continuous Builds. This is not an exhaustive list, but you can use this as a starting point on how to integrate it with your system.

### Atlassion Bamboo & QUnit ###

by Mike Arvela <mike@arvela.net>

As I managed to come up with a solution myself, I thought it would be a good idea to share it. The approach might not be flawless, but it's the first one that seemed to work. Feel free to post improvements and suggestions.

What I did in a nutshell:
  * Launch an instance of Xvfb, a virtual framebuffer
  * Using JsTestDriver:
    * launch an instance of Firefox into the virtual framebuffer (headlessly)
    * capture the Firefox instance and run the test suite
    * generate JUnit-compliant test results .XML
  * Use Bamboo to inspect the results file to pass or fail the build

I will next go through the more detailed phases. This is what my my directory structure ended up looking like:
```
lib/
    JsTestDriver.jar
test/
    qunit/
            equiv.js
            QUnitAdapter.js
    jsTestDriver.conf
    run_js_tests.sh
    tests.js
test-reports/
build.xml
```

On the build server:
  * Install Xvfb (apt-get install Xvfb)
  * Install Firefox (apt-get install firefox)

Into your application to be built:
  * Install JsTestDriver: http://code.google.com/p/js-test-driver/
    * add the QUnit adapters equiv.js and QUnitAdapter.js
    * configure JsTestDriver (jsTestDriver.conf):

```
server: http://localhost:4224

load:
# Load QUnit adapters (may be omitted if QUnit is not used)
  - qunit/equiv.js
  - qunit/QUnitAdapter.js   

# Tests themselves (you'll want to add more files)
  - tests.js
```


Create a script file for running the unit tests and generating test results (example in Bash, run\_js\_tests.sh):

```
#!/bin/bash
# directory to write output XML (if this doesn't exist, the results will not be generated!)
OUTPUT_DIR="../test-reports"
mkdir $OUTPUT_DIR

XVFB=`which Xvfb`
if [ "$?" -eq 1 ];
then
    echo "Xvfb not found."
    exit 1
fi

FIREFOX=`which firefox`
if [ "$?" -eq 1 ];
then
    echo "Firefox not found."
    exit 1
fi

$XVFB :99 -ac &    # launch virtual framebuffer into the background
PID_XVFB="$!"      # take the process ID
export DISPLAY=:99 # set display to use that of the xvfb

# run the tests
java -jar ../lib/JsTestDriver.jar --config jsTestDriver.conf --port 4224 --browser $FIREFOX --tests all --testOutput $OUTPUT_DIR

kill $PID_XVFB     # shut down xvfb (firefox will shut down cleanly by JsTestDriver)
echo "Done."
```

Create an Ant target that calls the script:
```
<target name="test">        
    <exec executable="cmd" osfamily="windows">
        <!-- This might contain something different in a Windows environment -->
    </exec>

    <exec executable="/bin/bash" dir="test" osfamily="unix">
        <arg value="run_js_tests.sh" />
    </exec>
</target>   
```
Finally, tell the Bamboo build plan to both invoke the test target and look for JUnit test results. Here the default "/test-reports/