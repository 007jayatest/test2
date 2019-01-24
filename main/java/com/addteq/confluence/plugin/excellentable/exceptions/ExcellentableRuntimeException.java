/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addteq.confluence.plugin.excellentable.exceptions;

/**
 *
 * @author vikash.kumar
 */
public class ExcellentableRuntimeException extends RuntimeException{

    public ExcellentableRuntimeException() {
    }

    public ExcellentableRuntimeException(String message) {
        super(message);
    }

    public ExcellentableRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcellentableRuntimeException(Throwable cause) {
        super(cause);
    }

    public ExcellentableRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
