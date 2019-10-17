/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 heimuheimu
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

import com.heimuheimu.naiveredis.DirectRedisClient;
import com.heimuheimu.naiveredis.cluster.standard.StandardRedisNodeRouter;
import com.heimuheimu.naiveredis.constant.BeanStatusEnum;
import com.heimuheimu.naiveredis.constant.RedisClientMethod;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.facility.ASKRedirectionHelper;
import com.heimuheimu.naiveredis.facility.clients.DirectRedisClientListListener;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Supplier;

/**
 * 用于连接标准的 Redis 集群客户端，Redis 集群相关资料可参考以下内容：
 * <ul>
 *     <li><a href="https://redis.io/topics/cluster-tutorial">Redis cluster tutorial</a></li>
 *     <li><a href="https://redis.io/topics/cluster-spec">Redis Cluster Specification</a></li>
 * </ul>
 *
 * <p><strong>说明：</strong>{@code StandardRedisClusterClient} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class StandardRedisClusterClient extends AbstractRedisClusterClient {

    private static final Logger REDIS_CONNECTION_LOG = LoggerFactory.getLogger("NAIVEREDIS_CONNECTION_LOG");

    /**
     * 创建 Redis 直连客户端所使用的 Socket 配置信息
     */
    private final SocketConfiguration configuration;

    /**
     * Redis 操作超时时间，单位：毫秒
     */
    private final int timeout;

    /**
     * 最小压缩字节数，当 Value 字节数小于或等于该值，不进行压缩
     */
    private final int compressionThreshold;

    /**
     * Redis 操作过慢最小时间，单位：毫秒
     */
    private final int slowExecutionThreshold;

    /**
     * PING 命令发送时间间隔，单位：秒，用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     */
    private final int pingPeriod;

    /**
     * Redis 直连客户端列表事件监听器
     */
    private final DirectRedisClientListListener listener;

    /**
     * 是否启用 Redis 集群中的 Slave 节点，用于执行已读操作
     */
    private final boolean isSlaveActive;

    /**
     * 重载 Redis 集群客户端路由器任务使用的私有锁
     */
    private final Object reloadRouterTaskLock = new Object();

    /**
     * 重载 Redis 集群客户端路由器任务是否运行，访问或设置此变量需获取锁 {@link #reloadRouterTaskLock}
     */
    private boolean isReloadRouterTaskRunning = false;

    /**
     * Redis 集群客户端路由器
     */
    private volatile StandardRedisNodeRouter standardRedisNodeRouter;

    /**
     * Redis 集群客户端所处状态
     */
    private volatile BeanStatusEnum state = BeanStatusEnum.NORMAL;

    /**
     * 构造一个 StandardRedisClusterClient 实例，创建直连客户端的 {@link java.net.Socket} 配置信息使用
     * {@link SocketConfiguration#DEFAULT}，Redis 操作超时时间设置为 5 秒，最小压缩字节数设置为 64 KB，
     * Redis 操作过慢最小时间设置为 50 毫秒，心跳检测时间设置为 30 秒，不启用 Slave 节点。
     *
     * @param bootstrapHosts Redis 集群启动主机地址列表，用于加载集群信息，不允许为 {@code null} 或空数组
     * @param listener Redis 直连客户端列表事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果集群中的所有 Redis 直连客户端均不可用，将会抛出此异常
     */
    public StandardRedisClusterClient(String[] bootstrapHosts, DirectRedisClientListListener listener) throws IllegalStateException {
        this(bootstrapHosts, null, 5000, 64 * 1024, 50, 30, listener, false);
    }

    /**
     * 构造一个 StandardRedisClusterClient 实例。
     *
     * @param bootstrapHosts Redis 集群启动主机地址列表，用于加载集群信息，不允许为 {@code null} 或空数组
     * @param configuration 创建 Redis 直连客户端所使用的 Socket 配置信息，允许为 {@code null}
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param compressionThreshold 最小压缩字节数，当 Value 字节数小于或等于该值，不进行压缩，不能小于等于 0
     * @param slowExecutionThreshold Redis 操作过慢最小时间，单位：毫秒，不能小于等于 0
     * @param pingPeriod PING 命令发送时间间隔，单位：秒，用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param listener Redis 直连客户端列表事件监听器，允许为 {@code null}
     * @param isSlaveActive 是否启用 Redis 集群中的 Slave 节点，用于执行已读操作
     * @throws IllegalStateException 如果集群中的所有 Redis 直连客户端均不可用，将会抛出此异常
     */
    public StandardRedisClusterClient(String[] bootstrapHosts, SocketConfiguration configuration, int timeout,
                                      int compressionThreshold, int slowExecutionThreshold, int pingPeriod,
                                      DirectRedisClientListListener listener, boolean isSlaveActive) throws IllegalStateException {
        this.configuration = configuration;
        this.timeout = timeout;
        this.compressionThreshold = compressionThreshold;
        this.slowExecutionThreshold = slowExecutionThreshold;
        this.pingPeriod = pingPeriod;
        this.listener = listener;
        this.isSlaveActive = isSlaveActive;
        this.standardRedisNodeRouter = new StandardRedisNodeRouter(bootstrapHosts, configuration, timeout,
                compressionThreshold, slowExecutionThreshold, pingPeriod, listener, isSlaveActive);
    }

    @Override
    public synchronized void close() {
        if (state != BeanStatusEnum.CLOSED) {
            state = BeanStatusEnum.CLOSED;
            try {
                super.close();
            } catch (Exception ignored) {}
            this.standardRedisNodeRouter.close();
        }
    }

    @Override
    protected DirectRedisClient getClient(RedisClientMethod method, String key) {
        int slot = standardRedisNodeRouter.getSlot(key);
        return standardRedisNodeRouter.getClientBySlot(slot, isSlaveActive && method.isReadOnly());
    }

    private DirectRedisClient getRedirectionClient(RedirectionErrorMessage redirectionErrorMessage, RedisClientMethod method,
                                                   Supplier<Map<String, Object>> errorParameterMapSupplier,
                                                   boolean isThrowException) throws IllegalStateException {
        if (redirectionErrorMessage.type == RedirectionType.MOVED) {
            standardRedisNodeRouter.moved(redirectionErrorMessage.slot, redirectionErrorMessage.host);
            startReloadRouterTask();
        }
        DirectRedisClient redirectionClient = null;
        Exception redirectionClientException = null;
        try {
            redirectionClient = standardRedisNodeRouter.getClientByHost(redirectionErrorMessage.host);
        } catch (Exception e) {
            redirectionClientException = e;
        }
        if (redirectionClient == null || !redirectionClient.isAvailable()) {
            clusterMonitor.onUnavailable();
            Map<String, Object> errorParameterMap = errorParameterMapSupplier.get();
            errorParameterMap.put("redirectionErrorMessage", redirectionErrorMessage);
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog(getClass().getSimpleName() + method.getMethodName(),
                    "redirection DirectRedisClient is not available", errorParameterMap);
            NAIVEREDIS_ERROR_LOG.error(errorMessage);
            if (redirectionClientException == null) {
                LOG.error(errorMessage);
                if (isThrowException) {
                    throw new IllegalStateException(errorMessage);
                }
            } else {
                LOG.error(errorMessage, redirectionClientException);
                if (isThrowException) {
                    throw new IllegalStateException(errorMessage, redirectionClientException);
                }
            }
            return null;
        }
        return redirectionClient;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> T execute(RedisClientMethod method, String key, RedisMethodDelegate delegate) {
        checkKey(method, key);
        DirectRedisClient client = getAvailableClient(method, key);
        try {
            return (T) delegate.delegate(client);
        } catch (Exception e) {
            RedirectionErrorMessage redirectionErrorMessage = null;
            if (e instanceof RedisException) {
                redirectionErrorMessage = parseRedirectionErrorMessage(method, e.getMessage());
            }
            if (redirectionErrorMessage != null) {
                DirectRedisClient redirectionClient = getRedirectionClient(redirectionErrorMessage, method,
                        () -> {
                            Map<String, Object> errorParameterMap = new LinkedHashMap<>();
                            errorParameterMap.put("key", key);
                            return errorParameterMap;
                        }, true);
                if (redirectionErrorMessage.type == RedirectionType.ASK) {
                    try {
                        ASKRedirectionHelper.onASKRedirection();
                        return (T) delegate.delegate(redirectionClient);
                    } finally {
                        ASKRedirectionHelper.removeASKRedirection();
                    }
                } else {
                    return (T) delegate.delegate(redirectionClient);
                }
            } else {
                throw e;
            }
        }
    }

    @Override
    protected <T> Map<String, T> internalMultiGet(RedisClientMethod method, Set<String> keySet) throws IllegalArgumentException {
        Map<String, T> result = new HashMap<>();
        if (keySet == null || keySet.isEmpty()) {
            return result;
        }
        Map<Integer, Set<String>> slotKeyMap = new HashMap<>();
        for (String key : keySet) {
            try {
                checkKey(method, key);
            } catch (IllegalArgumentException e) {
                onMultiGetFailed(method, keySet, "keySet could not contain empty key", e);
                throw e;
            }
            int slot = standardRedisNodeRouter.getSlot(key);
            Set<String> slotKeySet = slotKeyMap.computeIfAbsent(slot, k -> new HashSet<>());
            slotKeySet.add(key);
        }
        Map<Future<Map<String, T>>, Set<String>> futureMap = new HashMap<>();
        for (int slot : slotKeyMap.keySet()) {
            Set<String> subKeySet = slotKeyMap.get(slot);
            DirectRedisClient client = null;
            try {
                client = getAvailableClient(method, subKeySet.iterator().next());
            } catch (Exception e) {
                onMultiGetFailed(method, subKeySet, "DirectRedisClient is not available", e);
            }
            if (client != null) {
                try {
                    RedisMethodDelegate delegate = buildMultiGetDelegate(method, subKeySet);
                    futureMap.put(asyncExecutor.submit(client, delegate), subKeySet);
                } catch (RejectedExecutionException e) {
                    onMultiGetFailed(method, subKeySet, "thread pool is too busy", e);
                }
            }
        }

        Map<Future<Map<String, T>>, Set<String>> retryFutureMap = new HashMap<>();
        for (Future<Map<String, T>> future : futureMap.keySet()) {
            try {
                result.putAll(future.get());
            } catch (Exception e) {
                RedirectionErrorMessage redirectionErrorMessage = null;
                if (e instanceof ExecutionException && e.getCause() instanceof RedisException) {
                    redirectionErrorMessage = parseRedirectionErrorMessage(method, e.getCause().getMessage());
                }
                if (redirectionErrorMessage != null) {
                    Set<String> subKeySet = futureMap.get(future);
                    DirectRedisClient redirectionClient = getRedirectionClient(redirectionErrorMessage, method,
                            () -> {
                                Map<String, Object> errorParameterMap = new LinkedHashMap<>();
                                errorParameterMap.put("keySet", subKeySet);
                                return errorParameterMap;
                            }, false);
                    if (redirectionClient != null) {
                        try {
                            RedisMethodDelegate delegate = buildMultiGetDelegate(method, subKeySet);
                            if (redirectionErrorMessage.type == RedirectionType.ASK) {
                                retryFutureMap.put(asyncExecutor.submit(redirectionClient, delegate, true), subKeySet);
                            } else {
                                retryFutureMap.put(asyncExecutor.submit(redirectionClient, delegate), subKeySet);
                            }
                        } catch (RejectedExecutionException rejectedExecutionException) {
                            onMultiGetFailed(method, subKeySet, "thread pool is too busy", rejectedExecutionException);
                        }
                    } else {
                        clusterMonitor.onMultiGetError();
                    }
                } else {
                    onMultiGetFailed(method, futureMap.get(future), "unexpected error", e);
                }
            }
        }
        mergeMultiGetResult(method, result, retryFutureMap);
        return result;
    }

    /**
     * 解析 Redis 集群返回的重定向错误信息，如果错误信息不属于重定向错误或解析出错，将会返回 {@code null}，该方法不会抛出异常。
     *
     * @param method 发生 Redis 错误的 Redis 方法
     * @param errorMessage Redis 错误信息
     * @return Redis 集群重定向信息，可能为 {@code null}
     */
    private RedirectionErrorMessage parseRedirectionErrorMessage(RedisClientMethod method, String errorMessage) {
        if (errorMessage != null) {
            RedirectionType type = null;
            if (errorMessage.startsWith("MOVED ")) {
                type = RedirectionType.MOVED;
            } else if (errorMessage.startsWith("ASK ")) {
                type = RedirectionType.ASK;
            }
            if (type != null) {
                try {
                    String[] parts = errorMessage.split(" ");
                    int slot = Integer.parseInt(parts[1]);
                    return new RedirectionErrorMessage(type, slot, parts[2]);
                } catch (Exception e) { // should not happen, just for bug detection
                    Map<String, Object> errorParameterMap = new LinkedHashMap<>();
                    errorParameterMap.put("method", method);
                    errorParameterMap.put("errorMessage", errorMessage);
                    String text = LogBuildUtil.buildMethodExecuteFailedLog("StandardRedisClusterClient#parseRedirectionErrorMessage(RedisClientMethod method, String key, String errorMessage)",
                            "unexpected error", errorParameterMap);
                    LOG.error(text, e);
                }
            }
        }
        return null;
    }

    /**
     * Redis 集群重定向类型。
     */
    private enum RedirectionType {

        /**
         * 永久重定向
         */
        MOVED,

        /**
         * 单次请求重定向
         */
        ASK
    }

    /**
     * Redis 集群重定向信息。
     */
    private static class RedirectionErrorMessage {

        /**
         * Redis 集群重定向类型
         */
        private final RedirectionType type;

        /**
         * 需要重定向的 slot 值
         */
        private final int slot;

        /**
         * 重定向的目标主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
         */
        private final String host;

        /**
         * 构造一个 RedirectionErrorMessage 实例。
         *
         * @param type Redis 集群重定向类型
         * @param slot 需要重定向的 slot 值
         * @param host 重定向的目标主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
         */
        private RedirectionErrorMessage(RedirectionType type, int slot, String host) {
            this.type = type;
            this.slot = slot;
            this.host = host;
        }

        @Override
        public String toString() {
            return "RedirectionErrorMessage{" +
                    "type=" + type +
                    ", slot=" + slot +
                    ", host='" + host + '\'' +
                    '}';
        }
    }

    /**
     * 当发生 MOVED 错误时，启动重载 Redis 集群客户端路由器任务，重新加载 slot 和 Redis 主机地址映射关系。
     */
    private void startReloadRouterTask() {
        if (state == BeanStatusEnum.NORMAL) {
            synchronized (reloadRouterTaskLock) {
                if (!isReloadRouterTaskRunning) {
                    Thread reloadThread = new Thread(() -> {
                        try {
                            try {
                                Thread.sleep(2000 + new Random().nextInt(3000)); // 随机延迟 2-5 秒后开始重新加载
                            } catch (InterruptedException ignored) {}
                            long startTime = System.currentTimeMillis();
                            REDIS_CONNECTION_LOG.info("StandardRedisNodeRouter reload task has been started. `currentNodes`:`{}`.", standardRedisNodeRouter.getNodes());
                            while (state == BeanStatusEnum.NORMAL) {
                                StandardRedisNodeRouter newRouter = null;
                                try {
                                    newRouter = new StandardRedisNodeRouter(standardRedisNodeRouter.getHosts(),
                                            configuration, timeout, compressionThreshold, slowExecutionThreshold, pingPeriod, listener, isSlaveActive);
                                } catch (Exception e) {
                                    REDIS_CONNECTION_LOG.error("Reload StandardRedisNodeRouter failed. `currentNodes`:`"
                                            + standardRedisNodeRouter.getNodes() + "`.", e);
                                }
                                if (newRouter != null) {
                                    StandardRedisNodeRouter oldRouter = this.standardRedisNodeRouter;
                                    this.standardRedisNodeRouter = newRouter;
                                    REDIS_CONNECTION_LOG.info("StandardRedisNodeRouter reload task has been finished. `cost`:`{}ms`. `currentNodes`:`{}`. `previousNodes`:`{}`.",
                                            System.currentTimeMillis() - startTime, standardRedisNodeRouter.getNodes(), oldRouter.getNodes()); // lgtm [java/print-array]
                                    Thread closeThread = new Thread(() -> { // (3 + timeout) 秒后关闭已弃用的 Redis 集群客户端路由器
                                        try {
                                            Thread.sleep(3000 + oldRouter.getTimeout());
                                        } catch (InterruptedException ignored) {
                                        }
                                        oldRouter.close();
                                    });
                                    closeThread.setName("naiveredis-cluster-router-close-task");
                                    closeThread.setDaemon(true);
                                    closeThread.start();
                                    break;
                                } else {
                                    try {
                                        Thread.sleep(1000); // 新 Router 创建失败，等待 1 秒后重试
                                    } catch (InterruptedException ignored) {
                                    }
                                }
                            }
                        } finally {
                            synchronized (reloadRouterTaskLock) {
                                isReloadRouterTaskRunning = false;
                            }
                        }
                    });
                    reloadThread.setName("naiveredis-cluster-router-reload-task");
                    reloadThread.setDaemon(true);
                    reloadThread.start();
                    isReloadRouterTaskRunning = true;
                }
            }
        }
    }
}
