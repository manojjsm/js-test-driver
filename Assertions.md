# Default Assertions #

## `fail([msg])` ##

Throws a JavaScript Error with given message string.

## `assert([msg], actual)` ##
## `assertTrue([msg], actual)` ##

Fails if the result isn't truthy. To use a message, add it as the first parameter.

## `assertFalse([msg], actual)` ##

Fails if the result isn't falsy.

## `assertEquals([msg], expected, actual)` ##

Fails if the expected and actual values can not be compared to be equal.

## `assertNotEquals([msg], expected, actual)` ##

Fails if the expected and actual values can be compared to be equal.

## `assertSame([msg], expected, actual)` ##

Fails if the expected and actual values are not references to the same object.

## `assertNotSame([msg], expected, actual)` ##

Fails if the expected and actual are references to the same object.

## `assertNull([msg], actual)` ##

Fails if the given value is not exactly null.

## `assertNotNull([msg], actual)` ##

Fails if the given value is exactly null.

## `assertUndefined([msg], actual)` ##

Fails if the given value is not undefined.

## `assertNotUndefined([msg], actual)` ##

Fails if the given value is undefined.

## `assertNaN([msg], actual)` ##

Fails if the given value is not a NaN.

## `assertNotNaN([msg], actual)` ##

Fails if the given value is a NaN.

## `assertException([msg], callback, error)` ##

Fails if the code in the callback does not throw the given error.

## `assertNoException([msg], callback)` ##

Fails if the code in the callback throws an error.

## `assertArray([msg], actual)` ##

Fails if the given value is not an Array.

## `assertTypeOf([msg], expected, value)` ##

Fails if the JavaScript type of the value isn't the expected string.

## `assertBoolean([msg], actual)` ##

Fails if the given value is not a Boolean. Convenience function to assertTypeOf.

## `assertFunction([msg], actual)` ##

Fails if the given value is not a Function. Convenience function to assertTypeOf.

## `assertObject([msg], actual)` ##

Fails if the given value is not an Object. Convenience function to assertTypeOf.

## `assertNumber([msg], actual)` ##

Fails if the given value is not a Number. Convenience function to assertTypeOf.

## `assertString([msg], actual)` ##

Fails if the given value is not a String. Convenience function to assertTypeOf.

## `assertMatch([msg], regexp, actual)` ##

Fails if the given value does not match the given regular expression.

## `assertNoMatch([msg], regexp, actual)` ##

Fails if the given value matches the given regular expression.

## `assertTagName([msg], tagName, element)` ##

Fails if the given DOM element is not of given tagName.

## `assertClassName([msg], className, element)` ##

Fails if the given DOM element does not have given CSS class name.

## `assertElementId([msg], id, element)` ##

Fails if the given DOM element does not have given ID.

## `assertInstanceOf([msg], constructor, actual)` ##

Fails if the given object is not an instance of given constructor.

## `assertNotInstanceOf([msg], constructor, actual)` ##

Fails if the given object is an instance of given constructor.