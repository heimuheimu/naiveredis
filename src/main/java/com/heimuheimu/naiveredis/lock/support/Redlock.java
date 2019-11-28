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

package com.heimuheimu.naiveredis.lock.support;

import com.heimuheimu.naiveredis.constant.BeanStatusEnum;
import com.heimuheimu.naiveredis.exception.RedisDistributedLockException;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.lock.LockConfiguration;
import com.heimuheimu.naiveredis.lock.LockInfo;
import com.heimuheimu.naiveredis.lock.RedisDistributedLock;
import com.heimuheimu.naiveredis.monitor.RedisDistributedLockMonitor;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * 使用 Redlock 算法实现的 Redis 分布式锁，更多信息请参考文档：
 * <a href="https://redis.io/topics/distlock#the-redlock-algorithm">The Redlock algorithm</a>
 *
 * <p><strong>注意：</strong>在极端情况下，可能会出现两个客户端同时持有同一个 Redis 分布式锁。</p>
 * <p><strong>说明：</strong>Redlock 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class Redlock implements RedisDistributedLock, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoReconnectRedisLockClient.class);

    private static final Logger REDIS_DISTRIBUTED_LOCK_LOG = LoggerFactory.getLogger("NAIVEREDIS_DISTRIBUTED_LOCK_LOG");

    /**
     * Redis 地址数组
     */
    private final String[] hosts;

    /**
     * 自动重连 Redis 分布式锁客户端数组，与 {@link #hosts} 对应
     */
    private final AutoReconnectRedisLockClient[] clients;

    /**
     * 最低限度需要获取成功的锁数量
     */
    private final int minimumLockCount;

    /**
     * Redis 分布式锁信息监控器
     */
    private final RedisDistributedLockMonitor monitor = RedisDistributedLockMonitor.getInstance();

    /**
     * 当前实例所处状态，访问此变量需获取 this 锁
     */
    private BeanStatusEnum state = BeanStatusEnum.NORMAL;

    /**
     * 构造一个 Redlock 实例，Socket 配置信息将会使用 {@link SocketConfiguration#DEFAULT}，PING 命令发送时间间隔为 30 秒。
     *
     * @param hosts Redis 地址数组，数组长度必须大于等于 3，Redis 地址由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param listener 自动重连 Redis 分布式锁客户端事件监听器，允许为 {@code null}
     * @throws IllegalArgumentException 如果 Redis 地址数组长度小于 3，将会抛出此异常
     * @throws IllegalStateException 如果 Redlock 创建过程中发生错误，将会抛出此异常
     */
    public Redlock(String[] hosts, AutoReconnectRedisLockClientListener listener)
            throws IllegalArgumentException, IllegalStateException {
        this(hosts, null, 30, listener);
    }

    /**
     * 构造一个 Redlock 实例。
     *
     * @param hosts Redis 地址数组，数组长度必须大于等于 3，Redis 地址由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param configuration Socket 配置信息，如果传 {@code null}，将会使用 {@link SocketConfiguration#DEFAULT} 配置信息
     * @param pingPeriod PING 命令发送时间间隔，单位：秒。用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param listener 自动重连 Redis 分布式锁客户端事件监听器，允许为 {@code null}
     * @throws IllegalArgumentException 如果 Redis 地址数组长度小于 3，将会抛出此异常
     * @throws IllegalStateException 如果 Redlock 创建过程中发生错误，将会抛出此异常
     */
    public Redlock(String[] hosts, SocketConfiguration configuration, int pingPeriod,
                   AutoReconnectRedisLockClientListener listener) throws IllegalArgumentException, IllegalStateException {
        if (hosts.length < 3) {
            String errorMessage = "Fails to construct Redlock: `hosts length must equal or greater than 3`. `hosts`:`"
                    + Arrays.toString(hosts) + "`.";
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage);
            LOGGER.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
        try {
            this.hosts = hosts;
            this.clients = new AutoReconnectRedisLockClient[hosts.length];
            for (int i = 0; i < hosts.length; i++) {
                this.clients[i] = new AutoReconnectRedisLockClient(hosts[i], configuration, pingPeriod, listener);
            }
        } catch (Exception e) {
            String errorMessage = "Fails to construct Redlock: `create AutoReconnectRedisLockClient failed`. `hosts`:`"
                    + Arrays.toString(hosts) + "`.";
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage, e);
            LOGGER.error(errorMessage, e);
            close();
            throw new IllegalStateException(errorMessage, e);
        }
        this.minimumLockCount = this.hosts.length / 2 + 1;
        REDIS_DISTRIBUTED_LOCK_LOG.info("Redlock init success. `hosts`:`" + Arrays.toString(hosts) + "`. `configuration`:`"
                + configuration + "`. `pingPeriod`:`" + pingPeriod + "`. `listener`:`" + listener + "`.");
    }

    @Override
    public LockInfo tryLock(String name) throws RedisDistributedLockException {
        return tryLock(name, LockConfiguration.DEFAULT);
    }

    @Override
    public LockInfo tryLock(String name, LockConfiguration configuration) throws RedisDistributedLockException {
        try {
            String token = UUID.randomUUID().toString();
            LockInfo lockInfo = tryLock(name, token, configuration.getValidity(), configuration.getTimeout());
            if (lockInfo != null) {
                monitor.onSuccess();
            } else {
                monitor.onFail();
            }
            return lockInfo;
        } catch (RedisDistributedLockException e) {
            monitor.onError();
            throw e;
        } catch (Exception e) { // should not happen
            monitor.onError();
            String errorMessage = "Acquire redis distributed lock failed. `hosts`:`" + Arrays.toString(hosts)
                    + "`. `name`:`" + name + "`. `configuration`:`" + configuration + "`.";
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage, e);
            throw new RedisDistributedLockException(errorMessage, e);
        }
    }

    @Override
    public LockInfo tryLock(String name, int waitTime) throws RedisDistributedLockException {
        return tryLock(name, LockConfiguration.DEFAULT, waitTime);
    }

    @Override
    public LockInfo tryLock(String name, LockConfiguration configuration, int waitTime) throws RedisDistributedLockException {
        try {
            long startTime = System.currentTimeMillis();
            String token = UUID.randomUUID().toString();
            while (System.currentTimeMillis() - startTime < waitTime) {
                LockInfo lockInfo = tryLock(name, token, configuration.getValidity(), configuration.getTimeout());
                if (lockInfo != null) {
                    monitor.onSuccess();
                    return lockInfo;
                } else {
                    try {
                        Thread.sleep(configuration.getRandomDelay());
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            }
            monitor.onFail();
            return null;
        } catch (RedisDistributedLockException e) {
            monitor.onError();
            throw e;
        } catch (Exception e) { // should not happen
            monitor.onError();
            String errorMessage = "Acquire redis distributed lock failed. `hosts`:`" + Arrays.toString(hosts)
                    + "`. `name`:`" + name + "`. `configuration`:`" + configuration + "`.";
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage, e);
            throw new RedisDistributedLockException(errorMessage, e);
        }
    }

    @Override
    public void unlock(LockInfo lockInfo) throws RedisDistributedLockException {
        if (lockInfo != null) {
            ArrayList<String> errorHosts = new ArrayList<>();
            for (AutoReconnectRedisLockClient client : clients) {
                try {
                    client.unlock(lockInfo.getName(), lockInfo.getToken());
                } catch (Exception ignored) {
                    errorHosts.add(client.getHost());
                }
            }
            if (isClusterCrashed(errorHosts.size())) {
                monitor.onUnlockError();
                String errorMessage = "Release redis distributed lock failed. `errorHosts`:`" + errorHosts + "`. `hosts`:`"
                        + Arrays.toString(hosts) + "`. `lockInfo`:`" + lockInfo + "`.";
                throw new RedisDistributedLockException(errorMessage);
            } else {
                long holdingTime = System.currentTimeMillis() - lockInfo.getCreatedTime();
                monitor.onUnlockSuccess(holdingTime);
            }
        }
    }

    @Override
    public synchronized void close() {
        if (state != BeanStatusEnum.CLOSED) {
            state = BeanStatusEnum.CLOSED;
            for (AutoReconnectRedisLockClient client : clients) {
                if (client != null) {
                    client.close();
                }
            }
            REDIS_DISTRIBUTED_LOCK_LOG.info("Redlock has been closed. `hosts`:`" + Arrays.toString(hosts) + "`. ");
        }
    }

    @Override
    public String toString() {
        return "Redlock{" +
                "hosts=" + Arrays.toString(hosts) +
                ", clients=" + Arrays.toString(clients) +
                ", monitor=" + monitor +
                '}';
    }

    /**
     * 根据 Redlock 算法获取 Redis 分布式锁（Redis 集群中一半以上获取锁成功），如果获取成功，将会返回对应的锁信息，
     * 如果锁已被占用，将会返回 null。
     *
     * @param name 锁名称，不允许为 {@code null} 或空字符串
     * @param token 锁对应的 token 值，不允许为 {@code null} 或空字符串
     * @param validity 锁的最大持有时间，单位：秒，不允许小于等于 0
     * @param timeout 获取锁的超时时间，单位：毫秒，不允许小于等于 0，通常超时时间应远小于锁的最大持有时间
     * @return 锁信息，可能为 {@code null}
     * @throws RedisDistributedLockException 如果获取 Redis 分布式锁过程中发生错误，将会抛出此异常
     */
    private LockInfo tryLock(String name, String token, int validity, int timeout) throws RedisDistributedLockException {
        if (name == null || name.isEmpty()) {
            String errorMessage = "Acquire redis distributed lock failed: `name could not be empty`. `hosts`:`"
                    + Arrays.toString(hosts) + "`.";
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage);
            throw new RedisDistributedLockException(errorMessage);
        }
        try {
            LockInfo lockInfo = new LockInfo(name, token, validity);
            FutureLock[] futureLocks = new FutureLock[clients.length];
            ArrayList<String> errorHosts = new ArrayList<>();
            for (int i = 0; i < clients.length; i++) {
                try {
                    futureLocks[i] = clients[i].submit(name, token, validity);
                } catch (IllegalStateException | RedisException ignored) {
                    errorHosts.add(hosts[i]);
                }
            }
            if (isClusterCrashed(errorHosts.size())) {
                String errorMessage = "Acquire redis distributed lock failed. `errorHosts`:`" + errorHosts + "`. `hosts`:`"
                        + Arrays.toString(hosts) + "`. `name`:`" + name + "`. `token`:`" + token + "`. `validity`:`"
                        + validity + "`. `timeout`:`" + timeout + "`.";
                REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage);
                throw new RedisDistributedLockException(errorMessage);
            }
            ArrayList<AutoReconnectRedisLockClient> successClients = new ArrayList<>();
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < clients.length; i++) {
                if (futureLocks[i] != null) {
                    int leftTimeout = Math.max(1, timeout - (int) (System.currentTimeMillis() - startTime));
                    try {
                        if (futureLocks[i].get(leftTimeout)) {
                            successClients.add(clients[i]);
                        }
                    } catch (IllegalStateException ignored) {
                        errorHosts.add(hosts[i]);
                    } catch (TimeoutException e) { // try to release lock due to timeout
                        errorHosts.add(hosts[i]);
                        try {
                            clients[i].unlock(name, token);
                        } catch (IllegalStateException | RedisException ignored) {}
                    }
                }
            }

            if (successClients.size() >= minimumLockCount) {
                return lockInfo;
            } else {
                for (AutoReconnectRedisLockClient successClient : successClients) { // try to release lock due to fails to acquire
                    try {
                        successClient.unlock(name, token);
                    } catch (IllegalStateException | RedisException ignored) {}
                }
                if (isClusterCrashed(errorHosts.size())) {
                    String errorMessage = "Acquire redis distributed lock failed. `errorHosts`:`" + errorHosts + "`. `hosts`:`"
                            + Arrays.toString(hosts) + "`. `name`:`" + name + "`. `token`:`" + token + "`. `validity`:`"
                            + validity + "`. `timeout`:`" + timeout + "`.";
                    REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage);
                    throw new RedisDistributedLockException(errorMessage);
                }
                return null;
            }
        } catch (RedisDistributedLockException e) {
            throw e;
        } catch (Exception e) { // unexpected error
            String errorMessage = "Acquire redis distributed lock failed. `hosts`:`" + Arrays.toString(hosts)
                    + "`. `name`:`" + name + "`. `token`:`" + token + "`. `validity`:`" + validity + "`. `timeout`:`"
                    + timeout + "`.";
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage, e);
            throw new RedisDistributedLockException(errorMessage, e);
        }
    }

    private boolean isClusterCrashed(int errorHostsCount) {
        return hosts.length - errorHostsCount < minimumLockCount;
    }
}
