package com.heimuheimu.naiveredis.pubsub;

import com.heimuheimu.naiveredis.TestRedisProvider;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@link RedisSubscribeClient} 单元测试类。
 *
 * <p><strong>注意：</strong>如果没有提供测试 Redis 地址，即 {@link TestRedisProvider#getRedisHost()} 返回空或 {@code null}，测试用例不会执行。</p>
 */
public class TestRedisSubscribeClient {

    private static String HOST;

    @BeforeClass
    public static void init() {
        String redisHost = TestRedisProvider.getRedisHost();
        if (redisHost == null || redisHost.isEmpty()) {
            Assume.assumeTrue("TestRedisSubscribeClient will be ignored: `empty redis host`.", false);
        } else {
            HOST = redisHost;
        }
    }

    @Test
    public void testConstructor() {
        try {
            new RedisSubscribeClient(HOST, null, 30, null, 50,
                    null, null, null);
            Assert.fail("Expected throw `IllegalArgumentException`.");
        } catch (IllegalArgumentException ignored) {}
        List<NaiveRedisChannelSubscriber> channelSubscriberList = new ArrayList<>();
        channelSubscriberList.add(new DummyChannelSubscriber("demo"));
        List<NaiveRedisPatternSubscriber> patternSubscriberList = new ArrayList<>();
        patternSubscriberList.add(new DummyPatternSubscriber(("d*")));
        try {
            new RedisSubscribeClient("impossible.redis.host:6179", null, 5, null, 50,
                    channelSubscriberList, patternSubscriberList, null);
        } catch (IllegalStateException ignored) {}
        RedisSubscribeClient client = new RedisSubscribeClient(HOST, null, 5, null, 50,
                channelSubscriberList, patternSubscriberList, null);
        client.init();
        client.close();
    }

    private static class DummyChannelSubscriber implements NaiveRedisChannelSubscriber {

        private String[] channels;

        public DummyChannelSubscriber(String... channels) {
            this.channels = channels;
        }

        @Override
        public List<String> getChannelList() {
            return Arrays.asList(channels);
        }

        @Override
        public <T> void consume(String channel, T message) {
            // do nothing
        }
    }

    private static class DummyPatternSubscriber implements NaiveRedisPatternSubscriber {

        private String[] patterns;

        public DummyPatternSubscriber(String... patterns) {
            this.patterns = patterns;
        }

        @Override
        public List<String> getPatternList() {
            return Arrays.asList(patterns);
        }

        @Override
        public <T> void consume(String pattern, String channel, T message) {

        }
    }
}
