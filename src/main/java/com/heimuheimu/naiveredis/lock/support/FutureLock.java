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

import com.heimuheimu.naivemonitor.monitor.ExecutionMonitor;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.command.storage.SetCommand;
import com.heimuheimu.naiveredis.constant.BeanStatusEnum;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.monitor.RedisLockClientMonitorFactory;
import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;

/**
 * 可异步获取的 Redis 分布式锁，FutureLock 实例通过调用 {@link AutoReconnectRedisLockClient#submit(String, String, int)} 方法返回，
 * 不应通过构造函数创建 FutureLock 实例。
 *
 * <p><strong>说明：</strong>FutureLock 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class FutureLock {

    private static final Logger REDIS_DISTRIBUTED_LOCK_LOG = LoggerFactory.getLogger("NAIVEREDIS_DISTRIBUTED_LOCK_LOG");

    /**
     * 获取锁操作开始时间
     */
    private final long startNanoTime;

    /**
     * 锁名称
     */
    private final String name;

    /**
     * 锁对应的 token 值
     */
    private final String token;

    /**
     * 锁的过期时间
     */
    private final int expiry;

    /**
     * 获取锁使用的 SetCommand 命令
     */
    private final SetCommand command;

    /**
     * 与 Redis 服务进行数据交互的管道
     */
    private final RedisChannel channel;

    /**
     * Redis 分布式锁客户端使用的执行信息监控器
     */
    private final ExecutionMonitor monitor;

    /**
     * 当前实例所处状态，访问此变量需要获取 this 锁
     */
    private BeanStatusEnum state = BeanStatusEnum.NORMAL;

    /**
     * 构造一个 FutureLock 实例。
     *
     * @param startNanoTime 获取 Redis 分布式锁的开始时间
     * @param name 锁名称
     * @param token 锁对应的 token 值
     * @param expiry 锁的过期时间
     * @param command 获取锁使用的 SetCommand 命令，不允许为 {@code null}
     * @param channel 与 Redis 服务进行数据交互的管道，不允许为 {@code null}
     * @param monitor Redis 分布式锁客户端使用的执行信息监控器，不允许为 {@code null}
     */
    FutureLock(long startNanoTime, String name, String token, int expiry, SetCommand command, RedisChannel channel, ExecutionMonitor monitor) {
        this.startNanoTime = startNanoTime;
        this.name = name;
        this.token = token;
        this.expiry = expiry;
        this.command = command;
        this.channel = channel;
        this.monitor = monitor;
    }

    /**
     * 返回 Redis 分布式锁获取的结果，获取成功返回 {@code true}，否则返回 {@code false}。
     *
     * <p><strong>注意：</strong>此方法仅允许调用一次，重复调用将会抛出 IllegalStateException 异常。</p>
     *
     * @param timeout 获取锁的超时时间，单位：毫秒，不允许小于等于 0
     * @return Redis 分布式锁获取的结果
     * @throws IllegalArgumentException 如果 timeout 小于等于 0，将会抛出此异常
     * @throws TimeoutException 如果等待 Redis 分布式锁获取的结果超时，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 命令在等待 Redis 分布式锁获取结果时被关闭，将会抛出此异常
     * @throws IllegalStateException 此方法仅允许调用一次，如果重复调用，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错或出现其它未预期异常，将会抛出此异常
     */
    public synchronized boolean get(int timeout) throws IllegalArgumentException, TimeoutException, IllegalStateException, RedisException {
        if (state == BeanStatusEnum.NORMAL) {
            if (timeout <= 0) {
                monitor.onError(RedisLockClientMonitorFactory.ERROR_CODE_ILLEGAL_ARGUMENT);
                monitor.onExecuted(startNanoTime);
                String errorMessage = buildLockErrorMessage("timeout must greater than 0", name, token, expiry, timeout);
                REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            try {
                RedisData response = command.getResponseData(timeout);
                if (response.isError()) {
                    monitor.onError(RedisLockClientMonitorFactory.ERROR_CODE_REDIS_ERROR); // should not happen
                    String errorMessage = buildLockErrorMessage(response.getText(), name, token, expiry, timeout);
                    REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage);
                    throw new RedisException(RedisException.CODE_REDIS_SERVER, errorMessage);
                } else {
                    if (response.getValueBytes() != null) {
                        return true;
                    } else {
                        monitor.onError(RedisLockClientMonitorFactory.ERROR_CODE_LOCK_IS_EXIST);
                        if (REDIS_DISTRIBUTED_LOCK_LOG.isDebugEnabled()) {
                            REDIS_DISTRIBUTED_LOCK_LOG.debug(buildLockErrorMessage("lock is exist", name, token, expiry, timeout));
                        }
                        return false;
                    }
                }
            } catch (RedisException e) {
                throw e;
            } catch (TimeoutException e) {
                channel.onTimeout();
                monitor.onError(RedisLockClientMonitorFactory.ERROR_CODE_TIMEOUT);
                String errorMessage = buildLockErrorMessage("timeout", name, token, expiry, timeout);
                REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage, e);
                throw new TimeoutException(errorMessage, e);
            } catch (IllegalStateException e) {
                monitor.onError(RedisLockClientMonitorFactory.ERROR_CODE_ILLEGAL_STATE);
                String errorMessage = buildLockErrorMessage("illegal state", name, token, expiry, timeout);
                REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage, e);
                throw new IllegalStateException(errorMessage, e);
            } catch (Exception e) {
                monitor.onError(RedisLockClientMonitorFactory.ERROR_CODE_UNEXPECTED_ERROR);
                String errorMessage = buildLockErrorMessage("unexpected error", name, token, expiry, timeout);
                REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage, e);
                throw new RedisException(RedisException.CODE_UNEXPECTED_ERROR, errorMessage, e);
            } finally {
                state = BeanStatusEnum.CLOSED;
                monitor.onExecuted(startNanoTime);
            }
        } else {
            String errorMessage = buildLockErrorMessage("FutureLock has been closed", name, token, expiry, timeout);
            REDIS_DISTRIBUTED_LOCK_LOG.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    @Override
    public String toString() {
        return "FutureLock{" +
                "startNanoTime=" + startNanoTime +
                ", name='" + name + '\'' +
                ", token='" + token + '\'' +
                ", expiry=" + expiry +
                ", command=" + command +
                ", channel=" + channel +
                ", monitor=" + monitor +
                ", state=" + state +
                '}';
    }

    /**
     * 构造锁获取失败错误日志。
     *
     * @param reason 失败原因
     * @param name 锁名称
     * @param token 锁对应的 token 值
     * @param expiry 锁的过期时间，单位：秒
     * @param timeout 获取锁的超时时间，单位：毫秒
     * @return 锁获取失败错误日志
     */
    private String buildLockErrorMessage(String reason, String name, String token, int expiry, long timeout) {
        LinkedHashMap<String, Object> parameterMap = new LinkedHashMap<>();
        parameterMap.put("host", channel.getHost());
        parameterMap.put("name", name);
        parameterMap.put("token", token);
        parameterMap.put("expiry", expiry);
        parameterMap.put("timeout", timeout);
        return  "Acquire redis distributed lock failed: `" + reason + "`." + LogBuildUtil.build(parameterMap);
    }
}
