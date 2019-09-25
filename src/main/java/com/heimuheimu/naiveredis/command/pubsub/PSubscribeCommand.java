package com.heimuheimu.naiveredis.command.pubsub;

import com.heimuheimu.naiveredis.command.AbstractCommand;
import com.heimuheimu.naiveredis.data.RedisArray;
import com.heimuheimu.naiveredis.data.RedisBulkString;
import com.heimuheimu.naiveredis.data.RedisData;
import com.heimuheimu.naiveredis.facility.parameter.ConstructorParameterChecker;
import com.heimuheimu.naiveredis.facility.parameter.Parameters;
import com.heimuheimu.naiveredis.util.LogBuildUtil;

import java.util.Set;

/**
 * Redis PSUBSCRIBE 命令。命令定义请参考文档：
 * <a href="https://redis.io/commands/psubscribe">https://redis.io/commands/psubscribe</a>
 *
 * <p><strong>说明：</strong>{@code PSubscribeCommand} 类是线程安全的，可在多个线程中使用同一个实例。</p>
 *
 * @author heimuheimu
 */
public class PSubscribeCommand extends AbstractCommand {

    private final byte[] requestByteArray;

    /**
     * 构造一个 Redis PSUBSCRIBE 命令。
     *
     * @param patternSet Redis pattern 列表，不允许为 {@code null} 或空，也不允许包含为 {@code null} 或空字符串的 pattern
     * @throws IllegalArgumentException 如果 Redis pattern 列表为 {@code null} 或空，或者列表中包含 {@code null} 或空字符串的 pattern，将抛出此异常
     */
    public PSubscribeCommand(Set<String> patternSet) throws IllegalArgumentException {
        ConstructorParameterChecker checker = new ConstructorParameterChecker("PSubscribeCommand", null);
        checker.addParameter("patternSet", patternSet);

        checker.check("patternSet", "isEmpty", Parameters::isEmpty);

        int arrayIndex = 0;
        RedisData[] commandDataArray = new RedisData[1 + patternSet.size()];
        commandDataArray[arrayIndex++] = new RedisBulkString("PSUBSCRIBE".getBytes(RedisData.UTF8));

        for(String pattern : patternSet) {
            if (pattern == null || pattern.isEmpty()) {
                throw new IllegalArgumentException("Create `PSubscribeCommand` failed: `patternSet could not contain empty pattern`." +
                        LogBuildUtil.build(checker.getParameterMap()));
            }
            commandDataArray[arrayIndex++] = new RedisBulkString(pattern.getBytes(RedisData.UTF8));
        }

        this.requestByteArray = new RedisArray(commandDataArray).getRespByteArray();
    }

    @Override
    public byte[] getRequestByteArray() {
        return requestByteArray;
    }
}
