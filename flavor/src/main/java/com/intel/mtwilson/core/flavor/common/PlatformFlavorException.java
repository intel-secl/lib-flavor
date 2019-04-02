/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.common;

/**
 * 
 * @author ssbangal
 */
public class PlatformFlavorException extends Exception {

    ErrorCode errorCode = null;

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    private PlatformFlavorException() {}

    public PlatformFlavorException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PlatformFlavorException(ErrorCode errorCode, String message, Exception e) {
        super(message, e);
        this.errorCode = errorCode;
    }
}
