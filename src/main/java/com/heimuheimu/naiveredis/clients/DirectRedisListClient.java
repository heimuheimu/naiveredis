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

import com.heimuheimu.naiveredis.NaiveRedisListClient;
import com.heimuheimu.naiveredis.channel.RedisChannel;
import com.heimuheimu.naiveredis.command.list.*;
import com.heimuheimu.naiveredis.data.RedisDataParser;
import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;
import com.heimuheimu.naiveredis.facility.parameter.MethodParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;

import java.util.Collection;
import java.util.List;

/**
 * Redis LIST 直连客户端。
 *
 * @author heimuheimu
 */
public class DirectRedisListClient extends AbstractDirectRedisClient implements NaiveRedisListClient {

    /**
     * 构造一个 Redis LIST 直连客户端。
     *
     * @param channel 与 Redis 服务进行数据交互的管道
     * @param timeout Redis 操作超时时间，单位：毫秒，不能小于等于 0
     * @param slowExecutionThreshold 执行 Redis 命令过慢最小时间，单位：纳秒，不能小于等于 0
     */
    public DirectRedisListClient(RedisChannel channel, int timeout, long slowExecutionThreshold) {
        super(channel, timeout, slowExecutionThreshold);
    }

    @Override
    public int addFirstToList(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return addFirstToList(key, member, true);
    }

    @Override
    public int addFirstToList(String key, String member, boolean isAutoCreate) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "addFirstToList(String key, String member, boolean isAutoCreate)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("member", member);
        parameterChecker.addParameter("isAutoCreate", isAutoCreate);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> {
            if (isAutoCreate) {
                return new LPushCommand(key, member);
            } else {
                return new LPushXCommand(key, member);
            }
        }, RedisDataParser::parseInt);
    }

    @Override
    public int addFirstToList(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        if (members == null || members.isEmpty()) {
            return 0;
        }
        String methodName = methodNamePrefix + "addFirstToList(String key, Collection<String> members)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("members", members);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new LPushCommand(key, members), RedisDataParser::parseInt);
    }

    @Override
    public int addLastToList(String key, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        return addLastToList(key, member, true);
    }

    @Override
    public int addLastToList(String key, String member, boolean isAutoCreate) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "addLastToList(String key, String member, boolean isAutoCreate)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("member", member);
        parameterChecker.addParameter("isAutoCreate", isAutoCreate);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> {
            if (isAutoCreate) {
                return new RPushCommand(key, member);
            } else {
                return new RPushXCommand(key, member);
            }
        }, RedisDataParser::parseInt);
    }

    @Override
    public int addLastToList(String key, Collection<String> members) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        if (members == null || members.isEmpty()) {
            return 0;
        }
        String methodName = methodNamePrefix + "addLastToList(String key, Collection<String> members)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("members", members);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new RPushCommand(key, members), RedisDataParser::parseInt);
    }

    @Override
    public String popFirstFromList(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "popFirstFromList(String key)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (String) execute(methodName, parameterChecker.getParameterMap(), () -> new LPopCommand(key), RedisDataParser::parseString);
    }

    @Override
    public String popLastFromList(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "popLastFromList(String key)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (String) execute(methodName, parameterChecker.getParameterMap(), () -> new RPopCommand(key), RedisDataParser::parseString);
    }

    @Override
    public int insertIntoList(String key, String pivotalMember, String member, boolean isAfter) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "insertIntoList(String key, String pivotalMember, String member, boolean isAfter)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("pivotalMember", pivotalMember);
        parameterChecker.addParameter("member", member);
        parameterChecker.addParameter("isAfter", isAfter);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("pivotalMember", "isNull", Parameters::isNull);
        parameterChecker.check("member", "isNull", Parameters::isNull);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new LInsertCommand(key, pivotalMember, member, isAfter), RedisDataParser::parseInt);
    }

    @Override
    public void setToList(String key, int index, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "setToList(String key, int index, String member)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("index", index);
        parameterChecker.addParameter("member", member);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);

        execute(methodName, parameterChecker.getParameterMap(), () -> new LSetCommand(key, index, member), null);
    }

    @Override
    public int removeFromList(String key, int count, String member) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "removeFromList(String key, int count, String member)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("count", count);
        parameterChecker.addParameter("member", member);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);
        parameterChecker.check("member", "isNull", Parameters::isNull);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new LRemCommand(key, count, member), RedisDataParser::parseInt);
    }

    @Override
    public void trimList(String key, int startIndex, int endIndex) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "trimList(String key, int startIndex, int endIndex)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("startIndex", startIndex);
        parameterChecker.addParameter("endIndex", endIndex);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        execute(methodName, parameterChecker.getParameterMap(), () -> new LTrimCommand(key, startIndex, endIndex), null);
    }

    @Override
    public int getSizeOfList(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getSizeOfList(String key)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (int) execute(methodName, parameterChecker.getParameterMap(), () -> new LLenCommand(key), RedisDataParser::parseInt);
    }

    @Override
    public String getByIndexFromList(String key, int index) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getByIndexFromList(String key, int index)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("index", index);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (String) execute(methodName, parameterChecker.getParameterMap(), () -> new LIndexCommand(key, index), RedisDataParser::parseString);
    }

    @Override
    public List<String> getMembersFromList(String key, int startIndex, int endIndex) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException {
        String methodName = methodNamePrefix + "getMembersFromList(String key, int startIndex, int endIndex)";
        MethodParameterChecker parameterChecker = buildRedisCommandMethodParameterChecker(methodName);
        parameterChecker.addParameter("key", key);
        parameterChecker.addParameter("startIndex", startIndex);
        parameterChecker.addParameter("endIndex", endIndex);

        parameterChecker.check("key", "isEmpty", Parameters::isEmpty);

        return (List<String>) execute(methodName, parameterChecker.getParameterMap(), () -> new LRangeCommand(key, startIndex, endIndex), RedisDataParser::parseStringList);
    }
}
