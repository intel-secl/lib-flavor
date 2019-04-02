/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.model;

import com.intel.mtwilson.core.common.model.Measurement;
import java.util.List;

/**
 *
 * @author ssbangal
 */
public class PcrEx {

    // TODO Remove the digest class name
    private String value;
    private List<Measurement> event;

    public PcrEx() {
    }

    public PcrEx(String value, List<Measurement> event) {
        this.value = value;
        this.event = event;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Measurement> getEvent() {
        return event;
    }

    public void setEvent(List<Measurement> event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "Pojo [value - " + value + ", event - " + event + "]";
    }

    public static class Event {
    }

}
