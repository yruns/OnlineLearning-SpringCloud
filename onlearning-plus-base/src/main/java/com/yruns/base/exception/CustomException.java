package com.yruns.base.exception;


import lombok.Data;

/**
 * CustomException
 */
@Data
public class CustomException extends RuntimeException{

    private String errMessage;

    public CustomException() {}

    public CustomException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public static void cast(String errMessage) {
        throw new CustomException(errMessage);
    }
}
