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
import com.heimuheimu.naiveredis.lock.LockConfiguration;
import com.heimuheimu.naiveredis.lock.LockInfo;
import com.heimuheimu.naiveredis.lock.RedisDistributedLock;
import com.heimuheimu.naiveredis.monitor.RedisDistributedLockMonitor;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.UUID;

/**
 * 使用 Redis 单实例实现的 Redis 分布式锁，更多信息请参考文档：
 * <a href="https://redis.io/topics/distlock#correct-implementation-with-a-single-instance">Correct implementation with a single instance</a>
 *
 * <p><strong>注意：</strong>在极端情况下，可能会出现两个客户端同时持有同一个 Redis 分布式锁。</p>
 * <p><strong>说明：</strong>SingleInstanceLock 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class SingleInstanceLock implements RedisDistributedLock, Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoReconnectRedisLockClient.class);

    private static final Logger REDIS_DISTRIBUTED_LOCK_LOG = LoggerFactory.getLogger("NAIVEREDIS_DISTRIBUTED_LOCK_LOG");

    /**
     * Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     */
    private final String host;

    /**
     * 自动重连 Redis 分布式锁客户端
     */
    private final AutoReconnectRedisLockClient client;

    /**
     * Redis 分布式锁信息监控器
     */
    private final RedisDistributedLockMonitor monitor = RedisDistributedLockMonitor.getInstance();

    /**
     * 当前实例所处状态，访问此变量需获取 this 锁
     */
    private BeanStatusEnum state = BeanStatusEnum.NORMAL;

    /**
     * 构造一个 SingleInstanceLock 实例，Socket 配置信息将会使用 {@link SocketConfiguration#DEFAULT}，PING 命令发送时间间隔为 30 秒。
     *
     * @param host Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param listener 自动重连 Redis 分布式锁客户端事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果 SingleInstanceLock 创建过程中发生错误，将会抛出此异常
     */
    public SingleInstanceLock(String host, AutoReconnectRedisLockClientListener listener) throws IllegalStateException {
        this(host, null, 30, listener);
    }

    /**
     * 构造一个 SingleInstanceLock 实例。
     *
     * @param host Redis 服务主机地址，由主机名和端口组成，":"符号分割，例如：localhost:6379
     * @param configuration Socket 配置信息，如果传 {@code null}，将会使用 {@link SocketConfiguration#DEFAULT} 配置信息
     * @param pingPeriod PING 命令发送时间间隔，单位：秒。用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param listener 自动重连 Redis 分布式锁客户端事件监听器，允许为 {@code null}
     * @throws IllegalStateException 如果 SingleInstanceLock 创建过程中发生错误，将会抛出此异常
     */
    public SingleInstanceLock(String host, SocketConfiguration configuration, int pingPeriod,
                              AutoReconnectRedisLockClientListener listener) throws IllegalStateException {
        this.host = host;
        try {
            this.client = new AutoReconnectRedisLockClient(host, configuration, pingPeriod, listener);
        } catch (Exception e) {
            String errorMessage = "Fails to construct SingleInstanceLock: `create AutoReconnectRedisLockClient failed`. `host`:`"
                    + host + "`.";
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage, e);
            LOGGER.error(errorMessage, e);
            throw new IllegalStateException(errorMessage, e);
        }
        REDIS_DISTRIBUTED_LOCK_LOG.info("SingleInstanceLock init success. `host`:`" + host + "`. `configuration`:`"
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
            LockInfo lockInfo = new LockInfo(name, token, configuration.getValidity());
            boolean isSuccess = client.tryLock(name, token, configuration.getValidity(), configuration.getTimeout());
            if (isSuccess) {
                monitor.onSuccess();
                return lockInfo;
            } else {
                monitor.onFail();
                return null;
            }
        } catch (Exception e) {
            monitor.onError();
            String errorMessage = "Acquire redis distributed lock failed. `host`:`" + host + "`. `name`:`" + name
                    + "`. `configuration`:`" + configuration + "`.";
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
                LockInfo lockInfo = new LockInfo(name, token, configuration.getValidity());
                boolean isSuccess = client.tryLock(name, token, configuration.getValidity(), configuration.getTimeout());
                if (isSuccess) {
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
        } catch (Exception e) {
            monitor.onError();
            String errorMessage = "Acquire redis distributed lock failed. `host`:`" + host + "`. `name`:`" + name
                    + "`. `waitTime`:`" + waitTime + "ms`. `configuration`:`" + configuration + "`.";
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage, e);
            throw new RedisDistributedLockException(errorMessage, e);
        }
    }

    @Override
    public void unlock(LockInfo lockInfo) throws RedisDistributedLockException {
        if (lockInfo != null) {
            try {
                client.unlock(lockInfo.getName(), lockInfo.getToken());
                long holdingTime = System.currentTimeMillis() - lockInfo.getCreatedTime();
                monitor.onUnlockSuccess(holdingTime);
            } catch (Exception e) {
                monitor.onUnlockError();
                String errorMessage = "Release redis distributed lock failed. `host`:`" + host + "`. `lockInfo`:`" + lockInfo + "`.";
                throw new RedisDistributedLockException(errorMessage, e);
            }
        }
    }

    @Override
    public synchronized void close() {
        if (state != BeanStatusEnum.CLOSED) {
            state = BeanStatusEnum.CLOSED;
            client.close();
            REDIS_DISTRIBUTED_LOCK_LOG.info("SingleInstanceLock has been closed. `host`:`" + host + "`. ");
        }
    }

    @Override
    public String toString() {
        return "SingleInstanceLock{" +
                "host='" + host + '\'' +
                ", client=" + client +
                '}';
    }
}
