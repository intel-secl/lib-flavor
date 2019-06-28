/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.model;

/**
 *
 * @author ddhawale
 */
public class Bios {
    private String biosName;
    private String biosVersion;

    public String getBiosName() {
        return biosName;
    }

    public void setBiosName(String biosName) {
        this.biosName = biosName;
    }

    public String getBiosVersion() {
        return biosVersion;
    }

    public void setBiosVersion(String biosVersion) {
        this.biosVersion = biosVersion;
    }

    @Override
    public String toString() {
        return "Pojo [biosName - " + (biosName != null ? biosName : "") +
                ", biosVersion - " + (biosVersion != null ? biosVersion : "") + "]";
    }
}
