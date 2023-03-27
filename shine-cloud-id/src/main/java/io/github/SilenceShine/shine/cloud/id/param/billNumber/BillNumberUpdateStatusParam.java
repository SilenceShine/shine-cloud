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
public class BillNumberUpdateStatusParam extends IdentityParam {

    @NotNull(message = "状态不能为空")
    private Boolean status;

}
