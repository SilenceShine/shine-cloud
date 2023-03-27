package io.github.SilenceShine.shine.cloud.id.config;

import org.springframework.data.domain.ReactiveAuditorAware;
import reactor.core.publisher.Mono;

/**
 * @author SilenceShine
 * @since 1.0
 */
public class ShineIdReactiveAuditorAware implements ReactiveAuditorAware<String> {

    @Override
    public Mono<String> getCurrentAuditor() {
        return Mono.just("id");
    }

}
