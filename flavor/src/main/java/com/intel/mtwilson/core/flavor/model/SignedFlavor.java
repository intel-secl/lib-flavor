/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intel.dcsg.cpg.extensions.Extensions;
import com.intel.mtwilson.jackson.bouncycastle.BouncyCastleModule;
import com.intel.mtwilson.jackson.validation.ValidationModule;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;

import java.io.IOException;

public class SignedFlavor {
    private Flavor flavor;
    private String signature;

    public SignedFlavor() {
        this.flavor = null;
        this.signature = null;
    }

    public SignedFlavor(Flavor flavor, String signature) {
        this.flavor = flavor;
        this.signature = signature;
    }

    public Flavor getFlavor() {
        return flavor;
    }

    public void setFlavor(Flavor flavor) {
        this.flavor = flavor;
    }

    public String getSignature() { return signature; }

    public void setSignature(String signature) { this.signature = signature; }

    public static String serialize(SignedFlavor signedFlavor) throws JsonProcessingException {
        Extensions.register(Module.class, BouncyCastleModule.class);
        Extensions.register(Module.class, ValidationModule.class);

        try {
            ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            return mapper.writeValueAsString(signedFlavor);
        } catch (JsonProcessingException ex) {
            throw ex;
        }
    }

    public static SignedFlavor deserialize(String signedFlavor) throws IOException {
        Extensions.register(Module.class, BouncyCastleModule.class);
        Extensions.register(Module.class, ValidationModule.class);
        ObjectMapper mapper = JacksonObjectMapperProvider.createDefaultMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper.readValue(signedFlavor, SignedFlavor.class);
    }

}
