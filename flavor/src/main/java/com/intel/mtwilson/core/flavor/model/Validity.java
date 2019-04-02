/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.model;

/**
 * 
 * @author ssbangal
 */
public class Validity {
    private String notBefore;
    private String notAfter;

    public String getNotBefore ()
    {
        return notBefore;
    }

    public void setNotBefore (String notBefore)
    {
        this.notBefore = notBefore;
    }

    public String getNotAfter ()
    {
        return notAfter;
    }

    public void setNotAfter (String notAfter)
    {
        this.notAfter = notAfter;
    }

    @Override
    public String toString()
    {
        return "Pojo [notBefore - "+notBefore+", notAfter - "+notAfter+"]";
    }
}