package io.github.SilenceShine.shine.cloud.id.domain;

import io.github.SilenceShine.shine.spring.orm.data.reactive.r2dbc.BaseDomain;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 单据号
 * prefix+format+length
 *
 * @author SilenceShine
 * @since 1.0
 */
@Getter
@Setter
@Table(name = "bill_number")
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BillNumber extends BaseDomain {

    @Column("name")
    private String name;

    @Column("code")
    private String code;

    @Column("prefix")
    private String prefix;

    @Column("format")
    private String format;

    @Column("length")
    private Integer length;

    @Column("status")
    private Boolean status;

    @Column("`desc`")
    private String desc;

}
