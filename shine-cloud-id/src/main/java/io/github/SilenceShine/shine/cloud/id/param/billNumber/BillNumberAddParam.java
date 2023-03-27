package io.github.SilenceShine.shine.cloud.id.param.billNumber;

import io.github.SilenceShine.shine.core.param.BaseParam;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

/**
 * 单据号参数
 *
 * @author SilenceShine
 * @since 1.0
 */
@Getter
@Setter
public class BillNumberAddParam extends BaseParam {

    @NotNull(message = "name不能为空")
    private String name;

    @NotNull(message = "code不能为空")
    private String code;

    private String prefix;

    private String format;

    @Range(max = 9, message = "最长为9位可变长度")
    private Integer length;

    private String desc;

}
