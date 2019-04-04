/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 heimuheimu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.heimuheimu.naiveredis.cluster;

import com.heimuheimu.naivemonitor.monitor.ThreadPoolMonitor;
import com.heimuheimu.naiveredis.DirectRedisClient;
import com.heimuheimu.naiveredis.monitor.ThreadPoolMonitorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Redis multi-get 命令执行器，用于同时执行多台 Redis 服务的 multi-get 命令。
 *
 * <p><strong>说明：</strong>{@code MultiGetExecutor} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
class MultiGetExecutor implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(MultiGetExecutor.class);

    private final ThreadPoolExecutor executorService;

    private final ThreadPoolMonitor threadPoolMonitor;

    MultiGetExecutor() {
        executorService = new ThreadPoolExecutor(0, 200,
                60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new NamedThreadFactory());
        threadPoolMonitor = ThreadPoolMonitorFactory.get();
        threadPoolMonitor.register(executorService);
    }

    @SuppressWarnings("unchecked")
    <T> Future<Map<String, T>> submit(DirectRedisClient client, Set<String> keySet, boolean isGetCount) {
        try {
            return executorService.submit(new MultiGetTask(client, keySet, isGetCount));
        } catch (RejectedExecutionException e) {
            LOG.error("Redis Multi-Get failed: `thread pool is too busy`. `host`:`" + client.getHost() + "`. `isGetCount`: `"
                    + isGetCount + "`. `keySet`: `" + keySet + "`.", e);
            threadPoolMonitor.onRejected();
            return null;
        }
    }

    @Override
    public void close() {
        executorService.shutdown();
    }

    private static class MultiGetTask<T> implements Callable<Map<String, T>> {

        private final DirectRedisClient client;

        private final Set<String> keySet;

        private final boolean isGetCount;

        private MultiGetTask(DirectRedisClient client, Set<String> keySet, boolean isGetCount) {
            this.client = client;
            this.keySet = keySet;
            this.isGetCount = isGetCount;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<String, T> call() {
            if (!keySet.isEmpty()) {
                if (isGetCount) {
                    return (Map<String, T>) client.multiGetCount(keySet);
                } else {
                    return client.multiGet(keySet);
                }
            } else {
                return new HashMap<>();
            }
        }

    }

    private static class NamedThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("naiveredis-multi-get-" + threadNumber.getAndIncrement());
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }

}
