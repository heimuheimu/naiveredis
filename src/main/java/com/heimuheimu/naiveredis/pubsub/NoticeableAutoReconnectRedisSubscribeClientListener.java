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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 该监听器可用于当 Redis 订阅客户端发生服务不可用或者从不可用状态恢复时，进行实时通知。
 *
 * <p><strong>说明：</strong>NoticeableAutoReconnectRedisSubscribeClientListener 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 * @see NaiveServiceAlarm
 */
public class NoticeableAutoReconnectRedisSubscribeClientListener implements AutoReconnectRedisSubscribeClientListener {

    /**
     * 使用 Redis 订阅服务的项目名称
     */
    private final String project;

    /**
     * 使用 Redis 订阅服务的主机名称
     */
    private final String host;

    /**
     * 服务不可用报警器
     */
    private final NaiveServiceAlarm naiveServiceAlarm;

    /**
     * 构造一个 NoticeableAutoReconnectRedisSubscribeClientListener 实例。
     *
     * @param project 使用 Redis 订阅服务的项目名称
     * @param notifierList 服务不可用或从不可用状态恢复的报警消息通知器列表，不允许 {@code null} 或空
     * @throws IllegalArgumentException 如果 notifierList 为 {@code null} 或空时，抛出此异常
     */
    public NoticeableAutoReconnectRedisSubscribeClientListener(String project , List<ServiceAlarmMessageNotifier> notifierList)
            throws IllegalArgumentException {
        this(project, notifierList, null);
    }

    /**
     * 构造一个 NoticeableAutoReconnectRedisSubscribeClientListener 实例。
     *
     * @param project 使用 Redis 订阅服务的项目名称
     * @param notifierList 服务不可用或从不可用状态恢复的报警消息通知器列表，不允许 {@code null} 或空
     * @param hostAliasMap 别名 Map，Key 为机器名， Value 为别名，允许为 {@code null}
     * @throws IllegalArgumentException 如果 notifierList 为 {@code null} 或空时，抛出此异常
     */
    public NoticeableAutoReconnectRedisSubscribeClientListener(String project, List<ServiceAlarmMessageNotifier> notifierList,
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
    public void onCreated(String host, List<NaiveRedisChannelSubscriber> channelSubscriberList,
                          List<NaiveRedisPatternSubscriber> patternSubscriberList) {
        //do nothing
    }

    @Override
    public void onRecovered(String host, List<NaiveRedisChannelSubscriber> channelSubscriberList,
                            List<NaiveRedisPatternSubscriber> patternSubscriberList) {
        naiveServiceAlarm.onRecovered(getServiceContext(host, channelSubscriberList, patternSubscriberList));
    }

    @Override
    public void onClosed(String host, List<NaiveRedisChannelSubscriber> channelSubscriberList,
                         List<NaiveRedisPatternSubscriber> patternSubscriberList) {
        naiveServiceAlarm.onCrashed(getServiceContext(host, channelSubscriberList, patternSubscriberList));
    }

    protected ServiceContext getServiceContext(String redisHost, List<NaiveRedisChannelSubscriber> channelSubscriberList,
                                               List<NaiveRedisPatternSubscriber> patternSubscriberList) {
        StringBuilder nameBuffer = new StringBuilder("Redis 订阅 [");
        Set<String> channelSet = new LinkedHashSet<>();
        if (channelSubscriberList != null && !channelSubscriberList.isEmpty()) {
            for (NaiveRedisChannelSubscriber channelSubscriber : channelSubscriberList) {
                channelSet.addAll(channelSubscriber.getChannelList());
            }
        }
        if (!channelSet.isEmpty()) {
            nameBuffer.append("channels=");
            for (String channel : channelSet) {
                nameBuffer.append("`").append(channel).append("`, ");
            }
            nameBuffer.delete(nameBuffer.length() - 2, nameBuffer.length());
        }
        Set<String> patternSet = new LinkedHashSet<>();
        if (patternSubscriberList != null && !patternSubscriberList.isEmpty()) {
            for (NaiveRedisPatternSubscriber patternSubscriber : patternSubscriberList) {
                patternSet.addAll(patternSubscriber.getPatternList());
            }
        }
        if (!patternSet.isEmpty()) {
            if (!channelSet.isEmpty()) {
                nameBuffer.append("; ");
            }
            nameBuffer.append("patterns=");
            for (String pattern : patternSet) {
                nameBuffer.append("`").append(pattern).append("`, ");
            }
            nameBuffer.delete(nameBuffer.length() - 2, nameBuffer.length());
        }
        nameBuffer.append("]");
        ServiceContext serviceContext = new ServiceContext();
        serviceContext.setName(nameBuffer.toString());
        serviceContext.setHost(host);
        serviceContext.setProject(project);
        serviceContext.setRemoteHost(redisHost);
        return serviceContext;
    }
}
