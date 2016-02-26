A traditional JsTestDriver test case looks like this:
```
var MyTest = TestCase('MyTest');

MyTest.prototype.testSomething = function() {
  // make some assertions, etc.
};
```

We've extended the test cases to allow their test methods to accept a parameter. The parameter is a queue that accepts inline functions that represent sequential steps of the test. The test runner executes these steps in sequence, ending the test if at any point an assertion fails.

Here's an example that uses the queue but does not use any asynchronous operations.
```
var QueueTest = AsyncTestCase('QueueTest');

QueueTest.prototype.testSomething = function(queue) {
  var state = 0;

  queue.call('Step 1: assert the starting condition holds', function() {
    assertEquals(0, state);
  });

  queue.call('Step 2: increment our variable', function() {
    ++state;
  });

  queue.call('Step 3: assert the variable\'s value changed', function() {
    assertEquals(1, state);
  });
};
```
Note we are using AsyncTestCase() now instead of TestCase().  Also, the queue has a method call() that accepts an optional string to identify the step of the test, and an operation.  You could also omit the strings and just call `queue.call(function() {})`.  The functions passed to the queue's `call()` method usually have more than one line, but the above example is minimal.

Finally, in order to support testing asynchronous operations, as the test runner executes each step in the queue it passes a parameter to that step. The parameter is an empty pool of callback functions. You add your callback functions to the pool so the test runner can track that they are outstanding. The pool wraps your callback functions in a way that preserves their behavior but that notifies the test runner when an asynchronous system calls them. You call `var myTweakedCallback = callbacks.add(myOriginalCallback)` which returns the tweaked version. The test runner will not execute any subsequent step in the queue until all outstanding callbacks of the current step are complete. If the callbacks are not a called for an egregious amount of time, currently set to 30 seconds, the test fails.

Here's an example:
```
var AsynchronousTest = AsyncTestCase('AsynchronousTest');

AsynchronousTest.prototype.testSomethingComplicated = function(queue) {
  var state = 0;

  queue.call('Step 1: schedule the window to increment our variable 5 seconds from now.', function(callbacks) {
    var myCallback = callbacks.add(function() {
      ++state;
    });
    window.setTimeout(myCallback, 5000);
  });

  queue.call('Step 2: then assert our state variable changed', function() {
    assertEquals(1, state);
  });
};
```
Here's a more realistic example that communicates with a hypothetical HTTP server:

```
var XhrTest = AsyncTestCase('XhrTest');

XhrTest.prototype.testRequest = function(queue) {
  var xhr = new XMLHttpRequest();
  xhr.open('GET', '/some/path');

  var responseBody;
  
  queue.call('Step 1: send a request to the server and save the response body', function(callbacks) {
    var onStatusReceived = callbacks.add(function(status) {
      assertEquals(200, status);
    });
    
    var onBodyReceived = callbacks.add(function(body) {
      responseBody = body;
    });

    xhr.onreadystatechange = function() {
      if (xhr.readyState == 2) { // headers and status received
        onStatusReceived(xhr.status);
      } else if (xhr.readyState == 4) { // full body received
        onBodyReceived(xhr.responseText);
      }
    };

    xhr.send(null);
  });

  queue.call('Step 2: assert the response body matches what we expect', function() {
    assertEquals('hello', responseBody);
  });
};
```

## Advanced CallbackPool Features ##

### Noop Callbacks ###
Sometimes, you may want to do your assertions in the next test step, rather than within the callback you pass to the asynchronous system. CallbackPool has a method called noop() that returns a noop function that blocks the current step until it is called.

Example:
```
var NoopTest = AsyncTestCase('NoopTest');

NoopTest.prototype.testNoop = function(queue) {
  var asynchronousSystem = ...;
  assertFalse(asynchronousSystem.wasTriggered());
  queue.call('Trigger the system', function(callbacks) {
    asynchronousSystem.triggerLater(callbacks.noop());
  });
  queue.call('Assert about the system', function() {
    assertTrue(asynchronousSystem.wasTriggered());
  });
};
```

### Errbacks (Unexpected Callbacks) ###
Sometimes, you may want to fail the test immediately if your asynchronous system calls one callback function instead of another. For instance, imagine you pass two callback functions to your asynchronous system, one for a successful outcome, and another for an unsuccessful outcome. When one callback is executed, the other will never execute. They are mutually exclusive.

CallbackPool has a method called addErrback() precisely for this situation. Use addErrback() to return a function() that will fail the test if your asynchronous system calls it. AddErrback() accepts a string to identify which errback the asynchronous system called so you may easily identify it from the test failure message.

Example:
```
var ErrbackTest = AsyncTestCase('ErrbackTest');

ErrbackTest.prototype.testErrback = function(queue) {
  var asynchronousSystem = ...;
  assertFalse(asynchronousSystem.wasTriggered());
  queue.call('Trigger the system', function(callbacks) {
    asynchronousSystem.triggerLater(
        callbacks.noop(),
        callbacks.addErrback('Failed to trigger'));
  });
  queue.call('Assert about the system', function() {
    assertTrue(asynchronousSystem.wasTriggered());
  });
};
```

### Expect Multiple Invocations ###
Sometimes, your asynchronous system needs to call a single callback multiple times within one step of your test. Both add() and noop() accept an optional count argument that specifies how many times you expect your asynchronous system to call its callback.

Example:
```
var MultipleTest = AsyncTestCase('MultipleTest');

MultipleTest.prototype.testMultipleInvocations = function(queue) {
  queue.call('Expect three invocations', function(callbacks) {
    var count = 0;
    var intervalHandle;
    var callback = callbacks.add(function() {
      ++count;
      if (count >= 3) {
        window.clearInterval(intervalHandle);
      }
    }, 3); // expect callback to be called no less than 3 times
    intervalHandle = window.setInterval(callback, 1000);
  });
};
```