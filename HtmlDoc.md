# Html Doc #

So, you've got this lean and spare JsUnit test. And now, you want to test some DOM interactions and get some use out of testing on all these browsers. You could use the built in dom methods:
```

TestCase.prototype.setUp = function() {
  this.div = document.createElement('div');
  var p = document.createElement('p');
  div.appendChild(p);
  p.innerHTML = "bar";
  div.id = 'foo';
}

```

But, it's gets complicated. What if we could just have a simple declarative process to create html?

Well, it turns out, in JsTestDriver, you get just that. Using the simplified comment syntax you can either create the html scoped to your test, or appended to body:
## Html Scoped to a Test: ##
```

TestCase.prototype.testFoo = function() {
  assertUndefined(this.foo);
  /*:DOC foo = <div><p>foo</p></div>*/
  assertNotUndefined(this.foo);
};

```

## Html Appended to the Body ##
```

TestCase.prototype.testFoo = function() {
  /*:DOC += <div id="foo"></div> */
  assertNotNull(document.getElementById('foo'));
};
```