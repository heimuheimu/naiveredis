package com.heimuheimu.naiveredis.cluster;

import com.heimuheimu.naivemonitor.monitor.ThreadPoolMonitor;
import com.heimuheimu.naiveredis.DirectRedisClient;
import com.heimuheimu.naiveredis.monitor.ThreadPoolMonitorFactory;

import java.io.Closeable;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 使用线程池异步执行 Redis 方法。
 *
 * <p><strong>说明：</strong>{@code RedisMethodAsyncExecutor} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
class RedisMethodAsyncExecutor implements Closeable {

    private final ThreadPoolExecutor executorService;

    private final ThreadPoolMonitor threadPoolMonitor;

    RedisMethodAsyncExecutor() {
        executorService = new ThreadPoolExecutor(0, 200,
                60L, TimeUnit.SECONDS, new SynchronousQueue<>(), new NamedThreadFactory());
        threadPoolMonitor = ThreadPoolMonitorFactory.get();
        threadPoolMonitor.register(executorService);
    }

    @Override
    public void close() {
        executorService.shutdown();
    }

    /**
     * 提交一个异步执行 Redis 方法的任务。
     *
     * @param client 执行该 Redis 方法使用的 Redis 直连客户端，不允许为 {@code null}
     * @param delegate Redis 方法执行代理，不允许为 {@code null}
     * @param <T> Redis 方法返回值，可能为 {@code null}
     * @return Future 实例
     * @throws RejectedExecutionException 如果线程池繁忙，将会抛出此异常
     */
    @SuppressWarnings("unchecked")
    <T> Future<T> submit(DirectRedisClient client, RedisMethodDelegate delegate) throws RejectedExecutionException {
        try {
            return (Future<T>) executorService.submit(new RedisMethodExecuteTask(client, delegate));
        } catch (RejectedExecutionException e) {
            threadPoolMonitor.onRejected();
            throw e;
        }
    }

    public static class RedisMethodExecuteTask implements Callable<Object> {

        private final DirectRedisClient client;

        private final RedisMethodDelegate delegate;

        public RedisMethodExecuteTask(DirectRedisClient client, RedisMethodDelegate delegate) {
            this.client = client;
            this.delegate = delegate;
        }

        @Override
        public Object call() {
            return delegate.delegate(client);
        }
    }

    private static class NamedThreadFactory implements ThreadFactory {

        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName("naiveredis-method-async-executor-" + threadNumber.getAndIncrement());
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
