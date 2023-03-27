package io.github.SilenceShine.shine.cloud.id.exception;

import io.github.SilenceShine.shine.core.exception.ExceptionEnum;
import io.github.SilenceShine.shine.core.exception.ResultStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author SilenceShine
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum IdException implements ExceptionEnum {

    BILL_NUMBER_CODE_EXIST(ResultStatus.BAD_REQUEST, "单据号code已存在!"),
    BILL_NUMBER_CODE_NOT_NULL(ResultStatus.BAD_REQUEST, "单据号code不能为空!"),
    BILL_NUMBER_CODE_NOT_EXIST(ResultStatus.BAD_REQUEST, "单据号code:%s 不存在!"),
    ;
    private final int code;
    private final String message;

    @Override
    public Integer code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

}
