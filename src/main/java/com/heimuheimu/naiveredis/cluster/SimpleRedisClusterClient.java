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

import com.heimuheimu.naiveredis.DirectRedisClient;
import com.heimuheimu.naiveredis.constant.RedisClientMethod;
import com.heimuheimu.naiveredis.facility.ConsistentHashLocator;
import com.heimuheimu.naiveredis.facility.clients.DirectRedisClientList;
import com.heimuheimu.naiveredis.facility.clients.DirectRedisClientListListener;
import com.heimuheimu.naiveredis.net.SocketConfiguration;
import com.heimuheimu.naiveredis.util.LogBuildUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

/**
 * 一个简单的 Redis 集群客户端，连接多个 Redis 服务，根据 Key 进行 Hash 选择。
 *
 * <p><strong>说明：</strong>{@code SimpleRedisClusterClient} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class SimpleRedisClusterClient extends AbstractRedisClusterClient {

    /**
     * Redis 命令执行错误日志
     */
    private static final Logger NAIVEREDIS_ERROR_LOG = LoggerFactory.getLogger("NAIVEREDIS_ERROR_LOG");

    /**
     * Redis 地址数组
     */
    private final String[] hosts;

    /**
     * Redis 直连客户端列表
     */
    private final DirectRedisClientList directRedisClientList;

    /**
     * Redis 客户端定位器
     */
    private final ConsistentHashLocator consistentHashLocator = new ConsistentHashLocator();

    /**
     * 构造一个简单的 Redis 集群客户端，连接多个 Redis 服务，根据 Key 进行 Hash 选择。
     *
     * @param hosts Redis 地址数组，不允许为 {@code null} 或空数组
     * @param configuration 创建 Redis 直连客户端所使用的 Socket 配置信息，允许为 {@code null}
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param compressionThreshold 最小压缩字节数，当 Value 字节数小于或等于该值，不进行压缩，不能小于等于 0
     * @param slowExecutionThreshold Redis 操作过慢最小时间，单位：毫秒，不能小于等于 0
     * @param pingPeriod PING 命令发送时间间隔，单位：秒，用于心跳检测，如果该值小于等于 0，则不进行心跳检测
     * @param listener Redis 直连客户端列表事件监听器，允许为 {@code null}
     */
    public SimpleRedisClusterClient(String[] hosts, SocketConfiguration configuration, int timeout, int compressionThreshold,
                                    int slowExecutionThreshold, int pingPeriod, DirectRedisClientListListener listener) {
        this.hosts = hosts;
        this.directRedisClientList = new DirectRedisClientList("SimpleRedisClusterClient", hosts, configuration,
                timeout, compressionThreshold, slowExecutionThreshold, pingPeriod, listener);
    }

    @Override
    public void close() {
        directRedisClientList.close();
    }

    @Override
    public String toString() {
        return "SimpleRedisClusterClient{" +
                "hosts=" + Arrays.toString(hosts) +
                ", directRedisClientList=" + directRedisClientList +
                '}';
    }

    @Override
    protected DirectRedisClient getClient(RedisClientMethod method, Map<String, Object> parameterMap) {
        String key = (String) parameterMap.get("key");
        int clientIndex = consistentHashLocator.getIndex(key, hosts.length);
        DirectRedisClient client = directRedisClientList.get(clientIndex);
        if (client == null || !client.isAvailable()) {
            parameterMap.put("clientIndex", clientIndex);
            String errorMessage = LogBuildUtil.buildMethodExecuteFailedLog("SimpleRedisClusterClient" + method.getMethodName(),
                    "no available client", parameterMap);
            NAIVEREDIS_ERROR_LOG.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
        return client;
    }
}
