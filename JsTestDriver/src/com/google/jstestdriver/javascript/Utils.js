/*
 * Copyright 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

goog.provide('jstestdriver.utils');

jstestdriver.convertToJson = function(delegate) {
  var serialize = jstestdriver.parameterSerialize
  return function(url, data, callback, type) {
    delegate(url, serialize(data), callback, type);
  };
};


jstestdriver.parameterSerialize = function(data) {
  var modifiedData = {};
  for (var key in data) {
    modifiedData[key] = JSON.stringify(data[key]);
  }
  return modifiedData;
};


jstestdriver.bind = function(context, func) {
  function bound() {
    return func.apply(context, arguments);
  };
  bound.toString = function() {
    return "bound: " + context + " to: " + func;
  }
  return bound;
};


jstestdriver.extractId = function(url) {
  return url.match(/\/id\/(\d+)\//)[1];
};


jstestdriver.createPath = function(basePath, path) {
  var prefix = basePath.match(/^(.*)\/(slave|runner|bcr)\//)[1];
  return prefix + path;
};


jstestdriver.getBrowserFriendlyName = function() {
  if (jstestdriver.jQuery.browser.safari) {
    if (navigator.userAgent.indexOf('Chrome') != -1) {
      return 'Chrome';
    }
    return 'Safari';
  } else if (jstestdriver.jQuery.browser.opera) {
    return 'Opera';
  } else if (jstestdriver.jQuery.browser.msie) {
    return 'Internet Explorer';
  } else if (jstestdriver.jQuery.browser.mozilla) {
    if (navigator.userAgent.indexOf('Firefox') != -1) {
      return 'Firefox';
    }
    return 'Mozilla';
  }
};


jstestdriver.getBrowserFriendlyVersion = function() {
  if (jstestdriver.jQuery.browser.msie) {
    if (typeof XDomainRequest != 'undefined') {
      return '8.0';
    } 
  } else if (jstestdriver.jQuery.browser.safari) {
    if (navigator.appVersion.indexOf('Chrome/') != -1) {
      return navigator.appVersion.match(/Chrome\/(.*)\s/)[1];
    }
  }
  return jstestdriver.jQuery.browser.version;
};

jstestdriver.trim = function(str) {
  return str.replace(/(^\s*)|(\s*$)/g,'');
};


/**
 * Renders an html string as a dom nodes.
 * @param {string} htmlString The string to be rendered as html.
 * @param {Document} owningDocument The window that should own the html.
 */
jstestdriver.toHtml = function(htmlString, owningDocument) {
  var fragment = owningDocument.createDocumentFragment();
  var wrapper = owningDocument.createElement('div');
  wrapper.innerHTML = jstestdriver.trim(jstestdriver.stripHtmlComments(htmlString));
  while(wrapper.firstChild) {
    fragment.appendChild(wrapper.firstChild);
  }
  var ret =  fragment.childNodes.length > 1 ? fragment : fragment.firstChild;
  return ret;
};


jstestdriver.stripHtmlComments = function(htmlString) {
  var stripped = [];
  function getCommentIndices(offset) {
    var start = htmlString.indexOf('<!--', offset);
    var stop = htmlString.indexOf('-->', offset) + '-->'.length;
    if (start == -1) {
      return null;
    }
    return {
      'start' : start,
      'stop' : stop
    };
  }
  var offset = 0;
  while(true) {
    var comment = getCommentIndices(offset);
    if (!comment) {
      stripped.push(htmlString.slice(offset));
      break;
    }
    var frag = htmlString.slice(offset, comment.start);
    stripped.push(frag);
    offset = comment.stop;
  }
  return stripped.join('');
}


/**
 * Appends html string to the body.
 * @param {string} htmlString The string to be rendered as html.
 * @param {Document} owningDocument The window that should own the html.
 */
jstestdriver.appendHtml = function(htmlString, owningDocument) {
  var node = jstestdriver.toHtml(htmlString, owningDocument);
  jstestdriver.jQuery(owningDocument.body).append(node);
};


/**
 * @return {Number} The ms since the epoch.
 */
jstestdriver.now = function() { return new Date().getTime();}


/**
 * Creates a wrapper for jQuery.ajax that make a synchronous post
 * @param {jQuery} jQuery
 * @return {function(url, data):null}
 */
jstestdriver.createSynchPost = function(jQuery) {
  return jstestdriver.convertToJson(function(url, data) {
    return jQuery.ajax({
      'async' : false,
      'data' : data,
      'type' : 'POST',
      'url' : url,
      'contentType': 'application/x-www-form-urlencoded; charset=UTF-8'
    });
  });
};

jstestdriver.createAsynchPost = function(jQuery) {
  return jstestdriver.convertToJson(function(url, data, callback, type) {
    return jQuery.ajax({
      'data' : data,
      'type' : 'POST',
      'url' : url,
      'success' : callback,
      'dataType' : type,
      'contentType': 'application/x-www-form-urlencoded; charset=UTF-8'
    });
  });
};

jstestdriver.utils = {};

/**
 * Checks to see if an object is a a certain native type.
 * @param instance An instance to check.
 * @param nativeType A string of the type expected.
 * @returns True if of that type.
 */
jstestdriver.utils.isNative = function(instance, nativeType) {
  try {
    var typeString = String(Object.prototype.toString.apply(instance));
    return typeString.toLowerCase().indexOf(nativeType.toLowerCase()) != -1;
  } catch (e) {
    return false;
  }
};

/**
 * Serializes
 */
jstestdriver.utils.serializeErrors = function(errors) {
  var out = [];
  out.push('[');
  for (var i = 0; i < errors.length; ++i) {
    jstestdriver.utils.serializeErrorToArray(errors[i], out);
    if (i < errors.length - 1) {
      out.push(',');
    }
  }
  out.push(']');
  return out.join('');
};

jstestdriver.utils.serializeErrorToArray = function(error, out) {
  if (jstestdriver.utils.isNative(error, 'Error')) {
    out.push('{');
    out.push('"message":');
    jstestdriver.utils.serializeObjectToArray(error.message, out);
    jstestdriver.utils.serializePropertyOnObject('name', error, out);
    jstestdriver.utils.serializePropertyOnObject('description', error, out);
    jstestdriver.utils.serializePropertyOnObject('fileName', error, out);
    jstestdriver.utils.serializePropertyOnObject('lineNumber', error, out);
    jstestdriver.utils.serializePropertyOnObject('number', error, out);
    jstestdriver.utils.serializePropertyOnObject('stack', error, out);
    out.push('}');
  } else {
    out.push(jstestdriver.utils.serializeObject(error));
  }
};

jstestdriver.utils.serializeObject = function(obj) {
  var out = [];
  jstestdriver.utils.serializeObjectToArray(obj, out);
  return out.join('');
};


jstestdriver.utils.serializeObjectToArray =
   function(obj, opt_out){
  var out = opt_out || out;
  if (jstestdriver.utils.isNative(obj, 'Array')) {
    out.push('[');
    var arr = /** @type {Array.<Object>} */ (obj);
    for ( var i = 0; i < arr.length; i++) {
      jstestdriver.utils.serializeObjectToArray(arr[i], out);
      if (i < arr.length - 1) {
        out.push(',');
      }
    }
    out.push(']');
  } else {
    jstestdriver.utils.toJsonArray(out, obj, false, []);
  }
  return out;
};


jstestdriver.utils.serializePropertyOnObject = function(name, obj, out) {
  if (name in obj) {
    out.push(',');
    out.push('"' + name + '":');
    jstestdriver.utils.serializeObjectToArray(obj[name], out);
  }
};

jstestdriver.utils.quote = function(str) {
  return '"' + str.replace(/\\/g, '\\\\').
                   replace(/"/g, '\\"').
                   replace(/\n/g, '\\n').
                   replace(/\f/g, '\\f').
                   replace(/\r/g, '\\r').
                   replace(/\t/g, '\\t').
                   replace(/\v/g, '\\v') + '"';
};

jstestdriver.utils.quoteUnicode = function(string) {
  var str = jstestdriver.utils.quote(string);
  var chars = [];
  for ( var i = 0; i < str.length; i++) {
    var ch = str.charCodeAt(i);
    if (ch < 128) {
      chars.push(str.charAt(i));
    } else {
      var encode = "000" + ch.toString(16);
      chars.push("\\u" + encode.substring(encode.length - 4));
    }
  }
  return chars.join('');
}

jstestdriver.utils.includes = function(list, obj) {
  for (var i = 0; i < list.length; i++) {
    if (list[i] === obj) {
      return true;
    }
  }
  return false;
};


jstestdriver.utils.toJsonArray = function (buf, obj, pretty, stack) {
  if (jstestdriver.utils.isNative(obj, 'object')) {
    if (obj === window) {
      buf.push(jstestdriver.utils.quote('WINDOW'));
      return;
    }

    if (obj === document) {
      buf.push(jstestdriver.utils.quote('DOCUMENT'));
      return;
    }

    if (jstestdriver.utils.includes(stack, obj)) {
      buf.push(jstestdriver.utils.quote('RECURSION'));
      return;
    }
    stack.push(obj);
  }
  if (obj === null) {
    buf.push('null');
  } else if (obj instanceof RegExp) {
    buf.push(jstestdriver.utils.quoteUnicode(obj.toString()));
  } else if (jstestdriver.utils.isNative(obj, 'function')) {
    return;
  } else if (jstestdriver.utils.isNative(obj, 'boolean')) {
    buf.push('' + obj);
  } else if (jstestdriver.utils.isNative(obj, 'number')) {
    if (isNaN(obj)) {
      buf.push('null');
    } else {
      buf.push('' + obj);
    }
  } else if (jstestdriver.utils.isNative(obj, 'string')) {
    return buf.push(jstestdriver.utils.quoteUnicode(obj));
  } else if (jstestdriver.utils.isNative(obj, 'object')) {
    if (jstestdriver.utils.isNative(obj, 'array')) {
      buf.push("[");
      var len = obj.length;
      var sep = false;
      for(var i=0; i<len; i++) {
        var item = obj[i];
        if (sep) {
          buf.push(",");
        }
        if (!(item instanceof RegExp) &&
            (jstestdriver.utils.isNative(item, 'function') ||
             jstestdriver.utils.isNative(item, 'undefined'))) {
          buf.push('null');
        } else {
          jstestdriver.utils.toJsonArray(buf, item, pretty, stack);
        }
        sep = true;
      }
      buf.push("]");
    } else if (jstestdriver.utils.isNative(obj, 'date')) {
      buf.push(jstestdriver.utils.quoteUnicode(obj.toString()));
    } else {
      buf.push("{");
      if (pretty) buf.push(pretty);
      var comma = false;
      var childPretty = pretty ? pretty + "  " : false;
      var keys = [];
      for(var k in obj) {
        if (obj[k] === undefined)
          continue;
        keys.push(k);
      }
      keys.sort();
      for ( var keyIndex = 0; keyIndex < keys.length; keyIndex++) {
        var key = keys[keyIndex];
        var value = obj[key];
        if (typeof value != 'function') {
          if (comma) {
            buf.push(",");
            if (pretty) buf.push(pretty);
          }
          buf.push(jstestdriver.utils.quote(key));
          buf.push(":");
          jstestdriver.utils.toJsonArray(buf, value, childPretty, stack);
          comma = true;
        }
      }
      buf.push("}");
    }
  }
  if (jstestdriver.utils.isNative(obj, 'object')) {
    stack.pop();
  }
}

// needed for compatibility with the Jasmine plugin.
// TODO(corysmith): Fix that.
jstestdriver.angular = {};
/**
 * Serializes an object to Json while elegantly handling recursion.
 * @param {Object} obj Object to serialize.
 * @return {String} The serealized object.
 */
jstestdriver.angular.toJson = function(obj) {
  var out = [];
  jstestdriver.utils.toJsonArray(out, obj, false, []);
  return ' '.join(out);
}
