# Introduction #

The eclipse plugin for JS Test Driver allows you to enjoy all the benefits of JS Test Driver right from the comfort of Eclipse. This includes starting and stopping the server, capturing browsers, running and rerunning tests, filtering and viewing results.

# How to install #

In Eclipse, go to **Help** -> **Install new Software** (or whatever the similar menu item is in your version of eclipse.

Add the following url as an update site : **http://js-test-driver.googlecode.com/svn/update/**

Check the JS Test Driver checkbox and hit Next.

Hit Finish in the install details, accept any agreements and when Eclipse asks you to restart / apply changes, do so.

# How to use the plugin #

  * **Displaying the view**

Go to Window -> Show View -> Other, and select JS Test Driver view from the list. This should bring up the view from where you can interact with it.

  * **Global Preferences**

Go to Eclipse preferences (Either Window -> Preferences or Eclipse -> Preferences, depending on your OS). You should see a JS Test Driver menu item on the left. When you select that, you should see the following options

  * **Port** : This is the port that the JS Test Driver Server will start on
  * **Browser paths** : You can set the path to your browsers so that they can be launched from within eclipse itself.

  * **Server Panel**

The top part of the JS Test Driver view is the Server info panel. The green play button is to start the server. Once you click on that, it should change to a stop button, and the NOT RUNNING text should change to the capture URL. You can either copy paste this URL in a browser to capture it as a slave, or click one of the browser icons to launch the browser (only if you have set the browser path in the global preferences) to automatically capture it.

Once you have a browser captured, the icons should turn lit, and the bar should turn from yellow to green. This means you are now ready to run the tests.

  * **Running the tests**

There are a few ways to run the tests. The simplest way to run all your JS Test Driver tests is to go to Run Configurations and then add a new JS Test Driver configuration. Simply select your project and config file, and you are ready to run all your tests.

All other ways to run the tests require that you have a run configuration setup first. One you have done that, another way to run the tests once you have your browsers captured is to right click inside the text editor itself, and click Run as -> JS Test Driver Test. Inside the editor, it is context sensitive. By default, it will run all the tests inside that file. If you click on a line which has a test method definition, it will run just that test. If you select a bunch of lines, it will run only the tests in the highlighted portions.

The third way to run the tests is by selecting a js test file (or multiple) in the package and right clicking, selecting Run As -> JS Test Driver test. It will run all the tests in those files.

  * **Test Results View**

Now that you have run your tests, you can see the results similar to JUnit in the test results tree. It is grouped by Browser, then by Test case and finally all the test results. You can see the failure message or any log statements you had in your tests by double clicking a particular test.

The icons at the top also allow you to interact with tests. The Refresh Icon refreshes all captured browsers so that it holds a clean state. The Rerun button reruns the last launch configuration, and does nothing if you haven't previously run anything. And the last blue and red cross button filters your test results to show only failed tests.