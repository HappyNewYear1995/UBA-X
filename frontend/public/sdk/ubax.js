/*!
 * UBA-X Web Collection SDK (ubax.js)
 * Auto-tracks pageviews, clicks (with coordinates for heatmaps), page leave
 * (dwell time) and exposes ubax.track()/identify() for custom events.
 * Config via window.UBAX_CONFIG = { appKey, server, autoTrack }.
 */
(function (global) {
  'use strict';
  var cfg = global.UBAX_CONFIG || {};
  var SERVER = (cfg.server || '').replace(/\/$/, '');
  var APP_KEY = cfg.appKey || '';
  var AUTO = cfg.autoTrack !== false;
  var BATCH_URL = SERVER + '/api/collect/batch';
  var MAX_QUEUE = 30;
  var FLUSH_MS = 5000;

  function uuid() {
    return 'xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
      var r = (Math.random() * 16) | 0, v = c === 'x' ? r : (r & 0x3) | 0x8;
      return v.toString(16);
    });
  }
  function store(key, val) {
    try {
      if (val === undefined) return localStorage.getItem(key);
      localStorage.setItem(key, val); return val;
    } catch (e) { return val; }
  }
  function sessStore(key, val) {
    try {
      if (val === undefined) return sessionStorage.getItem(key);
      sessionStorage.setItem(key, val); return val;
    } catch (e) { return val; }
  }

  var distinctId = store('__ubax_did') || store('__ubax_did', uuid());
  var sessionId = sessStore('__ubax_sid') || sessStore('__ubax_sid', uuid().slice(0, 16));
  var userId = store('__ubax_uid') || null;
  var enterTime = Date.now();
  var queue = [];
  var flushing = false;

  function geo() {
    return { country: '中国' };
  }

  function baseProps() {
    return {
      screen: (global.screen ? global.screen.width + 'x' + global.screen.height : ''),
      lang: navigator.language || ''
    };
  }

  function push(payload) {
    payload.appKey = APP_KEY;
    payload.distinctId = distinctId;
    if (userId) payload.userId = userId;
    payload.sessionId = sessionId;
    payload.ts = Date.now();
    var g = geo(); payload.country = g.country;
    queue.push(payload);
    if (queue.length >= MAX_QUEUE) flush();
  }

  function send(events) {
    var body = JSON.stringify({ appKey: APP_KEY, events: events });
    try {
      if (navigator.sendBeacon && events.length > 1) {
        navigator.sendBeacon(BATCH_URL, new Blob([body], { type: 'application/json' }));
        return;
      }
    } catch (e) {}
    try {
      fetch(BATCH_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: body,
        keepalive: true
      }).catch(function () {});
    } catch (e) {}
  }

  function flush() {
    if (!queue.length || flushing) return;
    flushing = true;
    var events = queue.splice(0, queue.length);
    flushing = false;
    send(events);
  }

  function pageProps() {
    return {
      url: location.href,
      path: location.pathname,
      title: document.title,
      referrer: document.referrer || ''
    };
  }

  function trackPageview() {
    var p = pageProps();
    push(Object.assign({ type: 'pageview', event: '$pageview' }, p, { properties: baseProps() }));
  }

  function selectorOf(el) {
    if (!el || el === document.body || el === document.documentElement) return 'body';
    if (el.id) return '#' + el.id;
    var s = el.tagName ? el.tagName.toLowerCase() : 'node';
    if (el.className && typeof el.className === 'string') {
      var cls = el.className.trim().split(/\s+/).slice(0, 2).join('.');
      if (cls) s += '.' + cls;
    }
    return s;
  }

  function bindClicks() {
    document.addEventListener('click', function (ev) {
      var el = ev.target;
      var p = pageProps();
      push(Object.assign({
        type: 'click',
        event: '$click',
        x: ev.clientX,
        y: ev.clientY + (global.scrollY || 0),
        element: selectorOf(el),
        properties: { text: (el.innerText || el.value || '').slice(0, 40) }
      }, p));
    }, true);
  }

  function bindLeave() {
    function leave() {
      var p = pageProps();
      var payload = Object.assign({ type: 'leave', event: '$leave', duration: Date.now() - enterTime }, p);
      payload.appKey = APP_KEY; payload.distinctId = distinctId;
      payload.sessionId = sessionId; payload.ts = Date.now();
      if (userId) payload.userId = userId;
      send([payload]);
    }
    global.addEventListener('beforeunload', leave);
    document.addEventListener('visibilitychange', function () {
      if (document.visibilityState === 'hidden') leave();
    });
  }

  function bindHistory() {
    var last = location.pathname;
    function check() {
      if (location.pathname !== last) { last = location.pathname; enterTime = Date.now(); trackPageview(); }
    }
    global.addEventListener('popstate', check);
    var ps = history.pushState;
    history.pushState = function () { ps.apply(this, arguments); setTimeout(check, 0); };
  }

  var ubax = {
    track: function (event, props) {
      var p = pageProps();
      push(Object.assign({ type: 'event', event: event, properties: props || {} }, p));
      flush();
    },
    identify: function (uid) {
      userId = uid; store('__ubax_uid', uid);
    },
    flush: flush,
    config: { server: SERVER, appKey: APP_KEY, autoTrack: AUTO }
  };

  global.ubax = ubax;

  function init() {
    if (!APP_KEY) { console.warn('[UBA-X] missing appKey'); return; }
    if (AUTO) {
      trackPageview();
      bindClicks();
      bindLeave();
      bindHistory();
    }
    setInterval(flush, FLUSH_MS);
  }

  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
  else init();
})(typeof window !== 'undefined' ? window : this);
