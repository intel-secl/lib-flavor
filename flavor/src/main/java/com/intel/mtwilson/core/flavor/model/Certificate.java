/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.core.flavor.model;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author ssbangal
 */
public class Certificate {

    private Map<String, Map<String, Set<String>>> attribute;
    private Issuer issuer;

    public Issuer getIssuer() {
        return issuer;
    }

    public void setIssuer(Issuer issuer) {
        this.issuer = issuer;
    }

    public Map<String, Map<String, Set<String>>> getAttribute() {
        return attribute;
    }

    public void setAttribute(Map<String, Map<String, Set<String>>> attribute) {
        this.attribute = attribute;
    }

    public static class Issuer {

        private String publicKey;

        public Issuer() {}
        
        public Issuer(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

    }
}
