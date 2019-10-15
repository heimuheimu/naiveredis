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

package com.heimuheimu.naiveredis.pubsub;

import com.heimuheimu.naivemonitor.alarm.NaiveServiceAlarm;
import com.heimuheimu.naivemonitor.alarm.ServiceAlarmMessageNotifier;
import com.heimuheimu.naivemonitor.alarm.ServiceContext;
import com.heimuheimu.naivemonitor.util.MonitorUtil;

import java.util.List;
import java.util.Map;

/**
 * 该监听器可用于当 Redis 消息发布客户端发生服务不可用或者从不可用状态恢复时，进行实时通知。
 *
 * <p><strong>说明：</strong>NoticeableAutoReconnectRedisPublishClientListener 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 * @see NaiveServiceAlarm
 */
public class NoticeableAutoReconnectRedisPublishClientListener implements AutoReconnectRedisPublishClientListener {

    /**
     * 使用 Redis 消息发布服务的项目名称
     */
    private final String project;

    /**
     * 使用 Redis 消息发布服务的主机名称
     */
    private final String host;

    /**
     * 服务不可用报警器
     */
    private final NaiveServiceAlarm naiveServiceAlarm;

    /**
     * 构造一个 NoticeableAutoReconnectRedisPublishClientListener 实例。
     *
     * @param project 使用 Redis 消息发布服务的项目名称
     * @param notifierList 服务不可用或从不可用状态恢复的报警消息通知器列表，不允许 {@code null} 或空
     * @throws IllegalArgumentException 如果 notifierList 为 {@code null} 或空时，抛出此异常
     */
    public NoticeableAutoReconnectRedisPublishClientListener(String project, List<ServiceAlarmMessageNotifier> notifierList)
            throws IllegalArgumentException {
        this(project, notifierList, null);
    }

    /**
     * 构造一个 NoticeableAutoReconnectRedisPublishClientListener 实例。
     *
     * @param project 使用 Redis 消息发布服务的项目名称
     * @param notifierList 服务不可用或从不可用状态恢复的报警消息通知器列表，不允许 {@code null} 或空
     * @param hostAliasMap 别名 Map，Key 为机器名， Value 为别名，允许为 {@code null}
     * @throws IllegalArgumentException 如果 notifierList 为 {@code null} 或空时，抛出此异常
     */
    public NoticeableAutoReconnectRedisPublishClientListener(String project, List<ServiceAlarmMessageNotifier> notifierList,
                                                               Map<String, String> hostAliasMap) throws IllegalArgumentException {
        this.project = project;
        this.naiveServiceAlarm = new NaiveServiceAlarm(notifierList);
        String host = MonitorUtil.getLocalHostName();
        if (hostAliasMap != null && hostAliasMap.containsKey(host)) {
            this.host = hostAliasMap.get(host);
        } else {
            this.host = host;
        }
    }


    @Override
    public void onCreated(String host) {
        //do nothing
    }

    @Override
    public void onRecovered(String host) {
        naiveServiceAlarm.onRecovered(getServiceContext(host));
    }

    @Override
    public void onClosed(String host) {
        naiveServiceAlarm.onCrashed(getServiceContext(host));
    }

    protected ServiceContext getServiceContext(String redisHost) {
        ServiceContext serviceContext = new ServiceContext();
        serviceContext.setName("Redis 消息发布");
        serviceContext.setHost(host);
        serviceContext.setProject(project);
        serviceContext.setRemoteHost(redisHost);
        return serviceContext;
    }
}
