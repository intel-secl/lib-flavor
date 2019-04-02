/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.model;

/**
 * 
 * @author ssbangal
 */
public class Hardware {
    private String vendor;
    private String processorInfo;
    private String processorFlags;
    private Feature feature;

    public String getVendor ()
    {
        return vendor;
    }

    public void setVendor (String vendor)
    {
        this.vendor = vendor;
    }

    public String getProcessorInfo() {
        return processorInfo;
    }

    public void setProcessorInfo(String processorInfo) {
        this.processorInfo = processorInfo;
    }

    public String getProcessorFlags() {
        return processorFlags;
    }

    public void setProcessorFlags(String processorFlags) {
        this.processorFlags = processorFlags;
    }

    public Feature getFeature ()
    {
        return feature;
    }

    public void setFeature (Feature feature)
    {
        this.feature = feature;
    }

}
