const CACHE_NAME = 'njiafix-cache-v1';
const urlsToCache = [
  '/',
  '/matengenezo/',
  '/static/logo.png',
  '/static/manifest.json'
];

// Wakati wa kusakinisha Service Worker na kuhifadhi rasilimali muhimu
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => {
        return cache.addAll(urlsToCache);
      })
  );
  self.skipWaiting();
});

// Kusafisha cache za zamani wakati app inapojisasisha
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheName !== CACHE_NAME) {
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
  self.clientsClaim();
});

// Kusikiliza maombi ya mtandao na kutoa huduma hata ukiwa offline
self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request)
      .then((response) => {
        // Kama ipo kwenye cache, irudishe; kama haipo, ichekechoe mtandaoni na kuiweka kwenye akiba
        return response || fetch(event.request).then((fetchResponse) => {
          return caches.open(CACHE_NAME).then((cache) => {
            cache.put(event.request, fetchResponse.clone());
            return fetchResponse;
          });
        }).catch(() => {
          // Kama hakuna mtandao na faili halipo kwenye cache, unaweza kurudisha ukurasa mkuu wa Offline
          if (event.request.mode === 'navigate') {
            return caches.match('/');
          }
        });
      })
  );
});
