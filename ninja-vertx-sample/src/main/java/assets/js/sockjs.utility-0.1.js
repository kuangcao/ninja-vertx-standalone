var SockJSUtility = {};
(function() {
    // 連線方法
    (function(exports) {
        exports.connect = function(options) {
            // 連線Socket
            return new SockJSUtility.Socket(options);
        };
    })(SockJSUtility);


    // JS工具
    (function(exports) {

        var JSUtility = {
            /**
             *  合併兩物件
             */
            merge : function (target, additional, deep, lastseen) {
                var seen = lastseen || [],
                    depth = typeof deep == 'undefined' ? 2 : deep,
                    prop,
                    target = target || {};


                for (prop in additional) {
                    if (additional.hasOwnProperty(prop) && JSUtility.indexOf(seen, prop) < 0) {
                        // 是合併物件自己的屬性
                        if (typeof target[prop] !== 'object' || !depth) {
                            // 合併物件
                            target[prop] = additional[prop];
                            seen.push(additional[prop]);
                        } else {
                            // 合併下一層
                            JSUtility.merge(target[prop], additional[prop], depth - 1, seen);
                        }
                    }
                }
                return target;
            },
            /**
             *  合併prototype
             */
            mixin : function(ctor, ctor2) {
                JSUtility.merge(ctor.prototype, ctor2.prototype);
            },
            /**
             *  陣列索引
             */
            indexOf : function(arr, o, i) {
                for (var j = arr.length,
                         i = i < 0 ?
                             i + j < 0 ? 0 : i + j
                             : i || 0;
                     i < j && arr[i] !== o;
                     i++) {}

                return j <= i ? -1 : i;
            },
            /**
             *  是否為陣列
             */
            isArray : Array.isArray || function(obj) {
                return Object.prototype.toString.call(obj) === '[object Array]';
            },
            /**
             *  是否為物件
             */
            isObject : function(obj) {
                return Object.prototype.toString.call(obj) === '[object Object]';
            }
        };

        exports.JSUtility = JSUtility;

    })(SockJSUtility);


    // 事件處理
    (function(exports) {

        exports.EventEmitter = EventEmitter;

        function EventEmitter() {};

        EventEmitter.prototype = {
            constructor : EventEmitter,
            // 加入事件傾聽器
            on : function(name, fn) {
                if (!this.$events) {
                    // 沒有事件清單，建立空的事件清單
                    this.$events = {};
                }

                if (!this.$events[name]) {
                    // 沒有該事件
                    this.$events[name] = fn;
                } else if (exports.JSUtility.isArray(this.$events[name])) {
                    // 3個以上重複事件
                    // 有該事件，如果是事件陣列
                    this.$events[name].push(fn);
                } else {
                    // 第2個重複事件
                    this.$events[name] = [this.$events[name], fn];
                }
                return this;
            },
            // 移除事件傾聽器
            removeListener : function(name, fn) {
                if (this.$events && this.$events[name]) {
                    // 存在事件清單，且有該事件物件
                    var list = this.$events[name];

                    if (exports.JSUtility.isArray(list)) {
                        // 是陣列事件
                        var pos = -1;

                        for (var i = 0, l = list.length; i < l; i++) {
                            if (list[i] === fn || (list[i].listener && list[i].listener === fn)) {
                                // 如果事件相等，紀錄事件位置
                                pos = i;
                                break;
                            }
                        }

                        if (pos < 0) {
                            // 找不到該事件
                            return this;
                        }

                        // 移除該事件
                        list.splice(pos, 1);

                        if (!list.length) {
                            // 事件清單為空，移除整個事件變數
                            delete this.$events[name];
                        }
                    } else if (list === fn || (list.listener && list.listener === fn)) {
                        // 是單一事件
                        // 如果事件相等，移除該事件
                        delete this.$events[name];
                    }
                }
                return this;
            },
            removeAllListeners : function(name) {
                if (name === undefined) {
                    // 沒有傳入事件名稱，事件清單設為空物件(移除所有事件)
                    this.$events = {};
                    return this;
                }

                if (this.$events && this.$events[name]) {
                    // 有事件清單，有傳入事件名稱，將該事件清空
                    this.$events[name] = null;
                }

                return this;
            },
            emit : function(name) {
                if (!this.$events) {
                    // 沒有事件清單
                    return false;
                }
                // 指定事件名稱
                var handler = this.$events[name];

                if (!handler) {
                    // 沒有該事件名稱
                    return false;
                }
                // 移除掉第一個參數(事件名稱)
                var args = Array.prototype.slice.call(arguments, 1);

                if ('function' == typeof handler) {
                    // 只有一個事件function
                    handler.apply(this, args);
                } else if (io.util.isArray(handler)) {
                    // 是事件陣列
                    var listeners = handler.slice();

                    for (var i = 0, l = listeners.length; i < l; i++) {
                        listeners[i].apply(this, args);
                    }
                } else {
                    return false;
                }
                return true;
            }
        };
        // 複製事件方法
        EventEmitter.prototype.off = EventEmitter.prototype.removeListener;
        EventEmitter.prototype.$emit = EventEmitter.prototype.emit;

    })(SockJSUtility);


    // Socket連線工具
    (function(exports) {
        exports.Socket = Socket;

        function Socket(options) {
            // 選項設定
            this.utli_options = {
                // 伺服器位址
                host : document.domain,
                // 是否需要重新連線
                need_reconnect : true,
                // 重新連線延遲時間(預設0.5秒)
                reconnection_delay : 500,
                // 重新連線限制(無限制)
                reconnection_delay_limit : Infinity,
                // 最大重連嘗試次數
                max_reconnection_attempts : 20
            };
            // 是否已連線(預設為false)
            this.connected = false;
            // 是否連線中(預設為false)
            this.connecting = false;
            // 是否中斷目前連線
            this.disconnecting = false;

            // 合併選項值
            exports.JSUtility.merge(this.utli_options , options);
            // Socket Connection
            this.socket = function () {};
            // 連線到Socket
            this.connect();
        };

        Socket.prototype = {
            constructor : Socket,
            // 傳送訊息到 Socket Server
            send : function (event , messages) {
                var msg;
                if (!event)
                {
                    // 沒有傳遞任何參數
                    return false;
                }
                if (!messages)
                {
                    // 只有第一個參數，沒有後面訊息存在，則第一個參數為訊息
                    msg = JSON.stringify(event);
                }
                else
                {
                    // 使用事件+訊息方式傳遞
                    msg = {
                        event : event,
                        data : messages
                    };
                    msg = JSON.stringify(msg);
                }
                this.socket.send(msg);
            },
            // 中斷目前連線到Socket的連線
            disconnect : function () {
                this.disconnecting = true;
            },
            // 連線到Socket
            connect : function () {
                if (this.utli_options && this.utli_options.host && this.utli_options.host.length > 0)
                {
                    // 預存 Socket 物件
                    var self = this;
                    // 重新連線計時器
                    var reconnectionTimer = null;
                    // 嘗試重連次數
                    var reconnectionAttempts = 0;
                    // 延遲秒數(從頭計算)
                    var reconnectionDelay = this.utli_options['reconnection_delay'];

                    // 設定 callback function
                    /**
                     *  連線開啟時
                     */
                    var onopen = function() {
                        // console.log('socket opened!!');
                        // 是否已連線
                        self.connected = true;
                        // 是否連線中
                        self.connecting = false;
                        // 重設連線設定
                        resetReconnect();
                        // 送出"onopen"連線成功事件，給使用者自訂function
                        self.emit('onopen');
                        // 模擬呼叫預設callback
                        self.onopen && self.onopen();
                    };


                    /**
                     *  收到訊息時
                     */
                    var onmessage = function (server_message){
                        // 送出"onmessage"接收訊息事件，給使用者自訂function
                        self.emit('onmessage' , server_message);

                        try{
                            // 嘗試解析字串(訊息事件處理)
                            var messages = JSON.parse(server_message.data);
                            if (messages.event && messages.data)
                            {
                                // 若有伺服器訊息，且有指定訊息事件
                                self.emit(messages.event , messages.data);
                            }
                        }
                        catch(e)
                        {
                            // 無正確解析JSON，回傳非JSON字串
                        }
                        // 沒有事件，回傳原字串
                        // 模擬呼叫預設callback
                        self.onmessage && self.onmessage(server_message.data);
                    };


                    /**
                     *  連線關閉時
                     */
                    var onclose = function() {
                        // console.log('onclose?????');
                        // 是否已連線
                        self.connected = false;
                        // 是否連線中
                        self.connecting = false;
                        // 重新連線
                        reconnect();
                        // 送出"onclose"關閉連線事件，給使用者自訂function
                        self.emit('onclose');
                        // 模擬呼叫預設callback
                        self.onclose && self.onclose();
                    };


                    /**
                     *  重設連線設定
                     */
                    var resetReconnect = function () {
                        // 清除重連機制
                        clearInterval(reconnectionTimer);
                        // 嘗試重連次數歸零
                        reconnectionAttempts = 0;
                        // 是否中斷目前連線設為預設
                        self.disconnecting = false;
                        // 延遲秒數(從頭計算)
                        reconnectionDelay = self.utli_options['reconnection_delay'];
                    };


                    /**
                     *  重新連線到 Socket Server
                     */
                    var reconnect = function () {
                        /**
                         *  嘗試重新連線到 Socket Server
                         */
                        var attemptReconnect = function () {
                            if (self.disconnecting || self.connected) {
                                // 已連線成功，重設連線設定
                                return resetReconnect();
                            }

                            if (self.connecting) {
                                // 連線中不重連
                                return;
                            }

                            if (reconnectionAttempts++ < self.utli_options['max_reconnection_attempts'])
                            {
                                // 沒有超過嘗試重連上限次數(預設嘗試重連20次)，繼續重連
                                if (reconnectionDelay < self.utli_options['reconnection_delay_limit'])
                                {
                                    // 沒有超過重連秒數限制，下次重試秒數+(0.5秒*重連嘗試次數 + 0~5秒亂數-避免Client同時重連增加Server負擔)
                                    var addDelaySeconds = reconnectionAttempts*500 + (Math.random()*5);
                                    reconnectionDelay += addDelaySeconds;
                                }
                                // 嘗試連線到 Socket Server
                                attemptConnect();
                            }
                            // else 超過最大重試次數
                        };

                        // 嘗試重連 Server
                        reconnectionTimer = setTimeout(attemptReconnect, reconnectionDelay);
                    };


                    /**
                     *  連線到 Socket Server
                     */
                    var attemptConnect = function(){
                        // 是否已連線
                        self.connected = false;
                        // 是否連線中
                        self.connecting = true;
                        // 刪除原 Socket
                        self.socket = null;
                        delete self.socket;
                        // 連線
                        self.socket = new SockJS(self.utli_options.host);
                        // callback
                        // 連線開啟時
                        self.socket.onopen = onopen;
                        // 收到訊息時
                        self.socket.onmessage = onmessage;
                        // 連線關閉時
                        self.socket.onclose = onclose;
                    };

                    // 嘗試連線到 Socket Server
                    attemptConnect();
                }
                return this;
            }
        };

        // 繼承事件處理
        exports.JSUtility.mixin(Socket, exports.EventEmitter);

    })(SockJSUtility);
})();


/**
 * Based on JSON2 (http://www.JSON.org/js.html).
 */
// 實作 JSON function
(function(nativeJSON) {
    "use strict";

    var JSON = {};

    if (nativeJSON) {
        // 有原生JSON，重建JSON函式
        return;
    }
    // 沒有原生JSON，重建JSON函式
    nativeJSON = JSON;

    function f(n) {
        // Format integers to have at least two digits.
        return n < 10 ? '0' + n : n;
    }

    function date(d, key) {
        return isFinite(d.valueOf()) ?
        d.getUTCFullYear() + '-' +
        f(d.getUTCMonth() + 1) + '-' +
        f(d.getUTCDate()) + 'T' +
        f(d.getUTCHours()) + ':' +
        f(d.getUTCMinutes()) + ':' +
        f(d.getUTCSeconds()) + 'Z' : null;
    };

    var cx = /[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        escapable = /[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,
        gap,
        indent,
        meta = { // table of character substitutions
            '\b': '\\b',
            '\t': '\\t',
            '\n': '\\n',
            '\f': '\\f',
            '\r': '\\r',
            '"': '\\"',
            '\\': '\\\\'
        },
        rep;


    function quote(string) {

        // If the string contains no control characters, no quote characters, and no
        // backslash characters, then we can safely slap some quotes around it.
        // Otherwise we must also replace the offending characters with safe escape
        // sequences.

        escapable.lastIndex = 0;
        return escapable.test(string) ? '"' + string.replace(escapable, function(a) {
            var c = meta[a];
            return typeof c === 'string' ? c :
            '\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
        }) + '"' : '"' + string + '"';
    }


    function str(key, holder) {

        // Produce a string from holder[key].

        var i, // The loop counter.
            k, // The member key.
            v, // The member value.
            length,
            mind = gap,
            partial,
            value = holder[key];

        // If the value has a toJSON method, call it to obtain a replacement value.

        if (value instanceof Date) {
            value = date(key);
        }

        // If we were called with a replacer function, then call the replacer to
        // obtain a replacement value.

        if (typeof rep === 'function') {
            value = rep.call(holder, key, value);
        }

        // What happens next depends on the value's type.

        switch (typeof value) {
            case 'string':
                return quote(value);

            case 'number':

                // JSON numbers must be finite. Encode non-finite numbers as null.

                return isFinite(value) ? String(value) : 'null';

            case 'boolean':
            case 'null':

                // If the value is a boolean or null, convert it to a string. Note:
                // typeof null does not produce 'null'. The case is included here in
                // the remote chance that this gets fixed someday.

                return String(value);

            // If the type is 'object', we might be dealing with an object or an array or
            // null.

            case 'object':

                // Due to a specification blunder in ECMAScript, typeof null is 'object',
                // so watch out for that case.

                if (!value) {
                    return 'null';
                }

                // Make an array to hold the partial results of stringifying this object value.

                gap += indent;
                partial = [];

                // Is the value an array?

                if (Object.prototype.toString.apply(value) === '[object Array]') {

                    // The value is an array. Stringify every element. Use null as a placeholder
                    // for non-JSON values.

                    length = value.length;
                    for (i = 0; i < length; i += 1) {
                        partial[i] = str(i, value) || 'null';
                    }

                    // Join all of the elements together, separated with commas, and wrap them in
                    // brackets.

                    v = partial.length === 0 ? '[]' : gap ?
                    '[\n' + gap + partial.join(',\n' + gap) + '\n' + mind + ']' :
                    '[' + partial.join(',') + ']';
                    gap = mind;
                    return v;
                }

                // If the replacer is an array, use it to select the members to be stringified.

                if (rep && typeof rep === 'object') {
                    length = rep.length;
                    for (i = 0; i < length; i += 1) {
                        if (typeof rep[i] === 'string') {
                            k = rep[i];
                            v = str(k, value);
                            if (v) {
                                partial.push(quote(k) + (gap ? ': ' : ':') + v);
                            }
                        }
                    }
                } else {

                    // Otherwise, iterate through all of the keys in the object.

                    for (k in value) {
                        if (Object.prototype.hasOwnProperty.call(value, k)) {
                            v = str(k, value);
                            if (v) {
                                partial.push(quote(k) + (gap ? ': ' : ':') + v);
                            }
                        }
                    }
                }

                // Join all of the member texts together, separated with commas,
                // and wrap them in braces.

                v = partial.length === 0 ? '{}' : gap ?
                '{\n' + gap + partial.join(',\n' + gap) + '\n' + mind + '}' :
                '{' + partial.join(',') + '}';
                gap = mind;
                return v;
        }
    }

    // If the JSON object does not yet have a stringify method, give it one.

    JSON.stringify = function(value, replacer, space) {

        // The stringify method takes a value and an optional replacer, and an optional
        // space parameter, and returns a JSON text. The replacer can be a function
        // that can replace values, or an array of strings that will select the keys.
        // A default replacer method can be provided. Use of the space parameter can
        // produce text that is more easily readable.

        var i;
        gap = '';
        indent = '';

        // If the space parameter is a number, make an indent string containing that
        // many spaces.

        if (typeof space === 'number') {
            for (i = 0; i < space; i += 1) {
                indent += ' ';
            }

            // If the space parameter is a string, it will be used as the indent string.

        } else if (typeof space === 'string') {
            indent = space;
        }

        // If there is a replacer, it must be a function or an array.
        // Otherwise, throw an error.

        rep = replacer;
        if (replacer && typeof replacer !== 'function' &&
            (typeof replacer !== 'object' ||
            typeof replacer.length !== 'number')) {
            throw new Error('JSON.stringify');
        }

        // Make a fake root object containing our value under the key of ''.
        // Return the result of stringifying the value.

        return str('', {
            '': value
        });
    };

    // If the JSON object does not yet have a parse method, give it one.

    JSON.parse = function(text, reviver) {
        // The parse method takes a text and an optional reviver function, and returns
        // a JavaScript value if the text is a valid JSON text.

        var j;

        function walk(holder, key) {

            // The walk method is used to recursively walk the resulting structure so
            // that modifications can be made.

            var k, v, value = holder[key];
            if (value && typeof value === 'object') {
                for (k in value) {
                    if (Object.prototype.hasOwnProperty.call(value, k)) {
                        v = walk(value, k);
                        if (v !== undefined) {
                            value[k] = v;
                        } else {
                            delete value[k];
                        }
                    }
                }
            }
            return reviver.call(holder, key, value);
        }


        // Parsing happens in four stages. In the first stage, we replace certain
        // Unicode characters with escape sequences. JavaScript handles many characters
        // incorrectly, either silently deleting them, or treating them as line endings.

        text = String(text);
        cx.lastIndex = 0;
        if (cx.test(text)) {
            text = text.replace(cx, function(a) {
                return '\\u' +
                    ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
            });
        }

        // In the second stage, we run the text against regular expressions that look
        // for non-JSON patterns. We are especially concerned with '()' and 'new'
        // because they can cause invocation, and '=' because it can cause mutation.
        // But just to be safe, we want to reject all unexpected forms.

        // We split the second stage into 4 regexp operations in order to work around
        // crippling inefficiencies in IE's and Safari's regexp engines. First we
        // replace the JSON backslash pairs with '@' (a non-JSON character). Second, we
        // replace all simple value tokens with ']' characters. Third, we delete all
        // open brackets that follow a colon or comma or that begin the text. Finally,
        // we look to see that the remaining characters are only whitespace or ']' or
        // ',' or ':' or '{' or '}'. If that is so, then the text is safe for eval.

        if (/^[\],:{}\s]*$/
                .test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@')
                    .replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, ']')
                    .replace(/(?:^|:|,)(?:\s*\[)+/g, ''))) {

            // In the third stage we use the eval function to compile the text into a
            // JavaScript structure. The '{' operator is subject to a syntactic ambiguity
            // in JavaScript: it can begin a block or an object literal. We wrap the text
            // in parens to eliminate the ambiguity.

            j = eval('(' + text + ')');

            // In the optional fourth stage, we recursively walk the new structure, passing
            // each name/value pair to a reviver function for possible transformation.

            return typeof reviver === 'function' ?
                walk({
                    '': j
                }, '') : j;
        }

        // If the text is not JSON parseable, then a SyntaxError is thrown.

        throw new SyntaxError('JSON.parse');
    };

})(
    typeof JSON !== 'undefined' ? JSON : undefined
);