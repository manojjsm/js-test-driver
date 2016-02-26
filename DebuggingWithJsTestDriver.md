We have tried hard to make sure that your debugging experience with JsTestDriver is a good one. For the most part there is nothing special about the debugging JsTetsDriver tests. Here are typical steps:

  1. Start server and capture the browser which you would like to debug
  1. Run the failing test using `java -jar JsTestDriver.jar --tests MyTestCase.testIWantToDebug` This should load all of your JavaScript source files into the Browser
  1. Open your debugger and you should see all of your source files present in your debugger. Open the source file and place a breakpoint anywhere you wish.
  1. Rerun the test using command in step #2. The debugger should stop at your breakpoint where you should have full access all of your code, variables, and DOM.

Happy debugging...