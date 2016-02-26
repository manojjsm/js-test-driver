**JsTestDriver IntelliJ plugin** allows you to enjoy [all the benefits of JsTestDriver](http://code.google.com/p/js-test-driver/wiki/DesignPrinciples) right from the comfort of your IDE (WebStorm, PhpStorm, IntelliJ IDEA, RubyMine, PyCharm or AppCode).

It is the open-source project under the terms of [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).

### Features ###

  * starting and stopping the server;
  * running and rerunning tests;
  * filtering and viewing test results, navigation from results to source code;
  * jumping from JavaScript exception stacktrace to source code;
  * support for JsTestDriver configuration file: syntax and error highlighting, basic completion, navigation to referenced files;
  * capturing messages sent to `console.log()`;
  * support for Jasmine, QUnit and JsTestDriver built-in assertion frameworks:
    * quick-fixes for enabling global symbol reference resolving for each assertion framework (if you have QUnit or Jasmine tests in a project, you will be prompted to install the corresponding [adapter](http://code.google.com/p/js-test-driver/wiki/XUnitCompatibility));
    * contextual code generation actions (Alt+Insert) for creating new tests, setup and teardown methods for each assertion framework;
    * [declarative HTML injection](http://code.google.com/p/js-test-driver/wiki/HtmlDoc) support for JsTestDriver built-in assertion framework.

### Installation ###

Please visit [Installation](http://confluence.jetbrains.net/display/WI/Installation+of+JsTestDriver+IntelliJ+plugin) page.

### Getting started ###

Please visit [Getting Started](http://confluence.jetbrains.net/display/WI/Getting+Started+with+JsTestDriver+IntelliJ+plugin) page.

### Releases & Changelog ###

You can check out the [plugin page](http://plugins.intellij.net/plugin/?id=4468) for more information about the releases.


### Roadmap ###

You can find features that we are planning to implement in the future on the [Roadmap](http://confluence.jetbrains.net/display/WI/JsTestDriver+IntelliJ+plugin+roadmap) page.

### Issue tracker ###

If you've found a bug, a glitch or anything that doesn't work well, please file an issue in the [WebStorm/PhpStorm project issue tracker](http://youtrack.jetbrains.net/issues/WI#newissue=yes) (select _"Plugin: JsTestDriver"_ subsystem when creating an issue).

Please don't file an issue in the http://code.google.com/p/js-test-driver/issues/list?q=label:IntelliJ. Those bugs are handled normally, but it's an old way.

### For developers ###

If you are considering participation in the development of the plugin, or just going to build it yourself, please visit [Development](http://confluence.jetbrains.net/display/WI/Development+of+JsTestDriver+IntelliJ+plugin) page.