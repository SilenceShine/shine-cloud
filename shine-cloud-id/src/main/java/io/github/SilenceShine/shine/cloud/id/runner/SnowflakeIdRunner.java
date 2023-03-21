package io.github.SilenceShine.shine.cloud.id.runner;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 初始化雪花算法的 workerId, datacenterId
 *
 * @author SilenceShine
 * @since 1.0
 */
@Slf4j
@Component
public class SnowflakeIdRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        IdUtil.getSnowflake(1, 1);
    }

}
