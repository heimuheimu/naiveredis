package com.heimuheimu.naiveredis;

import com.heimuheimu.naiveredis.exception.RedisException;
import com.heimuheimu.naiveredis.exception.TimeoutException;

import java.util.Map;
import java.util.Set;

/**
 * Redis 字符串存储客户端，提供设置、获取字符串的方法，字符串不会被序列化，所有实现类需保证字符串为 UTF-8 编码，
 * 该客户端适用于多种 Redis 客户端共同操作 Redis 的场景。
 *
 * <p><strong>说明：</strong>{@code NaiveRedisRawStorageClient} 的实现类必须是线程安全的。</p>
 *
 * @author heimuheimu
 * @see NaiveRedisStorageClient
 */
public interface NaiveRedisRawStorageClient extends NaiveRedisKeysClient {

    /**
     * 获取 Key 对应的字符串，如果 Key 不存在，则返回 {@code null}。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/get">GET key</a></p>
     *
     * @param key Redis key，不允许 {@code null} 或空
     * @return Key 对应的字符串
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    String getString(String key) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 根据 Key 列表批量获取在 Redis 中存储的字符串，找到的 Key 将会把对应的 Key 和结果放入 Map 中，未找到或发生异常的 Key 不会出现在返回 Map 中，
     * 如果 keySet 为 {@code null} 或空列表，将返回空 Map，该方法不会返回 {@code null}。
     *
     * <p><strong>算法复杂度：</strong> O(N), N 为获取的 key 数量</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/mget">MGET key [key ...]</a></p>
     *
     * @param keySet Key 列表，列表中不允许包含为 {@code null} 或空字符串的 Key
     * @return Key 列表对应的字符串 Map，不会为 {@code null}
     * @throws IllegalArgumentException 如果 keySet 中包含 {@code null} 或空字符串的 Key，将抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    Map<String, String> multiGetString(Set<String> keySet) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将 Key 和字符串存储至 Redis 中，永久保存，如果 Key 已存在，原来的值将会被覆盖，字符串不会被序列化。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/set">SET key value</a></p>
     *
     * @param key Redis key，不允许 {@code null} 或空
     * @param value 字符串，不允许 {@code null}
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 value 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    void setString(String key, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 将 Key 和字符串存储至 Redis 中，并指定过期时间，如果 Key 已存在，原来的值将会被覆盖，字符串不会被序列化。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/set">SET key value EX seconds</a></p>
     *
     * @param key Redis key，不允许 {@code null} 或空
     * @param value 字符串，不允许 {@code null}
     * @param expiry 过期时间，单位：秒，如果小于等于 0，则为永久保存
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 value 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    void setString(String key, String value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 仅在 Key 不存在时，将字符串存储至 Redis 中，永久保存，并返回 {@code true}，如果 Key 已存在，不会替换原来的值，并返回 {@code false}。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/setnx">SETNX key value</a></p>
     *
     * @param key Redis key，不允许 {@code null} 或空
     * @param value 字符串，不允许 {@code null}
     * @return 是否保存成功
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 value 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    boolean setStringIfAbsent(String key, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 仅在 Key 不存在时，将字符串存储至 Redis 中，并指定过期时间，返回 {@code true}，如果 Key 已存在，不会替换原来的值，
     * 并返回 {@code false}。
     *
     * <p><strong>说明：</strong>Redis SETNX 不支持过期时间设置，如设置了过期时间，在保存成功后调用 {@link #expire(String, int)} 实现。</p>
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/setnx">SETNX key value</a></p>
     *
     * @param key Redis key，不允许 {@code null} 或空
     * @param value 字符串，不允许 {@code null}
     * @param expiry 过期时间，单位：秒，如果小于等于 0，则为永久保存
     * @return 是否保存成功
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 value 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    boolean setStringIfAbsent(String key, String value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 仅在 Key 存在时，执行更新操作，永久保存，并返回 {@code true}，如果 Key 不存在，不执行任何操作，并返回 {@code false}。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/set">SET key value XX</a></p>
     *
     * @param key Redis key，不允许 {@code null} 或空
     * @param value 字符串，不允许 {@code null}
     * @return 是否更新成功
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 value 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    boolean setStringIfExist(String key, String value) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;

    /**
     * 仅在 Key 存在时，执行更新操作，并指定过期时间，返回 {@code true}，如果 Key 不存在，不执行任何操作，并返回 {@code false}。
     *
     * <p><strong>算法复杂度：</strong> O(1)</p>
     *
     * <p><strong>Redis 命令：</strong><a href="https://redis.io/commands/set">SET key value EX seconds XX</a></p>
     *
     * @param key Redis key，不允许 {@code null} 或空
     * @param value 字符串，不允许 {@code null}
     * @param expiry 过期时间，单位：秒，如果小于等于 0，则为永久保存
     * @return 是否更新成功
     * @throws IllegalArgumentException 如果 key 为 {@code null} 或空，将会抛出此异常
     * @throws IllegalArgumentException 如果 value 为 {@code null}，将会抛出此异常
     * @throws IllegalStateException 如果 Redis 服务不可用，将会抛出此异常
     * @throws TimeoutException 如果操作超时，将会抛出此异常
     * @throws RedisException 如果 Redis 命令执行出错，将会抛出此异常
     */
    boolean setStringIfExist(String key, String value, int expiry) throws IllegalArgumentException, IllegalStateException, TimeoutException, RedisException;
}
