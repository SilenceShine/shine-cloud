package io.github.SilenceShine.shine.cloud.id.runner;

import io.github.SilenceShine.shine.cloud.id.util.SnowflakeIdUtil;
import io.github.SilenceShine.shine.util.log.LogUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * 初始化雪花算法的 workerId, datacenterId
 *
 * @author SilenceShine
 * @since 1.0
 */
@Slf4j
public class SnowflakeIdRunnerSync implements CommandLineRunner {

    private static final int ID_MAX = 31;
    private static final String REDISSON_KEY_PREFIX = "snowflake";
    private static final String WORK_ID_LOCK_NAME = "work-id";
    private static final String DATA_CENTER_ID_LOCK_NAME = "data-center-id";
    private Integer workId;
    private Integer dataCenterId;
    private final Object object = new Object();
    private final Object runnerObject = new Object();

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void run(String... args) throws Exception {
        new Thread(() -> {
            synchronized (object) {
                RLock workIdLock = getLock(WORK_ID_LOCK_NAME, id -> workId = id);
                RLock dataCenterIdLock = getLock(DATA_CENTER_ID_LOCK_NAME, id -> dataCenterId = id);
                LogUtil.info(SnowflakeIdRunnerSync.class, "获取id成功:{},{}", workId, dataCenterId);
                try {
                    synchronized (runnerObject) {
                        runnerObject.notify();
                    }
                    object.wait();
                } catch (InterruptedException e) {
                    LogUtil.error(SnowflakeIdRunnerSync.class, "线程等待异常:{}", e.getMessage());
                    System.exit(-1);
                }
                LogUtil.info(SnowflakeIdRunnerSync.class, "释放雪花算法 workId:{},dataCenterId:{}", workId, dataCenterId);
                workIdLock.unlock();
                dataCenterIdLock.unlock();
                synchronized (runnerObject) {
                    runnerObject.notify();
                }
            }
        }).start();
        synchronized (runnerObject) {
            runnerObject.wait();
        }
        SnowflakeIdUtil.init(workId, dataCenterId);
        LogUtil.info(this, "雪花算法初始化完成 workId:{},dataCenterId:{}", workId, dataCenterId);
    }

    @PreDestroy
    public void preDestroy() throws InterruptedException {
        synchronized (object) {
            object.notify();
        }
        synchronized (runnerObject) {
            runnerObject.wait();
        }
    }

    private RLock getLock(String lockName, Consumer<Integer> consumer) {
        return IntStream.range(0, ID_MAX)
                .boxed()
                .peek(consumer)
                .map(id -> redissonClient.getLock(REDISSON_KEY_PREFIX + ":" + lockName + ":" + id))
                .filter(Lock::tryLock)
                .findAny()
                .orElseThrow(() -> new RuntimeException("get id error, " + ID_MAX + " 个所有id已经占完!"));
    }

}
