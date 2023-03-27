package io.github.SilenceShine.shine.cloud.id.runner;

import io.github.SilenceShine.shine.cloud.id.properties.GlobalProperties;
import io.github.SilenceShine.shine.cloud.id.util.SnowflakeIdUtil;
import io.github.SilenceShine.shine.util.log.LogUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * @author Administrator
 */
@Slf4j
@Order(2)
@Component
public class SnowflakeIdRunner implements CommandLineRunner {

    private static final int ID_MAX = 31;
    private static final String REDISSON_KEY_PREFIX = "snowflake";
    private static final String WORK_ID_LOCK_NAME = "work-id";
    private static final String DATA_CENTER_ID_LOCK_NAME = "data-center-id";
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private Integer workId;
    private Integer dataCenterId;
    private Boolean notice = false;
    @Autowired
    private GlobalProperties properties;
    @Autowired
    private RedissonClient redissonClient;

    @PostConstruct
    public void init() {
        workId = properties.getSnowflake().getWorkId();
        dataCenterId = properties.getSnowflake().getDataCenterId();
    }

    @Override
    public void run(String... args) throws Exception {
        if (null == workId || null == dataCenterId) {
            new Thread(() -> {
                notice = true;
                RLock workIdLock = null;
                RLock dataCenterIdLock = null;
                if (null == workId) {
                    workIdLock = getLock(WORK_ID_LOCK_NAME, id -> workId = id);
                }
                if (null == dataCenterId) {
                    dataCenterIdLock = getLock(DATA_CENTER_ID_LOCK_NAME, id -> dataCenterId = id);
                }
                LogUtil.info(SnowflakeIdRunner.class, "Snowflake 获取id workId:{},dataCenterId:{}", workId, dataCenterId);
                lockNotice(true);
                LogUtil.info(SnowflakeIdRunner.class, "Snowflake 释放id workId:{},dataCenterId:{}", workId, dataCenterId);
                if (null != workIdLock) {
                    workIdLock.unlock();
                }
                if (null != dataCenterIdLock) {
                    dataCenterIdLock.unlock();
                }
                lockNotice(true);
            }).start();
            lockNotice(false);
        }
        SnowflakeIdUtil.init(workId, dataCenterId);
        LogUtil.info(this, "Snowflake 初始化完成 workId:{},dataCenterId:{}", workId, dataCenterId);
    }

    @PreDestroy
    public void preDestroy() {
        // 通知redisson释放分布式锁
        if (notice) {
            lockNotice(true);
        }
    }

    /**
     * 通知方法
     */
    private void lockNotice(boolean signal) {
        lock.lock();
        try {
            if (signal) {
                condition.signal();
            }
            condition.await();
        } catch (InterruptedException e) {
            LogUtil.info(this, "Snowflake 初始化失败:{}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取分布式锁
     */
    private RLock getLock(String lockName, Consumer<Integer> consumer) {
        return IntStream.range(0, ID_MAX)
                .boxed()
                .peek(consumer)
                .map(id -> redissonClient.getLock(REDISSON_KEY_PREFIX + ":" + lockName + ":" + id))
                .filter(Lock::tryLock)
                .findAny()
                .orElseThrow(() -> new RuntimeException("Snowflake get id error, " + ID_MAX + " 个所有id已经占完!"));
    }

}
