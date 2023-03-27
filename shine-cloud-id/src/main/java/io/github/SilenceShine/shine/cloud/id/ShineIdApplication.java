package io.github.SilenceShine.shine.cloud.id;

import io.github.SilenceShine.shine.spring.orm.data.reactive.repository.SimpleR2DbcShineRepository;
import io.github.SilenceShine.shine.util.log.LogUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * @author SilenceShine
 * @since 1.0
 */
@EnableR2dbcAuditing
@SpringBootApplication
@EnableR2dbcRepositories(repositoryBaseClass = SimpleR2DbcShineRepository.class)
public class ShineIdApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(ShineIdApplication.class, args);
            LogUtil.info(ShineIdApplication.class, "shine-cloud-id 启动成功!");
        } catch (Exception e) {
            LogUtil.error(ShineIdApplication.class, "shine-cloud-id 启动失败:{}", e.getMessage());
        }
    }

}