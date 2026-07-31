package com.haikou.government.common.core.exception;

/**
 * 业务异常
 *
 * @author gov-platform
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
