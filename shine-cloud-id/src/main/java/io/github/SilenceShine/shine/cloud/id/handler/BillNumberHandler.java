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
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

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

    public Mono<ServerResponse> getByCode(ServerRequest request) {
        return Mono.justOrEmpty(request.queryParams())
                .flatMap(map -> getBillNumberByCode(map.getFirst("code"))
                        .map(billNumber -> Tuples.of(billNumber,
                                Optional.ofNullable(map.getFirst("size"))
                                        .map(Integer::parseInt)
                                        .orElse(1))))
                .flatMap(tuple2 -> generateBillNumber(tuple2.getT1(), tuple2.getT2()))
                .flatMap(ReactorFunctionR::single);
    }

    /**
     * 检查单据号是否存在
     */
    private Mono<Boolean> checkCodeExist(String code) {
        return repository.exists(Example.of(BillNumber.builder().code(code).build()))
                .flatMap(exists -> exists
                        ? ReactorExceptionUtil.monoBiz(BILL_NUMBER_CODE_EXIST)
                        : Mono.just(Boolean.TRUE)
                );
    }

    /**
     * 获取单据号
     */
    private Mono<BillNumber> getBillNumberByCode(String code) {
        if (null == code) return ReactorExceptionUtil.monoBiz(BILL_NUMBER_CODE_NOT_NULL);
        return repository.<BillNumber>findOne(Example.of(BillNumber.builder().code(code).build()))
                .switchIfEmpty(ReactorExceptionUtil.monoBiz(BILL_NUMBER_CODE_NOT_EXIST));
    }

    /**
     * 根据code生成单据号 redis实现自增
     */
    private Mono<List<String>> generateBillNumber(BillNumber billNumber, Integer size) {
        return ReactiveRedisUtil.increment(RedisConstant.BILL_NUMBER + billNumber.getCode(), size)
                .map(idMax -> LongStream.range(idMax - size, idMax).boxed())
                .flatMapMany(Flux::fromStream)
                .map(id -> buildBillNumber(billNumber, id))
                .collectList();
    }

    /**
     * 构建每一个单据号
     */
    private String buildBillNumber(BillNumber billNumber, Long id) {
        var prefix = billNumber.getPrefix();
        var date = Condition.convert(billNumber.getFormat(), StrUtil::isNotBlank, format ->
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(format)));
        return prefix + date + formatLong(id, billNumber.getLength());
    }

    /**
     * 单据号补位
     */
    public static String formatLong(long number, int length) {
        String str = Long.toString(number);
        int i = Math.max(0, length) - str.length();
        return i > 0 ? "0".repeat(i) + str : str;
    }

}
