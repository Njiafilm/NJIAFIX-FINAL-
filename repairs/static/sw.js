const CACHE_NAME = 'njiafix-cache-v1';
const urlsToCache = [
  '/',
  '/matengenezo/',
  '/static/css/style.css', // Badilisha kama una faili maalum la CSS
  '/static/js/main.js',   // Badilisha kama una faili maalum la JS
];

// Wakati wa kusakinisha Service Worker
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => {
        return cache.addAll(urlsToCache);
      })
  );
});

// Kusikiliza maombi ya mtandao na kutoa huduma hataukiwa offline
self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request)
      .then((response) => {
        // Kama ipo kwenye cache, irudishe; kama haipo, ichekechoe mtandaoni
        return response || fetch(event.request);
      })
  );
});
