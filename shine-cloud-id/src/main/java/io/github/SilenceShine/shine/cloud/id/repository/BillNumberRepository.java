package io.github.SilenceShine.shine.cloud.id.repository;

import io.github.SilenceShine.shine.cloud.id.domain.BillNumber;
import io.github.SilenceShine.shine.spring.orm.data.reactive.repository.R2dbcShineRepository;
import org.springframework.stereotype.Repository;

/**
 * @author SilenceShine
 * @since 1.0
 */
@Repository
public interface BillNumberRepository extends R2dbcShineRepository<BillNumber, Long> {
}
