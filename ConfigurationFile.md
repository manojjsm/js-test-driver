# Introduction #

Configuration file is written in [YAML](http://www.yaml.org) and is used to tell the test runner which files to load to browser and in which order. By default the JsTestDriver looks for the configuration file in the current directory and with the name `jsTestDriver.conf`. You can use `--config` command line option to specify a different file.

Example:
```
server: http://localhost:4224

load:
  - src/*.js

test:
  - src-test/*.js

exclude:
 - uselessfile.js

serve:
 - css/main.css

proxy:
 - {matcher: "*", server: "http://localhost/whatever"}

plugin:
 - name: "coverage"
   jar: "lib/jstestdriver/coverage.jar"
   module: "com.google.jstestdriver.coverage.CoverageModule"

timeout: 90

```

## `server:` ##
Specifies the default location of the server. This value can be overridden with a command line option `--server`. See CommandLineFlags.

## `load:` ##
List of files to load to browser before the test can be run. We support globing with `*`. The files are loaded in the same order as specified in the configuration file or alphabetically if using globing.

You can declare external scripts by adding the http address of the script as a loadedable item.

## `test:` ##
A list of test sources to run.

## `exclude:` ##
Never load this file. Used in conjunction with globing and `load`. Useful saying load everything except these files.

## `serve:` ##
Load static files (images, css, html) so that they can be accessed on the same domain as jstd.

## `proxy:` ##
Set jstd to behave as proxy. See [proxy](http://code.google.com/p/js-test-driver/wiki/Proxy).

## `plugin:` ##
Load jstd plugin. See [plugins](http://code.google.com/p/js-test-driver/wiki/Plugins).

## `timeout:` ##
Timeout in seconds.