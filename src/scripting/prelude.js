
var ScriptManager = Java.type('scripting.ScriptManager');

var alert = ScriptManager.alert;
var scriptError = ScriptManager.scriptError;

var filter = function (pred, list) {
    if (list.filter) {
        return list.filter(pred);
    } else {
        var result = [];
        for (var i=0; i<list.length; i++) {
            if (pred(list[i])) {
               result.push(list[i]);
            }
        }
        return result;
    }
};

var map = function (fn, list) {
    if (list.map) {
        return list.map(fn);
    } else {
        var result = [];
        for (var i=0; i<list.length; i++) {
            result.push(fn(list[i], i));
        }
        return result;
    }
};

var reduce = function (op, initial, list) {
    if (list.reduce) {
        return list.reduce(op, initial);
    } else {
        var result = initial;
        for (var i=0; i<list.length; i++) {
            result = op(result, list[i]);
        }
        return result;
    }
};

Array.prototype.toString = function () {
    var sb = [];
    for (var i=0; i<this.length-1; i++) {
        sb.push(this[i] + ',');
    }
    sb.push(this[this.length-1]);
    return '[' + sb.join(' ') + ']';
};

Object.prototype.toString = function () {
    var sb = [];
    Object.keys(this).forEach(function (key) {
        sb.push(key + ": " + this[key]);
    });
    return '{' + sb.join(', ') + '}';
};

function list(xs) {
    var result = [];
    for (var i=0; i<xs.length; i++) {
        result.push(xs[i]);
    }
    return result;
}

