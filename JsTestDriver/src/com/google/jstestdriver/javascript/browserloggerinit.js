
jstestdriver.browserLogger = jstestdriver.BrowserLogger.create(window.top.location.toString(),
    function() {jstestdriver.jQuery.ajax.apply(jstestdriver.jQuery,
        arguments)});
