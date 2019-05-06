/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author ssbangal
 */
public enum ErrorCode {

    OK(0, "OK"), 
    SYSTEM_ERROR(5001,"System error. More information is available in the server log."), 
    UNKNOWN_ERROR(5002,"Unknown error. More information is available in the server log."),
    TPM_VERSION_NOT_SUPPORTED_ERROR(5003, "%s version of TPM is not supported by the platform"),
    UNKNOWN_FLAVOR_PART(5004, "Unknown flavor part specified."),
    UNKNOWN_VENDOR_SPECIFIED(5005, "Specified vendor is not supported"),
    UN_SUPPORTED_OS(5006, "Host operating system is not supported"),
    INVALID_INPUT(5007, "Invalid input specified."),
    FLAVOR_PART_CANNOT_BE_SUPPORTED(5008, "Requested flavor part cannot be supported. Please verify input parameters."),
    SOFTWARE_FLAVOR_CANNOT_BE_CREATED(5009, "No or invalid measurements, software flavor can not be created");

    public int getErrorCode() {
        
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private ErrorCode(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }
    
    private static class ErrorCodeCache {
        private static Map<Integer,ErrorCode> ecCache = new HashMap<Integer,ErrorCode>();

        static {
            for (final ErrorCode ec : ErrorCode.values()) {
                ecCache.put(ec.getErrorCode(), ec);
            }
        }
    }
    
    public static ErrorCode getErrorCode(int ec) {
        return ErrorCodeCache.ecCache.get(ec);
    }
    
    int errorCode;
    String message;
}
