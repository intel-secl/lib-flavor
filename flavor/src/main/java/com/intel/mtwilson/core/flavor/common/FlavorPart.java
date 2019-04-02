/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.common;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ddhawale
 */
public enum FlavorPart {
    PLATFORM("PLATFORM"),
    OS("OS"),
    HOST_UNIQUE("HOST_UNIQUE"),
    SOFTWARE("SOFTWARE"),
    ASSET_TAG("ASSET_TAG");

    private String value;

    FlavorPart(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static List<String> getValues() {
        List<String> flavorTypes = new ArrayList<>();
        for(FlavorPart flavorPart: values()) {
            flavorTypes.add(flavorPart.getValue());
        }
        return flavorTypes;
    }
}
