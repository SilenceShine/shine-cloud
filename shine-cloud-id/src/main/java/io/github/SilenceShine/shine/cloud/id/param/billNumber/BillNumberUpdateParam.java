package io.github.SilenceShine.shine.cloud.id.param.billNumber;

import io.github.SilenceShine.shine.core.param.IdentityParam;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * @author SilenceShine
 * @since 1.0
 */
@Getter
@Setter
public class BillNumberUpdateParam extends IdentityParam {

    @NotNull(message = "name不能为空")
    private String name;

    private String prefix;

    private String format;

    private String desc;

}
