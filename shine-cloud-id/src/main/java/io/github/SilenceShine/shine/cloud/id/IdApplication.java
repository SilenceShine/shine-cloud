package io.github.SilenceShine.shine.cloud.id;

import io.github.SilenceShine.shine.spring.orm.data.reactive.repository.SimpleR2DbcShineRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * @author SilenceShine
 * @since 1.0
 */
@SpringBootApplication
@EnableR2dbcRepositories(repositoryBaseClass = SimpleR2DbcShineRepository.class)
public class IdApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdApplication.class, args);
    }

}