package io.github.SilenceShine.shine.cloud.id.runner;

import io.github.SilenceShine.shine.cloud.id.util.SnowflakeIdUtil;
import io.github.SilenceShine.shine.util.log.LogUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 初始化雪花算法的 workerId, datacenterId
 *
 * @author SilenceShine
 * @since 1.0
 */
@Slf4j
@Component
public class SnowflakeIdRunner implements CommandLineRunner {

    private static final int ID_MAX = 31;
    private static final String WORK_ID_LOCK_NAME = "work-id";
    private static final String DATA_CENTER_ID_LOCK_NAME = "data-center-id";
    private static final long WAIT_TIME = 1L;
    private static final long LEASE_TIME = 60;
    private static final TimeUnit UNIT = TimeUnit.SECONDS;
    private Integer workId;
    private Integer dataCenterId;
    private final Object object = new Object();
    private final Object runnerObject = new Object();

    @Value("${spring.application.name}")
    private String name;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void run(String... args) throws Exception {
        new Thread(() -> {
            synchronized (object) {
                RLock workIdLock = getLock(WORK_ID_LOCK_NAME, id -> workId = id);
                RLock dataCenterIdLock = getLock(DATA_CENTER_ID_LOCK_NAME, id -> dataCenterId = id);
                LogUtil.error(SnowflakeIdRunner.class, "获取id成功:{},{}", workId, dataCenterId);
                try {
                    synchronized (runnerObject) {
                        runnerObject.notify();
                    }
                    object.wait();
                } catch (InterruptedException e) {
                    LogUtil.error(SnowflakeIdRunner.class, "线程等待异常:{}", e.getMessage());
                    System.exit(-1);
                }
                LogUtil.info(SnowflakeIdRunner.class, "释放雪花算法 workId:{},dataCenterId:{}", workId, dataCenterId);
                workIdLock.unlock();
                dataCenterIdLock.unlock();
            }
        }).start();
        synchronized (runnerObject) {
            runnerObject.wait();
        }
        SnowflakeIdUtil.init(workId, dataCenterId);
        LogUtil.info(this, "雪花算法初始化完成 workId:{},dataCenterId:{}", workId, dataCenterId);
    }

    @PreDestroy
    public void preDestroy() {
        synchronized (object) {
            object.notify();
        }
    }

    private RLock getLock(String lockName, Consumer<Integer> consumer) {
        for (int id = 0; id < ID_MAX; id++) {
            RLock rLock = getLock(name + ":" + lockName + ":" + id);
            if (null != rLock) {
                consumer.accept(id);
                return rLock;
            }
        }
        LogUtil.error(SnowflakeIdRunner.class, "get id error, {}个所有id已经占完!", ID_MAX);
        System.exit(-1);
        return null;
    }

    private RLock getLock(String lockName) {
        RLock rLock = redissonClient.getLock(lockName);
        try {
            if (rLock.tryLock(WAIT_TIME, 10L * LEASE_TIME, UNIT)) {
                return rLock;
            }
        } catch (InterruptedException e) {
            rLock.unlock();
            throw new RuntimeException(e);
        }
        return null;
    }

}
