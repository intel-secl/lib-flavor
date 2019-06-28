/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.model;

import com.intel.wml.measurement.xml.MeasurementType;

import java.util.Map;

/**
 *
 * @author ddhawal
 */
public class Software {
    private Map<String, MeasurementType> measurements;
    private String cumulativeHash;

    public Map<String, MeasurementType> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(Map<String, MeasurementType> measurements) {
        this.measurements = measurements;
    }

    public String getCumulativeHash() {
        return cumulativeHash;
    }

    public void setCumulativeHash(String cumulativeHash) {
        this.cumulativeHash = cumulativeHash;
    }
}
