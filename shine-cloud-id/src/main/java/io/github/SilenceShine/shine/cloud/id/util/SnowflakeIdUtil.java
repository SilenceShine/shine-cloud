package io.github.SilenceShine.shine.cloud.id.util;

import cn.hutool.core.lang.Singleton;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 雪花算法id
 *
 * @author SilenceShine
 * @since 1.0
 */
public class SnowflakeIdUtil {

    public static void init(long workId, long dataCenterId) {
        Singleton.put(new Snowflake(workId, dataCenterId));
    }

    public static long getSnowflakeId() {
        return IdUtil.getSnowflakeNextId();
    }

    public static List<Long> getSnowflakeIds(Integer size) {
        return IntStream.range(0, size)
                .boxed()
                .map(__ -> getSnowflakeId())
                .collect(Collectors.toList());
    }

}
