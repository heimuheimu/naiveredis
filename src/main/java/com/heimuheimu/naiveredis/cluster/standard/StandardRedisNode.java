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

package com.heimuheimu.naiveredis.cluster.standard;

import com.heimuheimu.naiveredis.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis 集群节点信息，每个节点负责指定区间内的 hash slot 值。
 *
 * <p><strong>说明：</strong>StandardRedisNode 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class StandardRedisNode {

    /**
     * 该节点负责的开始 hash slot 值（包含）
     */
    private final int startSlot;

    /**
     * 该节点负责的结束 hash slot 值（包含）
     */
    private final int endSlot;

    /**
     * 该节点对应的 Redis master 地址
     */
    private final String masterHost;

    /**
     * 该节点对应的 Redis slave 地址数组
     */
    private final String[] slaveHosts;

    /**
     * 记录已获取 Redis slave 地址的次数，用于做负载均衡
     */
    private final AtomicLong count = new AtomicLong(0);

    /**
     * 构建一个 StandardRedisNode 实例。
     *
     * @param startSlot 该节点负责的开始 hash slot 值（包含）
     * @param endSlot 该节点负责的结束 hash slot 值（包含）
     * @param masterHost 该节点对应的 Redis master 地址，不允许为 {@code null} 或空
     * @param slaveHosts 该节点对应的 Redis slave 地址数组，允许为 {@code null}
     * @throws IllegalArgumentException 如果 masterHost 为 {@code null} 或空，将会抛出此异常
     */
    public StandardRedisNode(int startSlot, int endSlot, String masterHost, String[] slaveHosts) throws IllegalArgumentException {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("StandardRedisNode", null);
        checker.addParameter("startSlot", startSlot);
        checker.addParameter("endSlot", endSlot);
        checker.addParameter("masterHost", masterHost);
        checker.addParameter("slaveHosts", slaveHosts);

        checker.check("masterHost", "isEmpty", Parameters::isEmpty);

        this.startSlot = startSlot;
        this.endSlot = endSlot;
        this.masterHost = masterHost;
        this.slaveHosts = slaveHosts == null ? new String[0] : slaveHosts;
    }

    /**
     * 获得该节点负责的开始 hash slot 值（包含）。
     *
     * @return 该节点负责的开始 hash slot 值（包含）
     */
    public int getStartSlot() {
        return startSlot;
    }

    /**
     * 获得该节点负责的结束 hash slot 值（包含）。
     *
     * @return 该节点负责的结束 hash slot 值（包含）
     */
    public int getEndSlot() {
        return endSlot;
    }

    /**
     * 获得该节点对应的 Redis master 地址。
     *
     * @return 该节点对应的 Redis master 地址
     */
    public String getMasterHost() {
        return masterHost;
    }

    /**
     * 获得该节点对应的 Redis slave 地址数组，不会返回 {@code null}，可能为空数组。
     *
     * @return 该节点对应的 Redis slave 地址数组，不会为 {@code null}
     */
    public String[] getSlaveHosts() {
        return slaveHosts;
    }

    /**
     * 获得下一个 Redis slave 地址，如果该节点 slave 地址数组为空，将会返回 {@code null}。
     *
     * @return 下一个 Redis slave 地址
     */
    public String getNextSlaveHost() {
        if (slaveHosts.length > 1) {
            int slaveHostIndex = (int) (Math.abs(count.getAndIncrement()) % slaveHosts.length);
            return slaveHosts[slaveHostIndex];
        } else if (slaveHosts.length == 1) {
            return slaveHosts[0];
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "StandardRedisNode{" +
                "startSlot=" + startSlot +
                ", endSlot=" + endSlot +
                ", masterHost='" + masterHost + '\'' +
                ", slaveHosts=" + Arrays.toString(slaveHosts) +
                '}';
    }
}
