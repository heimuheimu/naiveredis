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

import com.heimuheimu.naiveredis.NaiveRedisHashesClient;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.command.hashes.*;
import com.heimuheimu.naiveredis.data.RedisDataParser;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.facility.parameter.MethodParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;

import java.util.*;

/**
 * Redis Hashes 直连客户端。
 *
 * @author heimuheimu
 */
public class DirectRedisHashesClient extends AbstractDirectRedisClient implements NaiveRedisHashesClient {

    /**
     * 构造一个 Redis Hashes 直连客户端。
     *
     * @param channel 与 Redis 服务进行数据交互的管道
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param slowExecutionThreshold 执行 Redis 命令过慢最小时间，单位：纳秒，不能小于等于 0
     */
    public DirectRedisHashesClient(RedisChannel channel, int timeout, long slowExecutionThreshold) {
        super(channel, timeout, slowExecutionThreshold);
    }

    @Override
    public int putToHashes(String key, String member, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "putToHashes(String key, String member, String value)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("member", member);
        parameterChecker.addParameter("value", value);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);
        parameterChecker.check("value", "isNull", Parameters::isNull);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new HSetCommand(key, member, value),
                RedisDataParser::parseInt);
    }

    @Override
    public void putToHashes(String key, Map<String, String> memberMap) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        if (memberMap != null && !memberMap.isEmpty()) {
            String methodName = methodNamePrefix + "putToHashes(String key, Map<String, String> memberMap)";
            MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
            parameterChecker.addParameter("key", key);
            parameterChecker.addParameter("memberMap", memberMap);

            parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

            execute(methodName, parameterChecker.getParameterMap(), () -> new HMSetCommand(key, memberMap), null);
        }
    }

    @Override
    public int putIfAbsentToHashes(String key, String member, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "putIfAbsentToHashes(String key, String member, String value)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("member", member);
        parameterChecker.addParameter("value", value);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);
        parameterChecker.check("value", "isNull", Parameters::isNull);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new HSetNXCommand(key, member, value),
                RedisDataParser::parseInt);
    }

    @Override
    public long incrForHashes(String key, String member, long increment) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "incrForHashes(String key, String member, long increment)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("member", member);
        parameterChecker.addParameter("increment", increment);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);

        return (long) execute(methodName, parameterChecker.getParameterMap(), () -> new HIncrByCommand(key, member, increment),
                RedisDataParser::parseLong);
    }

    @Override
    public double incrByFloatForHashes(String key, String member, double increment) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "incrForHashes(String key, String member, long increment)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("member", member);
        parameterChecker.addParameter("increment", increment);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);

        return (double) execute(methodName, parameterChecker.getParameterMap(), () -> new HIncrByFloatCommand(key, member, increment),
                RedisDataParser::parseDouble);
    }

    @Override
    public int removeFromHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return removeFromHashes(key, Collections.singleton(member));
    }

    @Override
    public int removeFromHashes(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        if (members == null || members.size() == 0) {
            return 0;
        }

        String methodName = methodNamePrefix + "removeFromHashes(String key, Collection<String> members)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("members", members);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new HDelCommand(key, members),
                RedisDataParser::parseInt);
    }

    @Override
    public boolean isExistInHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "isExistInHashes(String key, String member)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("member", member);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);

        return (boolean) execute(methodName, parameterChecker.getParameterMap(), () -> new HExistsCommand(key, member),
                RedisDataParser::parseBoolean);
    }

    @Override
    public int getSizeOfHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getSizeOfHashes(String key)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new HLenCommand(key), RedisDataParser::parseInt);
    }

    @Override
    public String getValueFromHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getValueFromHashes(String key, String member)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("member", member);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);

        return (String) execute(methodName, parameterChecker.getParameterMap(), () -> new HGetCommand(key, member),
                RedisDataParser::parseString);
    }

    @Override
    public int getValueLengthFromHashes(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getValueLengthFromHashes(String key, String member)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("member", member);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new HStrLenCommand(key, member),
                RedisDataParser::parseInt);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getMemberMapFromHashes(String key, List<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        if (members == null || members.isEmpty()) {
            return new HashMap<>();
        }
        String methodName = methodNamePrefix + "getMemberMapFromHashes(String key, List<String> members)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("members", members);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (Map<String, String>) execute(methodName, parameterChecker.getParameterMap(), () -> new HMGetCommand(key, members),
                responseData -> {
                    HashMap<String, String> memberMap = new HashMap<>();
                    for (int i = 0; i < members.size(); i++) {
                        String value = responseData.get(i).getText();
                        if (value != null) {
                            memberMap.put(members.get(i), value);
                        }
                    }
                    return memberMap;
                });
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getAllFromHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getAllFromHashes(String key)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (Map<String, String>) execute(methodName, parameterChecker.getParameterMap(), () -> new HGetAllCommand(key),
                RedisDataParser::parseStringMap);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getKeysFromHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getKeysFromHashes(String key)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (List<String>) execute(methodName, parameterChecker.getParameterMap(), () -> new HKeysCommand(key),
                RedisDataParser::parseStringList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getValuesFromHashes(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getValuesFromHashes(String key)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (List<String>) execute(methodName, parameterChecker.getParameterMap(), () -> new HValsCommand(key),
                RedisDataParser::parseStringList);
    }
}
