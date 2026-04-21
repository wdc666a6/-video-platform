// Service Worker - 封面图片缓存
const CACHE_NAME = 'video-covers-v1';
const CACHE_DURATION = 7 * 24 * 60 * 60 * 1000; // 7天缓存

// 需要缓存的URL模式（图片代理接口）
const CACHE_URL_PATTERN = /\/api\/image\/proxy\?url=.+/;

// 安装事件
self.addEventListener('install', event => {
    console.log('[SW] Service Worker 安装');
    self.skipWaiting(); // 立即激活新的SW
});

// 激活事件 - 清理旧缓存
self.addEventListener('activate', event => {
    console.log('[SW] Service Worker 激活');
    event.waitUntil(
        caches.keys().then(cacheNames => {
            return Promise.all(
                cacheNames
                    .filter(name => name.startsWith('video-') && name !== CACHE_NAME)
                    .map(name => caches.delete(name))
            );
        })
    );
    self.clients.claim(); // 立即控制所有页面
});

// 拦截网络请求
self.addEventListener('fetch', event => {
    const url = new URL(event.request.url);

    // 只缓存图片代理请求
    if (url.pathname === '/api/image/proxy' && url.searchParams.has('url')) {
        event.respondWith(
            caches.open(CACHE_NAME).then(cache => {
                // 1. 先查找缓存
                return cache.match(event.request).then(cachedResponse => {
                    if (cachedResponse) {
                        // 检查缓存是否过期
                        const cacheTime = cachedResponse.headers.get('sw-cache-time');
                        if (cacheTime && (Date.now() - parseInt(cacheTime)) < CACHE_DURATION) {
                            console.log('[SW] 缓存命中:', url.searchParams.get('url'));
                            return cachedResponse;
                        } else {
                            // 缓存过期，删除旧缓存
                            cache.delete(event.request);
                        }
                    }

                    // 2. 缓存未命中或已过期，请求网络
                    console.log('[SW] 请求网络:', url.searchParams.get('url'));
                    return fetch(event.request).then(networkResponse => {
                        // 只缓存成功的响应
                        if (networkResponse.ok && networkResponse.status === 200) {
                            // 克隆响应（因为响应流只能使用一次）
                            const responseToCache = networkResponse.clone();

                            // 添加缓存时间戳
                            const headers = new Headers(networkResponse.headers);
                            headers.append('sw-cache-time', Date.now().toString());

                            // 存入缓存
                            cache.put(event.request, responseToCache);
                        }
                        return networkResponse;
                    }).catch(error => {
                        console.error('[SW] 网络请求失败:', error);
                        // 网络失败时，返回过期的缓存（如果有）
                        return cachedResponse || new Response('Network error', { status: 503 });
                    });
                });
            })
        );
    }
});
