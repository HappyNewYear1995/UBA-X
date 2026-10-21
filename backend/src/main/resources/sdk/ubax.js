/*!
 * ubax.js — UBA-X 浏览器行为采集 SDK
 * 通过应用控制台生成的嵌入片段加载:
 *   window.UBAX_CONFIG = { appKey, server, autoTrack };
 *   <script src="{server}/sdk/ubax.js" async></script>
 *
 * 采集: 页面浏览(pageview) / 元素点击(click) / 停留离开(leave) / 自定义(track)
 * 上报: 批量 POST {server}/api/collect/batch, 页面离开时用 sendBeacon 兜底。
 * 仅依赖浏览器标准 API, 无第三方依赖。
 */
(function (window, document) {
  'use strict';
  if (window.__ubaxLoaded) { return; }
  window.__ubaxLoaded = true;

  var CFG = window.UBAX_CONFIG || {};
  var APP_KEY = CFG.appKey || '';
  var SERVER = (CFG.server || '').replace(/\/+$/, '');
  var AUTO = CFG.autoTrack !== false;
  var FLUSH_MS = CFG.flushMs || 5000;
  var MAX_BATCH = CFG.batchSize || 30;
  var VERSION = '1.2.0';

  var LS_DISTINCT = 'ubax_distinct_id';
  var LS_SESSION = 'ubax_session';
  var LS_USER = 'ubax_user_id';

  function uuid() {
    if (window.crypto && crypto.randomUUID) { return crypto.randomUUID(); }
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      var r = (Math.random() * 16) | 0, v = c === 'x' ? r : (r & 0x3) | 0x8;
      return v.toString(16);
    });
  }
  function lsGet(k, def) { try { var v = localStorage.getItem(k); return v == null ? def : v; } catch (e) { return def; } }
  function lsSet(k, v) { try { localStorage.setItem(k, v); } catch (e) {} }

  var distinctId = lsGet(LS_DISTINCT, null);
  if (!distinctId) { distinctId = 'web-' + uuid(); lsSet(LS_DISTINCT, distinctId); }
  var sessionId = lsGet(LS_SESSION, null);
  // 会话 30 分钟过期续期
  var sessionTs = parseInt(lsGet(LS_SESSION + '_ts', '0'), 10) || 0;
  if (!sessionId || (Date.now() - sessionTs) > 30 * 60 * 1000) {
    sessionId = uuid(); lsSet(LS_SESSION, sessionId);
  }
  lsSet(LS_SESSION + '_ts', String(Date.now()));
  var userId = lsGet(LS_USER, null);

  var queue = [];
  var pageEnter = Date.now();
  var screen = (window.screen ? window.screen.width + 'x' + window.screen.height : '');

  function cssPath(el) {
    if (!(el instanceof Element)) { return ''; }
    var path = [];
    while (el && el.nodeType === Node.ELEMENT_NODE && path.length < 6) {
      var seg = el.tagName.toLowerCase();
      if (el.id) { path.unshift(seg + '#' + el.id); break; }
      var cls = (el.className && typeof el.className === 'string')
        ? el.className.trim().split(/\s+/).slice(0, 2).join('.') : '';
      if (cls) { seg += '.' + cls; }
      path.unshift(seg);
      el = el.parentNode;
    }
    return path.join(' > ');
  }
  function text(el) {
    var t = (el.getAttribute && (el.getAttribute('title') || el.getAttribute('aria-label'))) ||
            (el.innerText || el.value || '');
    return String(t).trim().slice(0, 60);
  }

  function push(evt) {
    evt.appKey = APP_KEY;
    evt.distinctId = distinctId;
    evt.sessionId = sessionId;
    if (userId) { evt.userId = userId; }
    evt.ts = Date.now();
    evt.url = location.href;
    evt.path = location.pathname;
    evt.title = document.title;
    evt.referrer = document.referrer || '';
    evt.screen = screen;
    queue.push(evt);
    if (queue.length >= MAX_BATCH) { flush(); }
  }

  function payload() { return { appKey: APP_KEY, events: queue }; }

  function flush(useBeacon) {
    if (!queue.length || !APP_KEY) { return; }
    var body = JSON.stringify(payload());
    var events = queue; queue = [];
    if (useBeacon && navigator.sendBeacon) {
      try {
        var blob = new Blob([body], { type: 'application/json' });
        if (navigator.sendBeacon(SERVER + '/api/collect/beacon', blob)) { return; }
      } catch (e) { /* fall through */ }
      queue = events.concat(queue); // beacon 失败, 放回队列
    }
    try {
      var xhr = new XMLHttpRequest();
      xhr.open('POST', SERVER + '/api/collect/batch', true);
      xhr.setRequestHeader('Content-Type', 'application/json');
      xhr.send(body);
    } catch (e) { /* ignore network errors */ }
  }

  // ---- 自动采集 ----
  function trackPageview() {
    push({ type: 'pageview', event: '$pageview' });
  }
  function trackClick(e) {
    var el = e.target;
    // 向上找可交互元素
    var hops = 0;
    while (el && hops < 5 && !(el.tagName && /^(A|BUTTON|INPUT|SELECT|TEXTAREA)$/.test(el.tagName))
           && !(el.getAttribute && el.getAttribute('data-ubax-track'))) {
      el = el.parentNode; hops++;
    }
    if (!el || !el.tagName) { return; }
    // Capture page geometry so clicks can be re-projected onto the full document
    // (clientX/Y are viewport-relative; without scroll + doc size a scrolled click
    // cannot be placed on a real full-page snapshot).
    var de = document.documentElement, bd = document.body;
    var sx = window.scrollX || window.pageXOffset || (de && de.scrollLeft) || 0;
    var sy = window.scrollY || window.pageYOffset || (de && de.scrollTop) || 0;
    var vpW = window.innerWidth || (de && de.clientWidth) || 0;
    var vpH = window.innerHeight || (de && de.clientHeight) || 0;
    var docW = Math.max(de ? de.scrollWidth : 0, bd ? bd.scrollWidth : 0, vpW);
    var docH = Math.max(de ? de.scrollHeight : 0, bd ? bd.scrollHeight : 0, vpH);
    push({
      type: 'click', event: '$click',
      x: e.clientX, y: e.clientY,
      scrollX: Math.round(sx), scrollY: Math.round(sy),
      vpW: Math.round(vpW), vpH: Math.round(vpH),
      docW: Math.round(docW), docH: Math.round(docH),
      element: (el.getAttribute && el.getAttribute('data-ubax-track')) || cssPath(el),
      properties: { text: text(el), tag: el.tagName.toLowerCase() }
    });
  }
  function trackLeave() {
    var dur = Math.max(0, Math.round((Date.now() - pageEnter) / 1000));
    push({ type: 'leave', event: '$leave', duration: dur * 1000, properties: { seconds: dur } });
    flush(true);
  }

  // ---- 公共 API ----
  // 上报身份绑定 (distinctId -> userId) 到服务端, 使登录前后事件归并到同一画像。
  // 失败静默, 不阻塞; 优先 sendBeacon, 回退 XHR。
  function reportAlias(uid) {
    if (!SERVER || !APP_KEY || !uid) { return; }
    var body = JSON.stringify({ appKey: APP_KEY, distinctId: distinctId, userId: String(uid) });
    try {
      if (navigator.sendBeacon) {
        var blob = new Blob([body], { type: 'application/json' });
        if (navigator.sendBeacon(SERVER + '/api/collect/identify', blob)) { return; }
      }
    } catch (e) {}
    try {
      var xhr = new XMLHttpRequest();
      xhr.open('POST', SERVER + '/api/collect/identify', true);
      xhr.setRequestHeader('Content-Type', 'application/json');
      xhr.send(body);
    } catch (e) {}
  }

  var ubax = {
    version: VERSION,
    track: function (event, props) { push({ type: 'custom', event: event || '$event', properties: props || {} }); },
    pageview: function () { trackPageview(); },
    identify: function (id) {
      if (!id) { return; }
      userId = String(id); lsSet(LS_USER, userId);
      reportAlias(userId);
    },
    reset: function () {
      distinctId = 'web-' + uuid(); lsSet(LS_DISTINCT, distinctId);
      sessionId = uuid(); lsSet(LS_SESSION, sessionId); lsSet(LS_SESSION + '_ts', String(Date.now()));
      userId = null; try { localStorage.removeItem(LS_USER); } catch (e) {}
    },
    flush: function () { flush(false); },
    getDistinctId: function () { return distinctId; },
    getSessionId: function () { return sessionId; }
  };
  window.ubax = ubax;

  if (AUTO) {
    trackPageview();
    document.addEventListener('click', trackClick, true);
    // SPA 路由变化 (History API)
    var lastPath = location.pathname;
    var _pushState = history.pushState;
    if (typeof _pushState === 'function') {
      history.pushState = function () {
        _pushState.apply(this, arguments);
        if (location.pathname !== lastPath) { lastPath = location.pathname; pageEnter = Date.now(); trackPageview(); }
      };
      window.addEventListener('popstate', function () {
        if (location.pathname !== lastPath) { lastPath = location.pathname; pageEnter = Date.now(); trackPageview(); }
      });
    }
    window.addEventListener('pagehide', trackLeave);
    document.addEventListener('visibilitychange', function () { if (document.visibilityState === 'hidden') { trackLeave(); } });
  }

  setInterval(function () { flush(false); }, FLUSH_MS);
  window.addEventListener('beforeunload', function () { flush(true); });
})(window, document);
