package io.github.SilenceShine.shine.cloud.id.handler;

import cn.hutool.core.util.IdUtil;
import io.github.SilenceShine.shine.spring.core.function.ReactorFunctionR;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 全局uid生成器
 *
 * @author SilenceShine
 * @since 1.0
 */
@Slf4j
@Component
public class SnowflakeIdHandler {

    private static final String PARAM_SIZE = "size";
    private static final int DEFAULT_SIZE = 1000;

    public Mono<ServerResponse> single(ServerRequest __) {
        return ReactorFunctionR.single(IdUtil.getSnowflakeNextId());
    }

    public Mono<ServerResponse> batch(ServerRequest request) {
        return Mono.justOrEmpty(request.queryParams().getFirst(PARAM_SIZE))
                .map(Integer::parseInt)
                .defaultIfEmpty(DEFAULT_SIZE)
                .flatMapMany(size -> Flux.range(0, size))
                .map(__ -> IdUtil.getSnowflakeNextId())
                .collectList()
                .flatMap(ReactorFunctionR::single);
    }

}
