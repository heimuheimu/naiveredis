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

package com.heimuheimu.naiveredis.facility.clients;

import com.heimuheimu.naiveredis.DirectRedisClient;
import com.heimuheimu.naiveredis.constant.BeanStatusEnum;
import com.heimuheimu.naiveredis.facility.Methods;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Redis 直连客户端列表，提供客户端自动恢复功能。
 *
 * <h3>Redis 连接信息 Log4j 配置</h3>
 * <blockquote>
 * <pre>
 * log4j.logger.NAIVEREDIS_CONNECTION_LOG=INFO, NAIVEREDIS_CONNECTION_LOG
 * log4j.additivity.NAIVEREDIS_CONNECTION_LOG=false
 * log4j.appender.NAIVEREDIS_CONNECTION_LOG=org.apache.log4j.DailyRollingFileAppender
 * log4j.appender.NAIVEREDIS_CONNECTION_LOG.file=${log.output.directory}/naiveredis/connection.log
 * log4j.appender.NAIVEREDIS_CONNECTION_LOG.encoding=UTF-8
 * log4j.appender.NAIVEREDIS_CONNECTION_LOG.DatePattern=_yyyy-MM-dd
 * log4j.appender.NAIVEREDIS_CONNECTION_LOG.layout=org.apache.log4j.PatternLayout
 * log4j.appender.NAIVEREDIS_CONNECTION_LOG.layout.ConversionPattern=%d{ISO8601} %-5p : %m%n
 * </pre>
 * </blockquote>
 *
 * <p><strong>说明：</strong>{@code DirectRedisClientList} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class DirectRedisClientList implements Closeable {

    private static final Logger REDIS_CONNECTION_LOG = LoggerFactory.getLogger("NAIVEREDIS_CONNECTION_LOG");

    private static final Logger LOG = LoggerFactory.getLogger(DirectRedisClientList.class);

    /**
     * Redis 直连客户端列表名称
     */
    private final String name;

    /**
     * Redis 地址数组，Redis 地址由主机名和端口组成，":"符号分割，例如：localhost:6379
     */
    private final String[] hosts;

    /**
     * 创建 Redis 直连客户端所使用的 Socket 配置信息
     */
    private final SocketConfiguration configuration;

    /**
     * Redis 操作超时时间，单位：毫秒，不能小于等于 0
     */
    private final int timeout;

    /**
     * 最小压缩字节数，当 Value 字节数小于或等于该值，不进行压缩，不能小于等于 0
     */
    private final int compressionThreshold;

    /**
     * Redis 操作过慢最小时间，单位：毫秒，不能小于等于 0
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
     * Redis 直连客户端列表，该列表顺序、大小与 {@link #hosts} 一致
     * <p>
     *     如果某个 Redis 直连客户端不可用，该客户端在列表中的值为 {@code null}
     * </p>
     */
    private final CopyOnWriteArrayList<DirectRedisClient> clientList = new CopyOnWriteArrayList<>();

    /**
     * {@link #clientList} 元素发生变更操作时，使用的私有锁
     */
    private final Object clientListUpdateLock = new Object();

    /**
     * Redis 直连客户端恢复任务是否运行
     */
    private boolean isRescueTaskRunning = false;

    /**
     * Redis 直连客户端恢复任务使用的私有锁
     */
    private final Object rescueTaskLock = new Object();

    /**
     * Redis 直连客户端列表所处状态
     */
    private volatile BeanStatusEnum state = BeanStatusEnum.NORMAL;

    /**
     * 构造一个 Redis 直连客户端列表，提供客户端自动恢复功能。
     *
     * @param name Redis 直连客户端列表名称
     * @param hosts Redis 地址数组，Redis 地址由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param configuration 创建 Redis 直连客户端所使用的 Socket 配置信息，允许为 {@code null}
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param compressionThreshold 最小压缩字节数，当 Value 字节数小于或等于该值，不进行压缩，不能小于等于 0
     * @param slowExecutionThreshold Redis 操作过慢最小时间，单位：毫秒，不能小于等于 0
     * @param pingPeriod PING 命令发送时间间隔，单位：秒，用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param listener Redis 直连客户端列表事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果所有 Redis 直连客户端均不可用，将会抛出此异常
     */
    public DirectRedisClientList(String name, String[] hosts, SocketConfiguration configuration, int timeout, int compressionThreshold,
                                 int slowExecutionThreshold, int pingPeriod, DirectRedisClientListListener listener) throws IllegalStateException {
        this.name = name;
        this.hosts = hosts;
        this.configuration = configuration;
        this.timeout = timeout;
        this.compressionThreshold = compressionThreshold;
        this.slowExecutionThreshold = slowExecutionThreshold;
        this.pingPeriod = pingPeriod;
        this.listener = listener;
        boolean hasAvailableClient = false;
        for (String host : hosts) {
            boolean isSuccess = createClient(-1, host);
            if (isSuccess) {
                hasAvailableClient = true;
                REDIS_CONNECTION_LOG.info("Add `{}` to `{}` is success. Hosts: `{}`.", host, name, hosts);
                Methods.invokeIfNotNull("DirectRedisClientListListener#onCreated(String host)", getParameterMap(-1, host),
                        listener, () -> listener.onCreated(name, host));
            } else {
                REDIS_CONNECTION_LOG.error("Add `{}` to `{}` failed. Hosts: `{}`.", host, name, hosts);
                Methods.invokeIfNotNull("DirectRedisClientListListener#onClosed(String host)", getParameterMap(-1, host),
                        listener, () -> listener.onClosed(name, host));
            }
        }
        if ( !hasAvailableClient ) {
            LOG.error("There is no available `DirectRedisClient`. `name`:`" + name + "`. hosts:`" + Arrays.toString(hosts) + "`.");
            throw new IllegalStateException("There is no available `DirectRedisClient`. `name`:`" + name + "`. hosts:`" + Arrays.toString(hosts) + "`.");
        }
    }

    /**
     * 获得 Redis 地址数组，不会返回 {@code null} 或空数组。
     *
     * @return Redis 地址数组
     */
    public String[] getHosts() {
        String[] copyHosts = new String[hosts.length];
        System.arraycopy(hosts, 0, copyHosts, 0, hosts.length);
        return copyHosts;
    }

    /**
     * 获得指定索引对应的 Redis 直连客户端，如果该客户端不可用，则随机获取一个可用客户端返回，如果当前没有可用客户端，将返回 {@code null}。
     *
     * @param clientIndex 索引位置
     * @param excludeClientIndices 随机获取可用客户端时，需要排除的索引位置
     * @return Redis 直连客户端，可能返回 {@code null}
     * @throws IndexOutOfBoundsException 如果索引位置越界，将抛出此异常
     */
    public DirectRedisClient orAvailableClient(int clientIndex, int... excludeClientIndices) {
        DirectRedisClient client = get(clientIndex);
        if (client == null) {
            client = getAvailableClient(excludeClientIndices);
        }
        return client;
    }

    /**
     * 获得指定索引对应的 Redis 直连客户端，如果该客户端不可用，则返回 {@code null}。
     *
     * @param clientIndex 索引位置
     * @return 索引对应的 Redis 直连客户端，可能返回 {@code null}
     * @throws IndexOutOfBoundsException 如果索引位置越界，将抛出此异常
     */
    public DirectRedisClient get(int clientIndex) throws IndexOutOfBoundsException {
        if (clientIndex >= hosts.length) {
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("DirectRedisClientList#get(int clientIndex)",
                    "client index out of range", getParameterMap(clientIndex, null));
            LOG.error(errorMessage);
            throw new IndexOutOfBoundsException(errorMessage);
        }
        DirectRedisClient redisClient = clientList.get(clientIndex);
        if (redisClient != null) {
            if (!redisClient.isAvailable()) {
                LOG.debug("DirectRedisClient is unavailable. `clientIndex`:`{}`. `host`:`{}`.", clientIndex, hosts[clientIndex]);
                removeUnavailableClient(redisClient);
                redisClient = null;
            } else {
                LOG.debug("Choose DirectRedisClient success. `clientIndex`:`{}`. `host`:`{}`.", clientIndex, hosts[clientIndex]);
            }
        } else {
            LOG.debug("DirectRedisClient is null. `clientIndex`:`{}`. `host`:`{}`.", clientIndex, hosts[clientIndex]);
            startRescueTask(); // make sure rescue task is running
        }
        return redisClient;
    }

    /**
     * 随机获取一个可用客户端返回，如果当前没有可用客户端，将返回 {@code null}。
     *
     * @param excludeClientIndices 随机获取可用客户端时，需要排除的索引位置
     * @return Redis 直连客户端，可能返回 {@code null}
     */
    public DirectRedisClient getAvailableClient(int... excludeClientIndices) {
        List<DirectRedisClient> availableClientList = new ArrayList<>();
        for (int i = 0; i < hosts.length; i++) {
            boolean isExcludeIndex = false;
            for (int excludeClientIndex : excludeClientIndices) {
                if (i == excludeClientIndex) {
                    LOG.debug("Exclude client index. `clientIndex`:`{}`. `host`:`{}`.", i, hosts[i]);
                    isExcludeIndex = true;
                    break;
                }
            }
            if (!isExcludeIndex) {
                DirectRedisClient client = clientList.get(i);
                if (client != null && client.isAvailable()) {
                    availableClientList.add(client);
                    LOG.debug("Add to available client list. `clientIndex`:`{}`. `host`:`{}`.", i, hosts[i]);
                } else {
                    LOG.debug("Unavailable client. `clientIndex`:`{}`. `host`:`{}`.", i, hosts[i]);
                }
            }
        }
        if (!availableClientList.isEmpty()) {
            DirectRedisClient availableClient = availableClientList.get(new Random().nextInt(availableClientList.size()));
            LOG.debug("Choose random available client success. `host`:`{}`.", availableClient.getHost());
            return availableClient;
        } else {
            LOG.debug("Choose random available client failed: `there is no available client`.");
            return null;
        }
    }

    @Override
    public synchronized void close() {
        if (state != BeanStatusEnum.CLOSED) {
            state = BeanStatusEnum.CLOSED;
            for (DirectRedisClient client : clientList) {
                if (client != null) {
                    client.close();
                }
            }
            REDIS_CONNECTION_LOG.info("DirectRedisClientList has been closed. `name`:`{}`. `hosts`:`{}`.", name, hosts);
        }
    }

    @Override
    public String toString() {
        return "DirectRedisClientList{" +
                "name='" + name + '\'' +
                ", hosts=" + Arrays.toString(hosts) +
                ", configuration=" + configuration +
                ", timeout=" + timeout +
                ", compressionThreshold=" + compressionThreshold +
                ", slowExecutionThreshold=" + slowExecutionThreshold +
                ", pingPeriod=" + pingPeriod +
                ", listener=" + listener +
                ", clientList=" + clientList +
                ", isRescueTaskRunning=" + isRescueTaskRunning +
                ", state=" + state +
                '}';
    }

    /**
     * 根据 Redis 地址，创建一个 Redis 直连客户端，并将其放入列表指定索引位置，如果索引位置小于 0，则在列表中新增该直连客户端。
     *
     * @param clientIndex 索引位置，如果为 -1，则在列表中添加
     * @param host Redis 地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @return 是否创建成功
     */
    private boolean createClient(int clientIndex, String host) {
        DirectRedisClient client = null;
        try {
            client = new DirectRedisClient(host, configuration, timeout, compressionThreshold, slowExecutionThreshold, pingPeriod,
                    this::removeUnavailableClient);
        } catch (Exception ignored) {}

        synchronized (clientListUpdateLock) {
            if (client != null && client.isAvailable()) {
                if (clientIndex < 0) {
                    clientList.add(client);
                } else {
                    clientList.set(clientIndex, client);
                }
                LOG.debug("Add `DirectRedisClient` to client list success." + LogBuildUtil.build(getParameterMap(clientIndex, host)));
                return true;
            } else {
                if (clientIndex < 0) {
                    clientList.add(null);
                } else {
                    clientList.set(clientIndex, null);
                }
                LOG.error("Add `DirectRedisClient` to client list failed." + LogBuildUtil.build(getParameterMap(clientIndex, host)));
                return false;
            }
        }
    }

    /**
     * 从列表中移除不可用的 Redis 直连客户端。
     *
     * @param unavailableClient 不可用的 Redis 直连客户端
     */
    private void removeUnavailableClient(DirectRedisClient unavailableClient) {
        if (unavailableClient == null) { //should not happen, just for bug detection
            String errorMessage = "Remove unavailable client failed: `null client`." +
                    LogBuildUtil.build(getParameterMap(-1, null));
            LOG.error(errorMessage);
            throw new NullPointerException(errorMessage);
        }
        boolean isRemoveSuccess = false;
        int clientIndex;
        synchronized (clientListUpdateLock) {
            clientIndex = clientList.indexOf(unavailableClient);
            if (clientIndex >= 0) {
                clientList.set(clientIndex, null);
                isRemoveSuccess = true;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Remove `DirectRedisClient` from client list success. `clientIndex`:`{}`.{}", clientIndex,
                            LogBuildUtil.build(getParameterMap(clientIndex, unavailableClient.getHost())));
                }
            }
        }
        if (isRemoveSuccess) {
            startRescueTask();
            Methods.invokeIfNotNull("DirectRedisClientListListener#onClosed(String host)", getParameterMap(clientIndex, unavailableClient.getHost()),
                    listener, () -> listener.onClosed(name, unavailableClient.getHost()));
        }
    }

    /**
     * 获得方法运行的通用参数 {@code Map}，用于日志打印。
     *
     * @param clientIndex 索引位置，允许小于 0
     * @param host Redis 地址，允许为 {@code null} 或空
     * @return 通用参数 {@code Map}
     */
    private Map<String, Object> getParameterMap(int clientIndex, String host) {
        Map<String, Object> parameterMap = new LinkedHashMap<>();
        if (clientIndex >= 0) {
            parameterMap.put("clientIndex", clientIndex);
        }
        if (host != null && !host.isEmpty()) {
            parameterMap.put("host", host);
        }
        parameterMap.put("name", name);
        parameterMap.put("hosts", hosts);
        return parameterMap;
    }

    /**
     * 启动 Redis 直连客户端重连恢复任务。
     */
    private void startRescueTask() {
        if (state == BeanStatusEnum.NORMAL) {
            synchronized (rescueTaskLock) {
                if (!isRescueTaskRunning) {
                    Thread rescueThread = new Thread() {

                        @Override
                        public void run() {
                            long startTime = System.currentTimeMillis();
                            REDIS_CONNECTION_LOG.info("DirectRedisClient rescue task has been started. `name`:`{}`. `hosts`:`{}`.", name, Arrays.toString(hosts));
                            try {
                                while (state == BeanStatusEnum.NORMAL) {
                                    boolean hasRecovered = true;
                                    for (int i = 0; i < hosts.length; i++) {
                                        if (clientList.get(i) == null) {
                                            String host = hosts[i];
                                            boolean isSuccess = createClient(i, host);
                                            if (isSuccess) {
                                                REDIS_CONNECTION_LOG.info("Rescue `{}` success. `name`:`{}`. `hosts`:`{}`.", host, name, Arrays.toString(hosts));
                                                Methods.invokeIfNotNull("DirectRedisClientListListener#onRecovered(String host)", getParameterMap(i, host),
                                                        listener, () -> listener.onRecovered(name, host));
                                            } else {
                                                hasRecovered = false;
                                                REDIS_CONNECTION_LOG.warn("Rescue `{}` failed. `name`:`{}`. `hosts`:`{}`.", host, name, Arrays.toString(hosts));
                                            }
                                        }
                                    }
                                    if (hasRecovered) {
                                        break;
                                    } else {
                                        Thread.sleep(500); //还有未恢复的客户端，等待 500ms 后继续尝试
                                    }
                                }
                                REDIS_CONNECTION_LOG.info("DirectRedisClient rescue task has been finished. Cost: {}ms. `name`:`{}`. `hosts`:`{}`.",
                                        System.currentTimeMillis() - startTime, name, hosts);
                            } catch (Exception e) { //should not happen, just for bug detection
                                REDIS_CONNECTION_LOG.error("DirectRedisClient rescue task execute failed: `{}`. Cost: {}ms. `name`:`{}`. `hosts`:`{}`.",
                                        e.getMessage(), System.currentTimeMillis() - startTime, name, hosts);
                                LOG.error("DirectRedisClient rescue task executed failed. `name`:`" + name + "`. `hosts`:`"
                                        + Arrays.toString(hosts) + "`.", e);
                            } finally {
                                rescueOver();
                            }
                        }

                        private void rescueOver() {
                            synchronized (rescueTaskLock) {
                                isRescueTaskRunning = false;
                            }
                        }

                    };
                    rescueThread.setName("naiveredis-redis-client-rescue-task");
                    rescueThread.setDaemon(true);
                    rescueThread.start();
                    isRescueTaskRunning = true;
                }
            }
        }
    }
}
