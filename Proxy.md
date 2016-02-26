# Introduction #

JsTestDriver has the ability to gateway unrecognized requests to servers-under-test. You may find this useful for larger integration tests that need to communicate with a backend server.

# Details #

To activate the gateway, add a gateway section to your jsTestDriver.conf file:

```
gateway:
 - {matcher: "/matchedPath", server: "http://localhost:7000"}
 - {matcher: "/wildcardPath/*", server: "http://localhost:8000/"}
 - {matcher: "*", server: "http://localhost:9000"}
```

The above configuration sends requests to `/matchedPath` along to the `http://localhost:7000`, requests to `/wildcardPath/{anything}` along to `http://localhost:8000/{anything}`, and any remaining requests to `{anything}` along to `http://localhost:9000/{anything}`.

The `gateway` entry of the configuration file is a list of matchers mapped to server addresses (including an optional path). When handling unknown requests, JsTestDriver iterates sequentially through the list of matchers, finds the first matching pattern, and forwards the request along to the server URL, appending any extra path information matched by a wildcard.

Matcher patterns come in three varieties:
  * Literal matchers, e.g. `/matchedPath`
  * Suffix matchers, e.g. `/wildcardPath/*`
  * Prefix matchers, e.g. `*.pdf`

# Path Collisions #

Sometimes your server-under-test may handle HTTP requests on URLs that JsTestDriver already handles. For instance, you may handle requests on `/cache` that are vital to your service and that you would like to test.

Use the following flag `--serverHandlerPrefix jstd` to prefix all JsTestDriver-specific request paths with `/jstd` so they won't collide with your service.

You have to use this flag when starting server and running tests both.

List of all folders used by jstd could be found [here](http://code.google.com/p/js-test-driver/source/browse/trunk/JsTestDriver/src/com/google/jstestdriver/server/handlers/JstdHandlersModule.java#119).

# Security #

Be sure you control or trust the servers that you enter into your `gateway` configuration entry. The gateway bypasses the browser's same-origin policy and forwards the requests almost verbatim.