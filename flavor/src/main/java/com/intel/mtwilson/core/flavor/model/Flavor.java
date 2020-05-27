/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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

import java.io.IOException;
import java.util.Map;

/**
 * 
 * @author ssbangal
 */
@JsonPropertyOrder({ "meta", "validity", "bios", "hardware", "pcrs", "external", "software"})
public class Flavor {
    private Meta meta;
    private Validity validity;
    private Bios bios;
    private Hardware hardware;
    private Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrs;
    private External external;
    private Software software;

    public Flavor() {}

    public Flavor(Meta meta, Bios bios, Hardware hardware, Map<DigestAlgorithm, Map<PcrIndex, PcrEx>> pcrs, External external, Software software) {
        this.meta = meta;
        this.bios = bios;
        this.hardware = hardware;
        this.pcrs = pcrs;
        this.external = external;
        this.software = software;
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

    public Bios getBios() {
        return bios;
    }

    public void setBios(Bios bios) {
        this.bios = bios;
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

    public Software getSoftware() {
        return software;
    }

    public void setSoftware(Software software) {
        this.software = software;
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

    public static Flavor deserialize(String flavor) throws IOException {
        Extensions.register(Module.class, BouncyCastleModule.class);
        Extensions.register(Module.class, ValidationModule.class);
        ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper.readValue(flavor, Flavor.class);
    }

    @Override
    public String toString() {
        return "Pojo [hardware - " + (hardware != null ? hardware.toString() : "") + 
                ", validity - " + (validity != null ? validity.toString() : "")  + 
                ", bios - " + (bios != null ? bios.toString() : "")  +
                ", meta - " + (meta != null ? meta.toString() : "")  +
                ", external - " + (external != null ? external.toString() : "") + "]";
    }
}
