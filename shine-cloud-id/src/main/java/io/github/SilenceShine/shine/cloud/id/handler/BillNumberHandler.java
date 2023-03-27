package io.github.SilenceShine.shine.cloud.id.handler;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import io.github.SilenceShine.shine.cloud.id.constant.RedisConstant;
import io.github.SilenceShine.shine.cloud.id.domain.BillNumber;
import io.github.SilenceShine.shine.cloud.id.param.billNumber.BillNumberAddParam;
import io.github.SilenceShine.shine.cloud.id.param.billNumber.BillNumberUpdateParam;
import io.github.SilenceShine.shine.cloud.id.param.billNumber.BillNumberUpdateStatusParam;
import io.github.SilenceShine.shine.cloud.id.repository.BillNumberRepository;
import io.github.SilenceShine.shine.cloud.id.util.SnowflakeIdUtil;
import io.github.SilenceShine.shine.core.util.ReactorExceptionUtil;
import io.github.SilenceShine.shine.spring.cache.util.ReactiveRedisUtil;
import io.github.SilenceShine.shine.spring.core.function.ReactorFunctionR;
import io.github.SilenceShine.shine.spring.validation.ObjectValidator;
import io.github.SilenceShine.shine.util.function.Condition;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import static io.github.SilenceShine.shine.cloud.id.exception.IdException.*;

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

    public Mono<ServerResponse> save(ServerRequest request) {
        return request.bodyToMono(BillNumberAddParam.class)
                .doOnNext(ObjectValidator::validate)
                .filterWhen(param -> checkCodeExist(param.getCode()))
                .map(param -> BeanUtil.copyProperties(param, BillNumber.class))
                .doOnNext(billNumber -> billNumber.setId(SnowflakeIdUtil.getSnowflakeId()))
                .flatMap(repository::save)
                .flatMap(ReactorFunctionR::single);
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        return request.bodyToMono(BillNumberUpdateParam.class)
                .doOnNext(ObjectValidator::validate)
                .flatMap(param -> repository.getById(param.getId())
                        .map(billNumber -> {
                            BeanUtil.copyProperties(param, billNumber);
                            return billNumber;
                        }))
                .flatMap(repository::save)
                .flatMap(ReactorFunctionR::single);
    }

    public Mono<ServerResponse> updateStatus(ServerRequest request) {
        return request.bodyToMono(BillNumberUpdateStatusParam.class)
                .doOnNext(ObjectValidator::validate)
                .flatMap(param -> repository.getById(param.getId())
                        .doOnNext(billNumber -> billNumber.setStatus(param.getStatus())))
                .flatMap(repository::save)
                .flatMap(ReactorFunctionR::single);
    }

    public Mono<ServerResponse> single(ServerRequest request) {
        final Mono<DataBuffer> emptyBufferMono = null;
        return Mono.justOrEmpty(request.queryParams().getFirst("code"))
                .switchIfEmpty(ReactorExceptionUtil.monoBiz(BILL_NUMBER_CODE_NOT_NULL))
                .map(code -> BillNumber.builder().code(code).build())
                .flatMap(domain -> repository.findOne(Example.of(domain)))
                .switchIfEmpty(ReactorExceptionUtil.monoBiz(BILL_NUMBER_CODE_NOT_EXIST))
                .flatMap(billNumber -> generateBillNumber(billNumber, 1))
                .flatMap(ReactorFunctionR::single);
    }

    private Mono<Boolean> checkCodeExist(String code) {
        return repository.exists(Example.of(BillNumber.builder().code(code).build()))
                .flatMap(exists -> exists
                        ? ReactorExceptionUtil.monoBiz(BILL_NUMBER_CODE_EXIST)
                        : Mono.just(Boolean.TRUE)
                );
    }

    public Mono<ServerResponse> batch(ServerRequest request) {
        return ReactorFunctionR.single();
    }

    /**
     * 根据code生成单据号 redis实现自增
     */
    private Mono<?> generateBillNumber(BillNumber billNumber, Integer size) {
        if (size == 1) {
            return ReactiveRedisUtil.increment(RedisConstant.BILL_NUMBER + billNumber.getCode())
                    .map(id -> {
                        return billNumber.getPrefix() + id;
                    });
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

    private String buildBillNumber(BillNumber billNumber, Long id) {
        var prefix = billNumber.getPrefix();

        var date = Condition.convert(billNumber.getFormat(), StrUtil::isNotBlank, format ->
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(format))
        );

        return prefix + date + id;
    }

}
