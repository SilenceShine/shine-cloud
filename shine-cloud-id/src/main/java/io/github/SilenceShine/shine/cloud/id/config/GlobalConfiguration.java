package io.github.SilenceShine.shine.cloud.id.config;

import io.github.SilenceShine.shine.cloud.id.properties.GlobalProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;

/**
 * @author SilenceShine
 * @since 1.0
 */
@Configuration
@EnableConfigurationProperties(GlobalProperties.class)
public class GlobalConfiguration {

    @Bean
    public ReactiveAuditorAware<String> auditorProvider() {
        return new ShineIdReactiveAuditorAware();
    }

}
