# Introduction #

Info needed for developers and contributors.

# Building the core jar #
Just checkout the project, install ant, and run `ant jar` in the root folder of the project.

## Releasing ##
Run `ant release`
_You should use Java6 and Ant 1.7.1 or greater, or your codesite password will be echoed. Don't worry, the Javac task will still target 1.5 class files._

This will build and test the code, tag the revision in svn, and publish the artifacts to the codesite, marking them Featured. You just need to remove the Featured tag from the files of the previous release.

## Troubleshooting ##

Gah!
```
instrument:
    [instr] processing instrumentation path ...

BUILD FAILED
com.vladium.emma.EMMARuntimeException: [UNEXPECTED_FAILURE] unexpected failure java.lang.ArrayIndexOutOfBoundsException: 14, please submit a bug report to: 'http://sourceforge.net/projects/emma'
        at com.vladium.emma.instr.InstrProcessorST._run(InstrProcessorST.java:784)
        at com.vladium.emma.Processor.run(Processor.java:54)
        at com.vladium.emma.instr.instrTask.execute(instrTask.java:77)
        at com.vladium.emma.emmaTask.execute(emmaTask.java:57)
        at org.apache.tools.ant.UnknownElement.execute(UnknownElement.java:288)
        at sun.reflect.GeneratedMethodAccessor3.invoke(Unknown Source)
```
you're running a 64-bit JVM, which emma doesn't work with. Run a 32-bit JVM.

# Building the Eclipse plugin #
Set some environment variables, so the Eclipse plugin will build. You may want to download the RCP/PDE plugin development version of Eclipse so that the plugin tools are available. Otherwise you'll need to figure out the appropriate plugins to add to an Eclipse install.
  * ECLIPSE\_LAUNCHER\_JAR=<eclipse dir>/plugins/org.eclipse.equinox.launcher\_1.0.101.R34x\_v20081125.jar
  * ECLIPSE\_PDE\_XML=<eclipse dir>/plugins/org.eclipse.pde.build\_3.4.1.R34x\_v20081217/scripts/build.xml
  * ECLIPSE\_BASE\_DIR=<eclipse dir>

Then run ant.

# Working on the IDEA plugin #
See [JsTestDriver IntelliJ plugin Development](http://confluence.jetbrains.net/display/WI/Development+of+JsTestDriver+IntelliJ+plugin) page

## To build a release ##
Point an Ant variable to the location of your IDEA installation (can't point to a "platform" IDE here, as the javac2.jar isn't included with those products).
```
ant -DIDEA_LIB_PATH=/Applications/IntelliJ\ IDEA\ 9.0.3\ CE.app/lib jar
```
or on Windows, `IDEA_LIB_PATH=C:\\devel\\idea-8\\lib\\`


## Releasing the plugin ##
  * Change the version number in META-INF/plugin.xml to match the release of the core library that's included (the IDEA plugin repo uses this to determine our version)
  * Update the changelog in the same file, adding a section for your release at the top. This will be displayed inside of IDEA, which helps users decide to update
  * Carefully build all the code - the core JSTD jar as well as the plugin, with the `zip` target
  * Log into the update site at http://plugins.intellij.net/ username jakeherringbone, password [Alex for it](ask.md)
  * Go to http://plugins.intellij.net/plugin/edit/?pid=4468
  * Fill in the form:
    * Select the zip file you just built
    * Copy the current change info into the 'Add info to RSS feed and news channels' box
    * Source code download URL http://code.google.com/p/js-test-driver/source/browse/
    * Forum URL http://groups.google.com/group/js-test-driver