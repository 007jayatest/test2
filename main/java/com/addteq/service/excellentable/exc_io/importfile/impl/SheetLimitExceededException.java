package com.addteq.service.excellentable.exc_io.importfile.impl;

/**
 * Exception thrown when the sheet import is exceeding its limit of the number of cells
 * Created by yagnesh.bhat on 8/17/18.
 */
public class SheetLimitExceededException extends Exception {

    public SheetLimitExceededException(String message) {
        super(message);
    }

}
