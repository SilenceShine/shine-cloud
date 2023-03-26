package io.github.SilenceShine.shine.cloud.id.handler;

import io.github.SilenceShine.shine.cloud.id.domain.BillNumber;
import io.github.SilenceShine.shine.cloud.id.repository.BillNumberRepository;
import io.github.SilenceShine.shine.core.exception.BizException;
import io.github.SilenceShine.shine.spring.cache.util.ReactiveRedisUtil;
import io.github.SilenceShine.shine.spring.core.function.ReactorFunctionR;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static io.github.SilenceShine.shine.cloud.id.exception.IdException.BILL_NUMBER_CODE_NOT_EXIST;
import static io.github.SilenceShine.shine.cloud.id.exception.IdException.BILL_NUMBER_CODE_NOT_NULL;

/**
 * 单据号生成器
 *
 * @author SilenceShine
 * @since 1.0
 */
@Slf4j
@Component
@AllArgsConstructor
public class BillNumberHandler {

    private final BillNumberRepository repository;
    private final RedissonReactiveClient redissonReactiveClient;

    public Mono<ServerResponse> single(ServerRequest request) {
        return Mono.justOrEmpty(request.queryParams().getFirst("code"))
                .switchIfEmpty(Mono.error(new BizException(BILL_NUMBER_CODE_NOT_NULL)))
                .map(code -> BillNumber.builder().code(code).build())
                .flatMap(domain -> repository.findOne(Example.of(domain)))
                .switchIfEmpty(Mono.error(new BizException(BILL_NUMBER_CODE_NOT_EXIST)))
                .flatMap(billNumber -> generateBillNumber(billNumber, 1))
                .flatMap(ReactorFunctionR::single);
    }

    public Mono<ServerResponse> batch(ServerRequest request) {
        return ReactorFunctionR.single();
    }

    /**
     * 根据code生成单据号 redis实现自增
     */
    private Mono<?> generateBillNumber(BillNumber billNumber, Integer size) {
        if (size == 1) {
            return ReactiveRedisUtil.increment("billNumber:" + billNumber.getCode())
                    .map(id -> billNumber.getPrefix() + id);
        } else {
            var rLockReactive = redissonReactiveClient.getLock("billNumber:");
            return rLockReactive.lock()
                    .delayUntil(unused -> rLockReactive.unlock())
                    .flatMap(unused -> ReactiveRedisUtil.increment("billNumber:" + billNumber.getCode(), size)
                            .flatMap(id -> Flux.range(Math.toIntExact(id - size), Math.toIntExact(id))
                                    .map(new Function<Integer, Object>() {
                                        @Override
                                        public Object apply(Integer integer) {
                                            return integer;
                                        }
                                    })
                                    .collectList())
                    );
        }
    }

}
