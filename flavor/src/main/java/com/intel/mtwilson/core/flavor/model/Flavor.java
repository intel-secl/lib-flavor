/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intel.dcsg.cpg.crypto.DigestAlgorithm;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.jackson.bouncycastle.BouncyCastleModule;
import com.intel.mtwilson.jackson.validation.ValidationModule;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import com.intel.mtwilson.core.common.model.PcrIndex;
import java.util.Map;

/**
 * 
 * @author ssbangal
 */
public class Flavor {
    private Meta meta;
    private Validity validity;
    private Hardware hardware;
    private Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrs;
    private External external;

    public Flavor() {}
    
    public Flavor(Meta meta, Hardware hardware, Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrs, External external) {
        this.meta = meta;
        this.hardware = hardware;
        this.pcrs = pcrs;
        this.external = external;
    }
    
    
    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public Validity getValidity() {
        return validity;
    }

    public void setValidity(Validity validity) {
        this.validity = validity;
    }

    public Hardware getHardware() {
        return hardware;
    }

    public void setHardware(Hardware hardware) {
        this.hardware = hardware;
    }

    public Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> getPcrs() {
        return pcrs;
    }

    public void setPcrs(Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrs) {
        this.pcrs = pcrs;
    }

    public External getExternal() {
        return external;
    }

    public void setExternal(External external) {
        this.external = external;
    }
    
    public static String serialize(Flavor flavor) throws JsonProcessingException {
        Extensions.register(Module.class, BouncyCastleModule.class);
        Extensions.register(Module.class, ValidationModule.class);
        
        try {
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            return mapper.writeValueAsString(flavor);
        } catch (JsonProcessingException ex) {
            throw ex;
        }        
    }
    
    
    @Override
    public String toString() {
        return "Pojo [hardware - " + (hardware != null ? hardware.toString() : "") + 
                ", validity - " + (validity != null ? validity.toString() : "")  + 
                ", meta - " + (meta != null ? meta.toString() : "")  + 
                ", external - " + (external != null ? external.toString() : "") + "]";
    }
}
