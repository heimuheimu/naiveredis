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

package com.heimuheimu.naiveredis.clients;

import com.heimuheimu.naiveredis.NaiveRedisSetClient;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.command.set.*;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.facility.parameter.MethodParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Redis SET 直连客户端。
 *
 * @author heimuheimu
 */
public class DirectRedisSetClient extends AbstractDirectRedisClient implements NaiveRedisSetClient {

    /**
     * 构造一个 Redis SET 直连客户端。
     *
     * @param channel 与 Redis 服务进行数据交互的管道
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param slowExecutionThreshold 执行 Redis 命令过慢最小时间，单位：纳秒，不能小于等于 0
     */
    public DirectRedisSetClient(RedisChannel channel, int timeout, long slowExecutionThreshold) {
        super(channel, timeout, slowExecutionThreshold);
    }

    @Override
    public int addToSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return addToSet(key, Collections.singleton(member));
    }

    @Override
    @SuppressWarnings("unchecked")
    public int addToSet(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        if (members == null || members.isEmpty()) {
            return 0;
        }

        String methodName = methodNamePrefix + "addToSet(String key, Collection<String> members)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("members", members);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("members", "members could not contain null member", parameterValue -> {
            for (String member : (Collection<String>) parameterValue) {
                if (member == null) {
                    return true;
                }
            }
            return false;
        });

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new SAddCommand(key, members),
                response -> Integer.valueOf(response.getText()));
    }

    @Override
    public void removeFromSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "removeFromSet(String key, String member)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("member", member);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);

        execute(methodName, parameterChecker.getParameterMap(), () -> new SRemCommand(key, Collections.singleton(member)), null);
    }

    @Override
    public boolean isMemberInSet(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "isMemberInSet(String key, String member)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("member", member);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);

        return (boolean) execute(methodName, parameterChecker.getParameterMap(), () -> new SIsMemberCommand(key, member),
                response -> Integer.valueOf(response.getText()) == 1);
    }

    @Override
    public int getSizeOfSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getSizeOfSet(String key)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new SCardCommand(key),
                response -> Integer.valueOf(response.getText()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getMembersFromSet(String key, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getMembersFromSet(String key, int count)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("count", count);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (List<String>) execute(methodName, parameterChecker.getParameterMap(), () -> new SRandMemberCommand(key, count), response -> {
            List<String> members = new ArrayList<>();
            for (int i = 0; i < response.size(); i++) {
                members.add(response.get(i).getText());
            }
            return members;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> popMembersFromSet(String key, int count) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "popMembersFromSet(String key, int count)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("count", count);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("count", "isEqualOrLessThanZero", Parameters::isEqualOrLessThanZero);

        return (List<String>) execute(methodName, parameterChecker.getParameterMap(), () -> new SPopCommand(key, count), response -> {
            List<String> members = new ArrayList<>();
            for (int i = 0; i < response.size(); i++) {
                members.add(response.get(i).getText());
            }
            return members;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getAllMembersFromSet(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getAllMembersFromSet(String key)";

        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (List<String>) execute(methodName, parameterChecker.getParameterMap(), () -> new SMembersCommand(key), response -> {
            List<String> members = new ArrayList<>();
            for (int i = 0; i < response.size(); i++) {
                members.add(response.get(i).getText());
            }
            return members;
        });
    }
}
